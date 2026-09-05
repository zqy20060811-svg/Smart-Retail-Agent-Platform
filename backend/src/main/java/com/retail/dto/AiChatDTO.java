package com.retail.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * AI 客服对话请求
 */
@Data
public class AiChatDTO implements Serializable {

    /** 会话ID，首次对话为空，后端自动创建会话 */
    private Long sessionId;

    @NotBlank(message = "提问内容不能为空")
    private String content;
}
