package com.retail.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.retail.entity.Category;
import com.retail.mapper.CategoryMapper;
import org.springframework.stereotype.Service;

/**
 * 分类服务（简单 CRUD 直接复用 MyBatis-Plus ServiceImpl）
 */
@Service
public class CategoryService extends ServiceImpl<CategoryMapper, Category> {
}
