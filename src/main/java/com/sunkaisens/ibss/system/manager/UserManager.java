package com.sunkaisens.ibss.system.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunkaisens.ibss.common.domain.router.RouterMeta;
import com.sunkaisens.ibss.common.domain.router.VueRouter;
import com.sunkaisens.ibss.common.service.CacheService;
import com.sunkaisens.ibss.common.utils.SunkUtil;
import com.sunkaisens.ibss.common.utils.TreeUtil;
import com.sunkaisens.ibss.system.domain.Menu;
import com.sunkaisens.ibss.system.domain.Role;
import com.sunkaisens.ibss.system.domain.User;
import com.sunkaisens.ibss.system.domain.UserConfig;
import com.sunkaisens.ibss.system.service.MenuService;
import com.sunkaisens.ibss.system.service.RoleService;
import com.sunkaisens.ibss.system.service.UserConfigService;
import com.sunkaisens.ibss.system.service.UserService;

/**
 * 封装一些和 User相关的业务操作
 */
@Service
public class UserManager {

    @Autowired
    private CacheService cacheService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private MenuService menuService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserConfigService userConfigService;


    /**
     * 通过用户名获取用户基本信息
     *
     * @param username 用户名
     * @return 用户基本信息
     */
    public User getUser(String username) {
        return SunkUtil.selectCacheByTemplate(
                () -> this.cacheService.getUser(username),
                () -> this.userService.findByName(username));
    }

    /**
     * 通过用户名获取用户角色集合
     *
     * @param username 用户名
     * @return 角色集合
     */
    public Set<String> getUserRoles(String username) {
        List<Role> roleList = SunkUtil.selectCacheByTemplate(
                () -> this.cacheService.getRoles(username),
                () -> this.roleService.findUserRole(username));
        return roleList.stream().map(Role::getRoleName).collect(Collectors.toSet());
    }

    /**
     * 通过用户名获取用户权限集合
     *
     * @param username 用户名
     * @return 权限集合
     */
    public Set<String> getUserPermissions(String username) {
        List<Menu> permissionList = SunkUtil.selectCacheByTemplate(
                () -> this.cacheService.getPermissions(username),
                () -> this.menuService.findUserPermissions(username));
        return permissionList.stream().map(Menu::getPerms).collect(Collectors.toSet());
    }

    /**
     * 通过用户名构建 Vue路由
     *
     * @param username 用户名
     * @return 路由集合
     */
    public ArrayList<VueRouter<Menu>> getUserRouters(String username) {
        List<VueRouter<Menu>> routes = new ArrayList<>();
        List<Menu> menus = this.menuService.findUserMenus(username);
        System.out.println("menu得个数"+menus);
        menus.forEach(menu -> {
            VueRouter<Menu> route = new VueRouter<>();
            route.setId(menu.getMenuId().toString());
            route.setParentId(menu.getParentId().toString());
            route.setIcon(menu.getIcon());
            route.setPath(menu.getPath());
            route.setComponent(menu.getComponent());
            route.setName(menu.getMenuName());
            route.setMeta(new RouterMeta(true, null));
            routes.add(route);
        });
        System.out.println(routes);
        return TreeUtil.buildVueRouter(routes);
    }
    /**
     * 通过用户 ID获取前端系统个性化配置
     *
     * @param userId 用户 ID
     * @return 前端系统个性化配置
     */
    public UserConfig getUserConfig(String userId) {
        return SunkUtil.selectCacheByTemplate(
                () -> this.cacheService.getUserConfig(userId),
                () -> this.userConfigService.findByUserId(userId));
    }

    /**
     * 将用户相关信息添加到 Redis缓存中
     *
     * @param user user
     */
    public void loadUserRedisCache(User user) throws Exception {
        // 缓存用户
        cacheService.saveUser(user.getUsername());
        // 缓存用户角色
        cacheService.saveRoles(user.getUsername());
        // 缓存用户权限
        cacheService.savePermissions(user.getUsername());
        // 缓存用户个性化配置
        cacheService.saveUserConfigs(String.valueOf(user.getUserId()));
    }

    /**
     * 将用户角色和权限添加到 Redis缓存中
     *
     * @param userIds userIds
     */
    public void loadUserPermissionRoleRedisCache(List<String> userIds) throws Exception {
    	for (String userId : userIds) {
            User user = userService.getById(userId);
            // 缓存用户角色
            cacheService.saveRoles(user.getUsername());
            // 缓存用户权限
            cacheService.savePermissions(user.getUsername());
        }
    }

    /**
     * 通过用户 id集合批量删除用户 Redis缓存
     *
     * @param userIds userIds
     */
    public void deleteUserRedisCache(String... userIds) throws Exception {
        for (String userId : userIds) {
            User user = userService.getById(userId);
            if (user != null) {
                cacheService.deleteUser(user.getUsername());
                cacheService.deleteRoles(user.getUsername());
                cacheService.deletePermissions(user.getUsername());
            }
            cacheService.deleteUserConfigs(userId);
        }
    }

}
