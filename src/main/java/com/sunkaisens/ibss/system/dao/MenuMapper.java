package com.sunkaisens.ibss.system.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunkaisens.ibss.system.domain.Menu;

import java.util.List;

public interface MenuMapper extends BaseMapper<Menu> {

    List<Menu> findUserPermissions(String userName);

    List<Menu> findUserMenus(String userName);

    /**
     * 查找当前菜单/按钮关联的用户 ID
     *
     * @param menuId menuId
     * @return 用户 ID集合
     */
    List<String> findUserIdsByMenuId(String menuId);

    /**
             * 删除菜单/按钮
     *
     * @param menuId menuId
     */
    int deleteMenus(String menuId);
}