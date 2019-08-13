package com.sunkaisens.ibss.system.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.sunkaisens.ibss.common.annotation.Log;
import com.sunkaisens.ibss.common.controller.BaseController;
import com.sunkaisens.ibss.common.domain.QueryRequest;
import com.sunkaisens.ibss.common.domain.RetrueCode;
import com.sunkaisens.ibss.common.domain.SunkResponse;
import com.sunkaisens.ibss.common.exception.SysInnerException;
import com.sunkaisens.ibss.common.utils.MD5Util;
import com.sunkaisens.ibss.system.domain.User;
import com.sunkaisens.ibss.system.domain.UserConfig;
import com.sunkaisens.ibss.system.service.UserConfigService;
import com.sunkaisens.ibss.system.service.UserService;
import com.wuwenze.poi.ExcelKit;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping("user")
@Api(tags="用户管理")
public class UserController extends BaseController {

    private String message;
    
    @Autowired
    private UserService userService;
    @Autowired
    private UserConfigService userConfigService;
    
    @GetMapping("check/{username}")
    public boolean checkUserName(@NotBlank(message = "{required}") @PathVariable String username) {
        return this.userService.findByName(username) == null;
    }

    @GetMapping("/{username}")
    @ApiOperation(value="获得单条的用户信息", notes="username   string类型")
    public User detail(@NotBlank(message = "{required}") @PathVariable String username) {
        return this.userService.findByName(username);
    }

    
    //获得用户的列表数据 查询   徐胜浩  2019/7/23
    @GetMapping
    @RequiresPermissions("user:view")
    @ApiOperation(value="获得全部的用户信息和条件获取用户信息")
    public Map<String, Object> userList(QueryRequest queryRequest, User user) {
        return getDataTable(userService.findUserDetail(user, queryRequest));
    }

    /**
     * xsh 2019/8/8 用户添加的修改 添加一个返回值
     * @param user
     * @return
     * @throws SysInnerException
     */
    @Log("新增用户")
    @PostMapping
	@RequiresPermissions("user:add")
    @ApiOperation(value="新增用户", notes="传入user")
    public Map<String, Object> addUser(@Valid @RequestBody User user) throws SysInnerException {
    	try {
            this.userService.createUser(user);
            //SunkResponse 向前台传状态值  RetrueCode.OK 0成功  ;   RetrueCode.ERROR(1) 1：失败
            return new SunkResponse().retureCode(RetrueCode.OK).message("添加成功");
        } catch (Exception e) {
            message = "添加失败";
            log.error(message, e);
            throw new SysInnerException(message);
        }
    }  
  
    /**
     * xsh 2019/8/8 用户修改的修改 添加一个返回值
     * @param user
     * @return
     * @throws SysInnerException
     */
    @Log("修改用户")
    @PutMapping
    @RequiresPermissions("user:update")
    @ApiOperation(value="修改用户", notes="传入user")
    public Map<String, Object> updateUser(@Valid @RequestBody User user) throws SysInnerException {
    	try {
            this.userService.updateUser(user);
            //SunkResponse 向前台传状态值  RetrueCode.OK 0成功  ;   RetrueCode.ERROR(1) 1：失败
            return new SunkResponse().retureCode(RetrueCode.OK).message("修改成功");
        } catch (Exception e) {
            message = "修改用户失败";
            log.error(message, e);
            throw new SysInnerException(message);
        }
    }
    /**
     * xsh 2019/8/8 用户删除的修改   添加一个返回值
     * @param user
     * @return
     * @throws SysInnerException
     */
    @Log("删除用户")
    @DeleteMapping("/{userIds}")
	@RequiresPermissions("user:delete")
    @ApiOperation(value="删除用户", notes="传入user")
    public Map<String, Object> deleteUsers(@NotBlank(message = "{required}") @PathVariable String userIds) throws SysInnerException {
    	try {
            String[] ids = userIds.split(StringPool.COMMA);
            this.userService.deleteUsers(ids);
            //SunkResponse 向前台传状态值  RetrueCode.OK 0成功  ;   RetrueCode.ERROR(1) 1：失败
            return new SunkResponse().retureCode(RetrueCode.OK).message("删除成功");
        } catch (Exception e) {
            message = "删除失败";
            log.error(message, e);
            throw new SysInnerException(message);
        }
    }

    @PutMapping("profile")
    public void updateProfile(@Valid User user) throws SysInnerException {
        try {
            this.userService.updateProfile(user);
        } catch (Exception e) {
            message = "修改个人信息失败";
            log.error(message, e);
            throw new SysInnerException(message);
        }
    }

    @PutMapping("avatar")
    public void updateAvatar(
            @NotBlank(message = "{required}") String username,
            @NotBlank(message = "{required}") String avatar) throws SysInnerException {
        try {
            this.userService.updateAvatar(username, avatar);
        } catch (Exception e) {
            message = "修改头像失败";
            log.error(message, e);
            throw new SysInnerException(message);
        }
    }

    @PutMapping("userconfig")
    public void updateUserConfig(@Valid UserConfig userConfig) throws SysInnerException {
        try {
            this.userConfigService.update(userConfig);
        } catch (Exception e) {
            message = "修改个性化配置失败";
            log.error(message, e);
            throw new SysInnerException(message);
        }
    }

    @GetMapping("password/check")
    public boolean checkPassword(
            @NotBlank(message = "{required}") String username,
            @NotBlank(message = "{required}") String password) {
        String encryptPassword = MD5Util.encrypt(username, password);
        User user = userService.findByName(username);
        if (user != null)
            return StringUtils.equals(user.getPassword(), encryptPassword);
        else
            return false;
    }

    @PutMapping("password")
    public void updatePassword(
            @NotBlank(message = "{required}") String username,
            @NotBlank(message = "{required}") String password) throws SysInnerException {
        try {
            userService.updatePassword(username, password);
        } catch (Exception e) {
            message = "修改密码失败";
            log.error(message, e);
            throw new SysInnerException(message);
        }
    }

    @PutMapping("password/reset")
    @RequiresPermissions("user:reset")
    public void resetPassword(@NotBlank(message = "{required}") String usernames) throws SysInnerException {
        try {
            String[] usernameArr = usernames.split(StringPool.COMMA);
            this.userService.resetPassword(usernameArr);
        } catch (Exception e) {
            message = "重置用户密码失败";
            log.error(message, e);
            throw new SysInnerException(message);
        }
    }

    @PostMapping("excel")
    @RequiresPermissions("user:export")
    @ApiOperation(value="导出角色")
    public void export(QueryRequest queryRequest, User user, HttpServletResponse response) throws SysInnerException {
        try {
            List<User> users = this.userService.findUserDetail(user, queryRequest).getRecords();
            ExcelKit.$Export(User.class, response).downXlsx(users, false);
        } catch (Exception e) {
            message = "导出Excel失败";
            log.error(message, e);
            throw new SysInnerException(message);
        }
    }
}
