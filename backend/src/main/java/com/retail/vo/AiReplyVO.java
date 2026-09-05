package com.retail.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * AI 客服回复
 */
@Data
@Builder
public class AiReplyVO implements Serializable {

    private Long sessionId;
    private String reply;
    /** 回复来源：dify / mock */
    private String source;
}
