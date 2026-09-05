package com.retail.controller.user;

import com.retail.common.context.BaseContext;
import com.retail.common.result.PageResult;
import com.retail.common.result.Result;
import com.retail.dto.AiChatDTO;
import com.retail.entity.AiChatMessage;
import com.retail.entity.AiChatSession;
import com.retail.security.ratelimit.RateLimit;
import com.retail.service.AiChatService;
import com.retail.vo.AiReplyVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * C 端用户：AI 客服对话（带限流）
 */
@RestController
@RequestMapping("/api/user/ai")
@RequiredArgsConstructor
@Api(tags = "用户端-AI客服")
public class UserAiController {

    private final AiChatService aiChatService;

    @PostMapping("/chat")
    @ApiOperation("AI 对话（60 秒内最多 10 次）")
    @RateLimit(key = "chat", max = 10, window = 60)
    public Result<AiReplyVO> chat(@Valid @RequestBody AiChatDTO chatDTO) {
        return Result.success(aiChatService.chat(BaseContext.getCurrentId(), chatDTO));
    }

    @GetMapping("/sessions")
    @ApiOperation("我的会话列表")
    public Result<PageResult<AiChatSession>> sessions(@RequestParam(defaultValue = "1") Integer page,
                                                      @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(aiChatService.sessions(BaseContext.getCurrentId(), page, pageSize));
    }

    @GetMapping("/sessions/{id}/messages")
    @ApiOperation("会话消息记录")
    public Result<List<AiChatMessage>> messages(@PathVariable Long id) {
        return Result.success(aiChatService.messages(BaseContext.getCurrentId(), id));
    }
}
