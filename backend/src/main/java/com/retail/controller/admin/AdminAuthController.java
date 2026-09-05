package com.retail.controller.admin;

import com.retail.common.result.Result;
import com.retail.dto.LoginDTO;
import com.retail.service.AdminUserService;
import com.retail.vo.LoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 管理端：登录
 */
@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
@Api(tags = "管理端-账号")
public class AdminAuthController {

    private final AdminUserService adminUserService;

    @PostMapping("/login")
    @ApiOperation("管理员登录")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        return Result.success(adminUserService.login(loginDTO));
    }
}
