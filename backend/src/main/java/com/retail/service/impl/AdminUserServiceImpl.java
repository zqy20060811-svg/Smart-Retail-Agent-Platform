package com.retail.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.retail.common.constant.JwtClaimsConstant;
import com.retail.common.constant.MessageConstant;
import com.retail.common.exception.BusinessException;
import com.retail.config.properties.JwtProperties;
import com.retail.dto.LoginDTO;
import com.retail.entity.AdminUser;
import com.retail.mapper.AdminUserMapper;
import com.retail.security.JwtUtil;
import com.retail.service.AdminUserService;
import com.retail.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl extends ServiceImpl<AdminUserMapper, AdminUser> implements AdminUserService {

    private final JwtProperties jwtProperties;

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        AdminUser admin = getOne(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getUsername, loginDTO.getUsername()));
        String md5 = DigestUtils.md5DigestAsHex(loginDTO.getPassword().getBytes(StandardCharsets.UTF_8));
        if (admin == null || !md5.equals(admin.getPassword())) {
            throw new BusinessException(MessageConstant.LOGIN_FAILED);
        }
        if (admin.getStatus() != null && admin.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.ADMIN_ID, admin.getId());
        String token = JwtUtil.createJWT(jwtProperties.getAdminSecretKey(), jwtProperties.getTtl(), claims);

        return LoginVO.builder()
                .id(admin.getId())
                .username(admin.getUsername())
                .name(admin.getName())
                .token(token)
                .build();
    }
}
