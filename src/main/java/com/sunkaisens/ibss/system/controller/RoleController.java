package com.sunkaisens.ibss.system.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

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
import com.sunkaisens.ibss.system.domain.Role;
import com.sunkaisens.ibss.system.domain.RoleMenu;
import com.sunkaisens.ibss.system.service.MenuService;
import com.sunkaisens.ibss.system.service.RoleMenuServie;
import com.sunkaisens.ibss.system.service.RoleService;
import com.wuwenze.poi.ExcelKit;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping("role")
@Api(tags="角色管理")
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
    @ApiOperation(value="获得全部的角色信息和单个信息")
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
    @ApiOperation(value="获得单个角色下的全部的id",notes="传入roleId")
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
    @ApiOperation(value="新增角色",notes="传入Role实体类")
    public Map<String, Object> addRole(@Valid @RequestBody Role role) throws SysInnerException {
    	try {
            this.roleService.createRole(role);
            //SunkResponse 向前台传状态值  RetrueCode.OK 0成功  ;   RetrueCode.ERROR(1) 1：失败
            return new SunkResponse().retureCode(RetrueCode.OK).message("添加成功");
        } catch (Exception e) {
            message = "添加失败";
            log.error(message, e);
            throw new SysInnerException(message);
        }
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
    @ApiOperation(value="修改角色",notes="传入Role实体类")
    public Map<String, Object> updateRole(@Valid @RequestBody Role role) throws SysInnerException {
    	try {
            this.roleService.updateRole(role);
            //SunkResponse 向前台传状态值  RetrueCode.OK 0成功  ;   RetrueCode.ERROR(1) 1：失败
            return new SunkResponse().retureCode(RetrueCode.OK).message("修改成功");
        } catch (Exception e) {
            message = "修改角色失败";
            log.error(message, e);
            throw new SysInnerException(message);
        }
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
    @ApiOperation(value="删除角色",notes="传入roleId")
    public Map<String, Object> deleteRoles(@NotBlank(message = "{required}") @PathVariable String roleIds) throws SysInnerException {
    	try {
            String[] ids = roleIds.split(StringPool.COMMA);
            this.roleService.deleteRoles(ids);
           //SunkResponse 向前台传状态值  RetrueCode.OK 0成功  ;   RetrueCode.ERROR(1) 1：失败
            return new SunkResponse().retureCode(RetrueCode.OK).message("修改成功");
        } catch (Exception e) {
            message = "删除角色失败";
            log.error(message, e);
            throw new SysInnerException(message);
        }
    }

    
    /**
     * xsh 2019/8/19 后期用了再修改
     */
    /*@PostMapping("excel")
    @RequiresPermissions("role:export")
    @ApiOperation(value="导出角色")
    public void export(QueryRequest queryRequest, Role role, HttpServletResponse response) throws SysInnerException {
        try {
            List<Role> roles = this.roleService.findRoles(role, queryRequest).getRecords();
            ExcelKit.$Export(Role.class, response).downXlsx(roles, false);
        } catch (Exception e) {
            message = "导出Excel失败";
            log.error(message, e);
            throw new SysInnerException(message);
        }
    }*/
}
