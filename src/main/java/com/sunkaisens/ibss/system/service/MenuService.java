package com.sunkaisens.ibss.system.service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sunkaisens.ibss.system.domain.Menu;

public interface MenuService extends IService<Menu> {

    List<Menu> findUserPermissions(String username);

    List<Menu> findUserMenus(String username);

    Map<String, Object> findMenus(Menu menu);

    List<Menu> findMenuList(Menu menu);

    void createMenu(Menu menu);

    void updateMenu(Menu menu) throws Exception;

    /**
             * 删除菜单/按钮
     *
     * @param menuIds menuIds
     */
    Map<String, Object> deleteMeuns(String[] menuIds) throws Exception;

}
