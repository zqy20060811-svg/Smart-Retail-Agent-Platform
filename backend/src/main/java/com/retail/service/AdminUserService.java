package com.retail.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.retail.dto.LoginDTO;
import com.retail.entity.AdminUser;
import com.retail.vo.LoginVO;

public interface AdminUserService extends IService<AdminUser> {

    /** 管理端登录 */
    LoginVO login(LoginDTO loginDTO);
}
