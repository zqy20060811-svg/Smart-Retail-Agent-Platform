package com.retail.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 登录成功返回
 */
@Data
@Builder
public class LoginVO implements Serializable {

    private Long id;
    private String username;
    private String name;
    private String token;
}
