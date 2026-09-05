package com.retail.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.retail.entity.Promotion;
import com.retail.mapper.PromotionMapper;
import org.springframework.stereotype.Service;

/**
 * 优惠活动服务
 */
@Service
public class PromotionService extends ServiceImpl<PromotionMapper, Promotion> {
}
