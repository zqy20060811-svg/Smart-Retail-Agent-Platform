package com.retail.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * C 端用户
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class User extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 登录用户名（手机号/自定义） */
    private String username;

    private String nickname;

    private String phone;

    @JsonIgnore
    private String password;

    private String avatar;

    /** 状态：1 正常 0 禁用 */
    private Integer status;
}
