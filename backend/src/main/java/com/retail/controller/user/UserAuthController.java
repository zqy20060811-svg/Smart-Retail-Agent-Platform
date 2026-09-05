package com.retail.controller.user;

import com.retail.common.context.BaseContext;
import com.retail.common.result.Result;
import com.retail.dto.LoginDTO;
import com.retail.dto.RegisterDTO;
import com.retail.entity.User;
import com.retail.service.UserService;
import com.retail.vo.LoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * C 端用户：注册/登录/个人信息
 */
@RestController
@RequestMapping("/api/user/auth")
@RequiredArgsConstructor
@Api(tags = "用户端-账号")
public class UserAuthController {

    private final UserService userService;

    @PostMapping("/register")
    @ApiOperation("用户注册")
    public Result<Void> register(@Valid @RequestBody RegisterDTO registerDTO) {
        userService.register(registerDTO);
        return Result.success();
    }

    @PostMapping("/login")
    @ApiOperation("用户登录")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        return Result.success(userService.login(loginDTO));
    }

    @GetMapping("/me")
    @ApiOperation("获取当前登录用户")
    public Result<User> me() {
        return Result.success(userService.getById(BaseContext.getCurrentId()));
    }
}
