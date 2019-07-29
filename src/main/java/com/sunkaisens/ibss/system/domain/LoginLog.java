package com.sunkaisens.ibss.system.domain;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@TableName("t_login_log")
@Data
public class LoginLog {
    /**
     * 用户 ID
     */
    private String username;

    /**
     * 登录时间
     */
    private Date loginTime;

    /**
     * 登录地点
     */
    private String location;

    private String ip;
}
