package com.sunkaisens.ibss.system.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.sunkaisens.ibss.common.domain.router.VueRouter;
import com.sunkaisens.ibss.common.exception.SysInnerException;
import com.sunkaisens.ibss.system.dao.MenuMapper;
import com.sunkaisens.ibss.system.domain.Menu;
import com.sunkaisens.ibss.system.manager.UserManager;
import com.sunkaisens.ibss.system.service.MenuService;
import com.wuwenze.poi.ExcelKit;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.sf.saxon.expr.Component.M;

@Slf4j
@Validated
@RestController
@RequestMapping("/menu")
@Api(tags="菜单管理的实现")
public class MenuController extends BaseController {

    private String message;

    @Autowired
    private UserManager userManager;
    @Autowired
    private MenuService menuService;

    @Autowired
    protected MenuMapper menuMapper;
    
    //得到登录用户的路由（菜单栏的路径）   徐胜浩  2019/7/23
    @GetMapping("/{username}")
    @ApiOperation(value="获得登录用户的路由", notes="username   string类型")
    public ArrayList<VueRouter<Menu>> getUserRouters(@NotBlank(message = "{required}") @PathVariable String username) {
        return this.userManager.getUserRouters(username);
    }
    
    //获取菜单表中全部的菜单  xsh  2019/8/1    
    @GetMapping
    @ApiOperation(value="获得全部的菜单信息", notes="menu 菜单实体类")
     // @RequiresPermissions("menu:view") //解决菜单管理删除之后页面无显示的问题
    public Map<String, Object> menuList(Menu menu) {
        return this.menuService.findMenus(menu);
    }
    /**
     * xsh 2019/8/8 菜单新增
     * @param menu
     * @throws Exception 
     */
    @Log("新增菜单/按钮")
    @PostMapping
    @RequiresPermissions("menu:add")
    @ApiOperation(value="菜单添加", notes="menu 菜单实体类")
    public Map<String, Object> addMenu(@Valid @RequestBody Menu menu) throws Exception {
    	 //定义一个map 向前台传状态值  state： 1成功  ;0失败
    	Map<String,Object> result = new HashMap<>();
    	//定义一个map用于获取新增后的状态
    	Map<String,Object> resultSate = new HashMap<>();
    	resultSate=this.menuService.createMenu(menu);//新增完之后获取一个状态值
    	result.put("result", resultSate); //封装map中把提示的状态值 返回到前台。。
        return result ;
    }

   /**
    * xsh 2019/8/8  菜单修改
	* @param menuIds
	* @throws Exception 
	*/
    @Log("修改菜单/按钮")
    @PutMapping
    @RequiresPermissions("menu:update")
    @ApiOperation(value="菜单的修改", notes="menu 菜单实体类")
    public Map<String, Object> updateMenu(@Valid @RequestBody Menu menu) throws Exception {
    	//定义一个map 向前台传状态值  state： 1成功  ;0失败
    	Map<String,Object> result = new HashMap<>();
    	//定义一个map用于获取修改后的状态
    	Map<String,Object> resultSate = new HashMap<>();//新增完之后获取一个状态值
    	resultSate=this.menuService.updateMenu(menu);
    	result.put("result", resultSate); //封装map中把提示的状态值 返回到前台。
       return result;
    }

    /**
     * xsh  2019/8/6菜单删除  
     * @param menuIds
     * @throws Exception 
     */
    @Log("删除菜单/按钮")
    @DeleteMapping("/{menuIds}")
    @RequiresPermissions("menu:delete")
    @ApiOperation(value="菜单的删除", notes="menuIds  string类型")
    public Map<String, Object> deleteMenus(@NotBlank(message = "{required}") @PathVariable String menuIds) throws Exception {
    	//定义一个map 向前台传状态值  state： 1成功  ;0失败
    	Map<String,Object> result = new HashMap<>();
    	//定义一个map用于获取删除后的状态
    	Map<String,Object> resultSate = new HashMap<>();
    	//获得全部的ids
		String[] ids = menuIds.split(StringPool.COMMA);
		resultSate=this.menuService.deleteMeuns(ids);
		result.put("result", resultSate);
    	return result;
    }

    @PostMapping("excel")
    @RequiresPermissions("menu:export")
    @ApiOperation(value="菜单的导出")
    public void export(Menu menu, HttpServletResponse response) throws SysInnerException {
        try {
            List<Menu> menus = this.menuService.findMenuList(menu);
            ExcelKit.$Export(Menu.class, response).downXlsx(menus, false);
        } catch (Exception e) {
            message = "导出Excel失败";
            log.error(message, e);
            throw new SysInnerException(message);
        }
    }
}
