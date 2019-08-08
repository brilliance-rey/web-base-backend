package com.sunkaisens.ibss.system.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.apache.poi.ss.formula.functions.IDStarAlgorithm;
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
import com.fasterxml.jackson.annotation.JacksonInject.Value;
import com.sunkaisens.ibss.common.annotation.Log;
import com.sunkaisens.ibss.common.controller.BaseController;
import com.sunkaisens.ibss.common.domain.QueryRequest;
import com.sunkaisens.ibss.common.exception.SysInnerException;
import com.sunkaisens.ibss.system.domain.Menu;
import com.sunkaisens.ibss.system.domain.Role;
import com.sunkaisens.ibss.system.domain.RoleMenu;
import com.sunkaisens.ibss.system.service.MenuService;
import com.sunkaisens.ibss.system.service.RoleMenuServie;
import com.sunkaisens.ibss.system.service.RoleService;
import com.wuwenze.poi.ExcelKit;

import io.swagger.annotations.ApiImplicitParam;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping("role")
public class RoleController extends BaseController {
     
    @Autowired
    private RoleService roleService;
    //xsh 2019/7/30 
    @Autowired
    private MenuService menuService;
    @Autowired
    private RoleMenuServie roleMenuServie;
    private String message;
    
    
    
    // 获取角色的list 形成表格   xsh 2019/7/23
    @GetMapping
    @RequiresPermissions("role:view")
    public Map<String, Object> roleList(QueryRequest queryRequest, Role role) {
        return getDataTable(roleService.findRoles(role, queryRequest));
    }

   
    @GetMapping("check/{roleName}")
    public boolean checkRoleName(@NotBlank(message = "{required}") @PathVariable String roleName) {
        Role result = this.roleService.findByName(roleName);
        return result == null;
    }
    
    //修改的时候默认的显示和全部菜单的显示  树形结构上的  暂时不用 即menuid  xsh 2019/7/30
    /*@GetMapping("roleMenu/{roleId}")
    public Map<String, Object> getRoleMenu(@NotBlank(message = "{required}") @PathVariable String roleId) {
    	Map<String, Object> result = new HashMap<>();
        //获取登录role全部关联的menu信息
    	List<String> ids = new ArrayList<>();
    	List<RoleMenu> list = this.roleMenuServie.getRoleMenusByRoleId(roleId);
    	//获得角色id关联的menuid
        for (RoleMenu roleMenu : list) {
		String roleMenuStr	=roleMenu.getMenuId().toString();
		ids.add(roleMenuStr);
		}
        //获取全部的菜单
        Menu menu = new Menu();
        Map<String, Object> menusNum=this.menuService.findMenus(menu);
        result.put("menuIds", ids);
        result.put("rows", menusNum);
        return  result;
        //return list.stream().map(roleMenu -> String.valueOf(roleMenu.getMenuId())).collect(Collectors.toList());
    }
    */
     //生成对应id下的全部menuID（修改的时候默认的个数）   xsh 2019/8/1
    @GetMapping("/role-menu/{roleId}")
    public List<String> getRoleMenus(@NotBlank(message = "{required}") @PathVariable String roleId) {
        List<RoleMenu> list = this.roleMenuServie.getRoleMenusByRoleId(roleId);
        //list.stream().map方法 把数据转成string的类型 xsh 2019/8/1
        return list.stream().map(roleMenu -> String.valueOf(roleMenu.getMenuId())).collect(Collectors.toList());
    }

    /**
     * xsh 2019/8/8 角色新增的修改 添加一个返回值
     * @param user
     * @return
     * @throws SysInnerException
     */
    @Log("新增角色")
    @PostMapping
    @RequiresPermissions("role:add")
    public Map<String, Object> addRole(@Valid @RequestBody Role role) throws SysInnerException {
    	 //定义一个map 向前台传状态值  state： 1成功  ;0失败
    	Map<String, Object> result = new HashMap<>();
    	try {
            this.roleService.createRole(role);
            result.put("state", 1);
            result.put("message", "添加成功");
        } catch (Exception e) {
            message = "新增角色失败";
            log.error(message, e);
            result.put("state", 0);
            result.put("message", "添加失败");
            throw new SysInnerException(message);
        }
    	return	result;
    }

    /**
     * xsh 2019/8/8 角色修改的修改 添加一个返回值
     * @param user
     * @return
     * @throws SysInnerException
     */
    @Log("修改角色")
    @PutMapping
    @RequiresPermissions("role:update")
    public Map<String, Object> updateRole(@Valid @RequestBody Role role) throws SysInnerException {
    	//定义一个map 向前台传状态值  state： 1成功  ;0失败
    	Map<String, Object> result = new HashMap<>();
    	try {
            this.roleService.updateRole(role);
            result.put("state", 1);
            result.put("message", "修改成功");
        } catch (Exception e) {
            message = "修改角色失败";
            log.error(message, e);
            result.put("state", 0);
            result.put("message", "修改失败");
            throw new SysInnerException(message);
        }
    	return result;
    }

    /**
     * xsh 2019/8/8 角色删除的修改 添加一个返回值
     * @param user
     * @return
     * @throws SysInnerException
     */
    @Log("删除角色")
    @DeleteMapping("/{roleIds}")
    @RequiresPermissions("role:delete")
    public Map<String, Object> deleteRoles(@NotBlank(message = "{required}") @PathVariable String roleIds) throws SysInnerException {
    	//定义一个map 向前台传状态值  state： 1成功  ;0失败
    	Map<String, Object> result = new HashMap<>();
    	try {
            String[] ids = roleIds.split(StringPool.COMMA);
            this.roleService.deleteRoles(ids);
            result.put("state", 1);
            result.put("message", "删除成功");
        } catch (Exception e) {
            message = "删除角色失败";
            log.error(message, e);
            result.put("state", 0);
            result.put("message", "删除失败");
            throw new SysInnerException(message);
        }
    return	result;
    }

    
    @PostMapping("excel")
    @RequiresPermissions("role:export")
    public void export(QueryRequest queryRequest, Role role, HttpServletResponse response) throws SysInnerException {
        try {
            List<Role> roles = this.roleService.findRoles(role, queryRequest).getRecords();
            ExcelKit.$Export(Role.class, response).downXlsx(roles, false);
        } catch (Exception e) {
            message = "导出Excel失败";
            log.error(message, e);
            throw new SysInnerException(message);
        }
    }
}
