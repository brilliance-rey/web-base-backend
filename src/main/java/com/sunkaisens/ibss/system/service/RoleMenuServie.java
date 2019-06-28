package com.sunkaisens.ibss.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sunkaisens.ibss.system.domain.RoleMenu;

import java.util.List;

public interface RoleMenuServie extends IService<RoleMenu> {

    void deleteRoleMenusByRoleId(String[] roleIds);

    void deleteRoleMenusByMenuId(String[] menuIds);

    List<RoleMenu> getRoleMenusByRoleId(String roleId);
}
