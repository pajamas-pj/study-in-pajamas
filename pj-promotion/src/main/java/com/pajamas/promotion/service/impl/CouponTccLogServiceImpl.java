package com.pajamas.promotion.service.impl;

import com.alibaba.cola.statemachine.StateMachine;
import com.pajamas.common.exceptions.BadRequestException;
import com.pajamas.common.exceptions.DbException;
import com.pajamas.promotion.domain.dto.UserCouponContext;
import com.pajamas.promotion.domain.po.CouponTccLog;
import com.pajamas.promotion.domain.po.UserCoupon;
import com.pajamas.promotion.enums.CouponTccStatus;
import com.pajamas.promotion.enums.UserCouponEvent;
import com.pajamas.promotion.enums.UserCouponStatus;
import com.pajamas.promotion.mapper.CouponTccLogMapper;
import com.pajamas.promotion.service.ICouponTccLogService;
import com.pajamas.promotion.service.IUserCouponService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.seata.core.context.RootContext;
import io.seata.rm.tcc.api.BusinessActionContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 优惠券TCC事务记录 服务实现类
 * </p>
 *
 * @author lipengjie
 * @since 2026-06-08
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CouponTccLogServiceImpl extends ServiceImpl<CouponTccLogMapper, CouponTccLog> implements ICouponTccLogService {

    private final IUserCouponService userCouponService;
    private final StateMachine<UserCouponStatus, UserCouponEvent, UserCouponContext> userCouponStateMachine;

    @Override
    @Transactional
    public boolean batchTry(BusinessActionContext context, List<Long> userCouponIds, String orderNo) {
        //1.获取全局与分支事务id
        String txId = RootContext.getXID();
        Long branchId = context.getBranchId();
        //2.遍历锁定优惠券
        for (Long userCouponId : userCouponIds) {
            //3.尝试提前插入，幂等唯一索引防重复
            CouponTccLog couponTccLog = new CouponTccLog()
                    .setTxId(txId)
                    .setBranchId(branchId)
                    .setCouponId(userCouponId)
                    .setStatus(CouponTccStatus.TRIED);
            try {
                save(couponTccLog);
            } catch (DuplicateKeyException e) {
                //4.冲突：说明记录已存在，查状态
                CouponTccLog existing = lambdaQuery()
                        .select(CouponTccLog::getStatus)
                        .eq(CouponTccLog::getTxId, txId)
                        .eq(CouponTccLog::getBranchId, branchId)
                        .one();
                //4.1防悬挂：Try 执行时查事务表，发现已有 Cancel 记录，直接拒绝
                //Cancel 比 Try 先到，不拦住 Try 资金永久冻结
                if (existing.getStatus() == CouponTccStatus.CANCELLED) {
                    log.warn("防悬挂：Try 在 Cancel 之后执行, txId={}, userCouponId={}", txId, userCouponId);
                    return false;
                }
                //4.2幂等：已存在
                log.info("Try 幂等：流水已存在, txId={}, userCouponId={}", txId, userCouponId);
                continue;
            }
            //5.插入成功，查询用户券
            UserCoupon userCoupon = userCouponService.getById(userCouponId);
            if (userCoupon == null) {
                throw new BadRequestException("用户券不存在: userCouponId=" + userCouponId);
            }
            //6.状态机校验并锁定
            UserCouponContext ctx = UserCouponContext.builder()
                    .userCouponId(userCouponId)
                    .userId(userCoupon.getUserId())
                    .couponId(userCoupon.getCouponId())
                    .operateTime(LocalDateTime.now())
                    .build();
            UserCouponStatus target = userCouponStateMachine.fireEvent(userCoupon.getStatus(), UserCouponEvent.LOCK, ctx);
            if (target == null) {
                //状态机不允许当前状态转换，跳过（幂等处理）
                removeById(couponTccLog);
                throw new BadRequestException("优惠券状态异常, 无法锁定: userCouponId=" + userCouponId);
            }
            //7.更新用户券状态 乐观锁
            boolean updated = userCouponService.lambdaUpdate()
                    .set(UserCoupon::getStatus, target)
                    .eq(UserCoupon::getId, userCouponId)
                    .eq(UserCoupon::getStatus, userCoupon.getStatus())
                    .update();
            if (!updated) {
                //更新失败
                throw new DbException("Try 锁定优惠券失败: userCouponId=" + userCouponId);
            }
        }
        return true;
    }

    @Override
    @Transactional
    public boolean batchConfirm(BusinessActionContext context) {
        //1.获取全局与分支事务id
        String txId = context.getXid();
        Long branchId = context.getBranchId();
        //2.尝试批量更新状态 乐观锁
        boolean update = lambdaUpdate()
                .set(CouponTccLog::getStatus, CouponTccStatus.CONFIRMED)
                .eq(CouponTccLog::getTxId, txId)
                .eq(CouponTccLog::getBranchId, branchId)
                .eq(CouponTccLog::getStatus, CouponTccStatus.TRIED)
                .update();
        if (!update) {
            //3.更新失败，说明状态已修改或不存在，查状态
            List<CouponTccLog> existingList = lambdaQuery()
                    .select(CouponTccLog::getStatus)
                    .eq(CouponTccLog::getTxId, txId)
                    .eq(CouponTccLog::getBranchId, branchId)
                    .list();
            if (existingList == null || existingList.isEmpty()) {
                throw new BadRequestException("事务记录不存在, txId=" + txId);
            }
            //3.1幂等：全部已处理
            boolean allConfirmed = existingList.stream()
                    .allMatch(item -> item.getStatus() == CouponTccStatus.CONFIRMED);
            if (allConfirmed) {
                log.info("Confirm 幂等：流水已修改, txId={}", txId);
                return true;
            }
            boolean anyCancelled = existingList.stream()
                    .anyMatch(item -> item.getStatus() == CouponTccStatus.CANCELLED);
            if (anyCancelled) {
                throw new BadRequestException("事务记录状态未异常");
            }
        }
        //4.更新成功,查询全部记录
        List<CouponTccLog> logList = lambdaQuery()
                .eq(CouponTccLog::getTxId, txId)
                .eq(CouponTccLog::getBranchId, branchId)
                .list();
        //5.遍历确认核销
        for (CouponTccLog item : logList) {
            //5.1查询用户券
            UserCoupon userCoupon = userCouponService.getById(item.getCouponId());
            if (userCoupon == null) {
                throw new BadRequestException("用户券不存在: id=" + item.getCouponId());
            }
            //5.3状态机校验并核销
            UserCouponContext ctx = UserCouponContext.builder()
                    .userCouponId(item.getCouponId())
                    .userId(userCoupon.getUserId())
                    .couponId(userCoupon.getCouponId())
                    .operateTime(LocalDateTime.now())
                    .build();
            UserCouponStatus target = userCouponStateMachine.fireEvent(userCoupon.getStatus(), UserCouponEvent.USE, ctx);
            if (target == null) {
                throw new BadRequestException("用户券状态异常, 无法核销: userCouponId=" + item.getCouponId());
            }
            //5.4更新用户券状态 乐观锁
            boolean updated = userCouponService.lambdaUpdate()
                    .set(UserCoupon::getStatus, target)
                    .eq(UserCoupon::getId, item.getCouponId())
                    .eq(UserCoupon::getStatus, userCoupon.getStatus())
                    .update();
            if (!updated) {
                throw new DbException("Confirm 更新用户券失败, userCouponId=" + item.getCouponId());
            }
        }
        return true;
    }

    @Override
    @Transactional
    public boolean batchCancel(BusinessActionContext context) {
        //1.获取全局与分支事务id
        String txId = context.getXid();
        Long branchId = context.getBranchId();
        //2.尝试批量更新状态 乐观锁
        boolean update = lambdaUpdate()
                .set(CouponTccLog::getStatus, CouponTccStatus.CANCELLED)
                .eq(CouponTccLog::getTxId, txId)
                .eq(CouponTccLog::getBranchId, branchId)
                .eq(CouponTccLog::getStatus, CouponTccStatus.TRIED)
                .update();
        if (!update) {
            //3.更新失败，说明状态已修改或不存在，查状态
            List<CouponTccLog> existingList = lambdaQuery()
                    .select(CouponTccLog::getStatus)
                    .eq(CouponTccLog::getTxId, txId)
                    .eq(CouponTccLog::getBranchId, branchId)
                    .list();
            //3.1不存在
            if (existingList == null || existingList.isEmpty()) {
                //3.2防悬挂：插入cancel占位记录
                //网络拥堵，Cancel 比 Try 先到，不拦住 Try 资金永久冻结
                save(new CouponTccLog()
                        .setTxId(txId)
                        .setBranchId(branchId)
                        .setCouponId(0L)
                        .setStatus(CouponTccStatus.CANCELLED));
                //3.3空回滚：请求丢包,Try 没执行，TC 发起了Cancel
                //事务控制表，Cancel 执行前查不到 Try 记录直接返回成功
                log.info("Cancel 空回滚：Try 未执行, txId={}", txId);
                return true;
            }
            //3.4已修改 幂等：状态机 或通过 全局事务 id 做唯一索引校验（额外维护confirm表）
            boolean allCancelled = existingList.stream()
                    .allMatch(item -> item.getStatus() == CouponTccStatus.CANCELLED);
            if (allCancelled) {
                log.info("Cancel 幂等：流水已修改, txId={}", txId);
                return true;
            }
            boolean anyConfirmed = existingList.stream()
                    .anyMatch(item -> item.getStatus() == CouponTccStatus.CONFIRMED);
            if (anyConfirmed) {
                throw new BadRequestException("事务记录状态未异常");
            }
        }
        //4.更新成功，查询全部记录
        List<CouponTccLog> logList = lambdaQuery()
                .select(CouponTccLog::getCouponId)
                .eq(CouponTccLog::getTxId, txId)
                .eq(CouponTccLog::getBranchId, branchId)
                .list();
        //5.遍历回滚解锁
        for (CouponTccLog item : logList) {
            //5.2查询用户券
            UserCoupon userCoupon = userCouponService.getById(item.getCouponId());
            if (userCoupon == null) {
                throw new BadRequestException("用户券不存在: id=" + item.getCouponId());
            }
            //5.3状态机校验并解锁
            UserCouponContext ctx = UserCouponContext.builder()
                    .userCouponId(item.getCouponId())
                    .userId(userCoupon.getUserId())
                    .couponId(userCoupon.getCouponId())
                    .operateTime(LocalDateTime.now())
                    .build();
            UserCouponStatus target = userCouponStateMachine.fireEvent(userCoupon.getStatus(), UserCouponEvent.UNLOCK, ctx);
            if (target == null) {
                throw new BadRequestException("用户券状态异常, 无法解锁: userCouponId=" + item.getCouponId());
            }
            //5.4恢复用户券状态 乐观锁
            boolean updated = userCouponService.lambdaUpdate()
                    .set(UserCoupon::getStatus, target)
                    .eq(UserCoupon::getId, item.getCouponId())
                    .eq(UserCoupon::getStatus, userCoupon.getStatus())
                    .update();
            if (!updated) {
                throw new DbException("Cancel 释放优惠券失败, userCouponId=" + item.getCouponId());
            }
        }
        return true;
    }
}
