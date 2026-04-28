package com.pajamas.learning.service;

import com.pajamas.common.domain.PageDTO;
import com.pajamas.learning.domain.dto.ReplyDTO;
import com.pajamas.learning.domain.po.InteractionReply;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pajamas.learning.domain.query.ReplyPageQuery;
import com.pajamas.learning.domain.vo.ReplyVO;

/**
 * <p>
 * 互动问题的回答或评论 服务类
 * </p>
 *
 * @author lipengjie
 * @since 2026-04-26
 */
public interface IInteractionReplyService extends IService<InteractionReply> {

    void saveReply(ReplyDTO replyDTO);

    PageDTO<ReplyVO> queryReplyPage(ReplyPageQuery query);

}
