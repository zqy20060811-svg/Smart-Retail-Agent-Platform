package com.retail.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.retail.entity.AiChatMessage;
import com.retail.mapper.AiChatMessageMapper;
import org.springframework.stereotype.Service;

@Service
public class AiChatMessageService extends ServiceImpl<AiChatMessageMapper, AiChatMessage> {
}
