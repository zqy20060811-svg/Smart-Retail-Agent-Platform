package com.retail.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.retail.common.constant.JwtClaimsConstant;
import com.retail.common.constant.MessageConstant;
import com.retail.common.exception.BusinessException;
import com.retail.config.properties.JwtProperties;
import com.retail.dto.LoginDTO;
import com.retail.dto.RegisterDTO;
import com.retail.entity.User;
import com.retail.mapper.UserMapper;
import com.retail.security.JwtUtil;
import com.retail.service.UserService;
import com.retail.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final JwtProperties jwtProperties;

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        User user = getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, loginDTO.getUsername()));
        String md5 = DigestUtils.md5DigestAsHex(loginDTO.getPassword().getBytes(StandardCharsets.UTF_8));
        if (user == null || !md5.equals(user.getPassword())) {
            throw new BusinessException(MessageConstant.LOGIN_FAILED);
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getTtl(), claims);

        return LoginVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getNickname())
                .token(token)
                .build();
    }

    @Override
    public void register(RegisterDTO registerDTO) {
        Long count = lambdaCount(registerDTO.getUsername());
        if (count != null && count > 0) {
            throw new BusinessException(MessageConstant.USER_EXISTS);
        }
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setNickname(registerDTO.getNickname() != null ? registerDTO.getNickname() : registerDTO.getUsername());
        user.setPhone(registerDTO.getPhone());
        user.setPassword(DigestUtils.md5DigestAsHex(registerDTO.getPassword().getBytes(StandardCharsets.UTF_8)));
        user.setStatus(1);
        save(user);
    }

    private Long lambdaCount(String username) {
        return this.count(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }
}
