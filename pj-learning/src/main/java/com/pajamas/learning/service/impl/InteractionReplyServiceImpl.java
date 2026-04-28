package com.pajamas.learning.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pajamas.api.client.user.UserClient;
import com.pajamas.api.dto.user.UserDTO;
import com.pajamas.common.domain.PageDTO;
import com.pajamas.common.utils.BeanUtils;
import com.pajamas.common.utils.CollUtils;
import com.pajamas.common.utils.UserContext;
import com.pajamas.learning.domain.dto.ReplyDTO;
import com.pajamas.learning.domain.po.InteractionQuestion;
import com.pajamas.learning.domain.po.InteractionReply;
import com.pajamas.learning.domain.query.ReplyPageQuery;
import com.pajamas.learning.domain.vo.ReplyVO;
import com.pajamas.learning.enums.QuestionStatus;
import com.pajamas.learning.mapper.InteractionReplyMapper;
import com.pajamas.learning.service.IInteractionQuestionService;
import com.pajamas.learning.service.IInteractionReplyService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 互动问题的回答或评论 服务实现类
 * </p>
 *
 * @author lipengjie
 * @since 2026-04-26
 */
@Service
@RequiredArgsConstructor
public class InteractionReplyServiceImpl extends ServiceImpl<InteractionReplyMapper, InteractionReply> implements IInteractionReplyService {
    private final IInteractionQuestionService questionService;
    private final UserClient userClient;
    @Override
    public void saveReply(ReplyDTO replyDTO) {
        //1.转换
        InteractionReply reply = BeanUtils.copyBean(replyDTO, InteractionReply.class);
        //获取用户id
        Long userId = UserContext.getUser();
        reply.setUserId(userId);
        save(reply);
        //2.查询是否有上级回答，没有则是回答
        Long answerId = reply.getAnswerId();
        Long questionId = reply.getQuestionId();
        InteractionQuestion question =questionService.getById(questionId);
        if(answerId == null) {
            //2.1.是回答，修改问题的最近一次回答和回答数量
            question.setLatestAnswerId(reply.getId());
            question.setAnswerTimes(question.getAnswerTimes() + 1);
            questionService.updateById(question);
        }else {
            //2.2.是评论，更新回答下评论数量
            InteractionReply rep = getById(reply.getId());
            rep.setReplyTimes(rep.getReplyTimes() + 1);
            updateById(rep);
        }
        //2.3如果是学生回答，修改问题的状态
        if(replyDTO.getIsStudent()) {
            question.setStatus(QuestionStatus.UN_CHECK);
            questionService.updateById(question);
        }
        //3.保存数据
        save(reply);
    }

    @Override
    public PageDTO<ReplyVO> queryReplyPage(ReplyPageQuery query) {
        //1.分页查询
        Long questionId = query.getQuestionId();
        Long answerId = query.getAnswerId();

        Page<InteractionReply> page = lambdaQuery()
                .eq(questionId != null, InteractionReply::getQuestionId, questionId)
                .eq(answerId != null, InteractionReply::getAnswerId, answerId)
                .eq(InteractionReply::getHidden, false)
                .page(query.toMpPageDefaultSortByCreateTimeDesc());
        List<InteractionReply> records = page.getRecords();
        if (CollUtils.isEmpty(records)) {
            return PageDTO.empty(page);
        }
        //2.准备数据，回答：用户信息，评论：用户信息，回复回答的用户信息
        Set<Long> userIds = new HashSet<>();
        Set<Long> replyIds = new HashSet<>();
        for(InteractionReply r : records){
            if(!r.getAnonymity()){
                //回答
                userIds.add(r.getUserId());
            }
            if(r.getTargetReplyId() != null){
                //评论
                replyIds.add(r.getTargetReplyId());
                userIds.add(r.getTargetUserId());
            }
        }
        List<UserDTO> users = userClient.queryUserByIds(userIds);
        Map<Long, UserDTO> userMap = new HashMap<>(users.size());
        if (CollUtils.isNotEmpty(users)) {
            userMap = users.stream().collect(Collectors.toMap(UserDTO::getId, u -> u));
        }

        List<InteractionReply> replies = new ArrayList<>(replyIds.size());
        Map<Long, InteractionReply> replyMap = new HashMap<>(replyIds.size());
        if (CollUtils.isNotEmpty(replies)) {
            replyMap = replies.stream().collect(Collectors.toMap(InteractionReply::getId, r -> r));
        }
        //4.封装数据
        List<ReplyVO> voList = new ArrayList<>(records.size());
        for(InteractionReply r : records){
            ReplyVO vo = BeanUtils.copyBean(r, ReplyVO.class);
            voList.add(vo);
            UserDTO user = userMap.get(r.getUserId());
            if(user != null){
                vo.setUserName(user.getName());
                vo.setUserIcon(user.getIcon());
            }
            //根据评论上级回答id查询回答是或否是匿名
            InteractionReply reply = replyMap.get(r.getTargetReplyId());
            if (!reply.getAnonymity()) {
                UserDTO targetUser = userMap.get(reply.getUserId());
                vo.setTargetUserName(targetUser.getName());
            }
        }
        //5.返回
        return PageDTO.of(page, voList);
    }
}
