package com.retail.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.retail.dto.LoginDTO;
import com.retail.dto.RegisterDTO;
import com.retail.entity.User;
import com.retail.vo.LoginVO;

public interface UserService extends IService<User> {

    /** C 端用户登录 */
    LoginVO login(LoginDTO loginDTO);

    /** C 端用户注册 */
    void register(RegisterDTO registerDTO);
}
