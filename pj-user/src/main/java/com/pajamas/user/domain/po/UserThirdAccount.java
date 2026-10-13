package com.pajamas.user.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户第三方账号关联
 * </p>
 *
 * @author lipengjie
 * @since 2026-10-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_third_account")
public class UserThirdAccount implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户id（关联 user.id）
     */
    private Long userId;

    /**
     * 平台类型：1-微信小程序 2-微信公众号 3-微信App 4-支付宝小程序 5-支付宝App
     */
    private Integer platform;

    /**
     * 应用id（不同小程序的 appid 不同）
     */
    private String appId;

    /**
     * 第三方平台下的唯一标识
     */
    private String openId;

    /**
     * 微信开放平台 unionid（同一开放平台下多应用共享）
     */
    private String unionId;

    /**
     * 第三方昵称
     */
    private String nickname;

    /**
     * 第三方头像
     */
    private String avatar;

    /**
     * 扩展信息（如 session_key 等，敏感数据谨慎存储）
     */
    private String extra;

    /**
     * 绑定时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 逻辑删除：0-未删除 1-已删除
     */
    private Integer deleted;


}
