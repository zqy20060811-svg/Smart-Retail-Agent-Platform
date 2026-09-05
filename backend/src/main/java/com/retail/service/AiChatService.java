package com.retail.service;

import com.retail.common.result.PageResult;
import com.retail.dto.AiChatDTO;
import com.retail.entity.AiChatMessage;
import com.retail.vo.AiReplyVO;

import java.util.List;

public interface AiChatService {

    /**
     * 对话：优先调用 Dify Agent，未配置时降级为基于本地知识库的 mock 回复
     */
    AiReplyVO chat(Long userId, AiChatDTO chatDTO);

    /** 会话列表 */
    PageResult<com.retail.entity.AiChatSession> sessions(Long userId, Integer page, Integer pageSize);

    /** 会话消息记录 */
    List<AiChatMessage> messages(Long userId, Long sessionId);
}
