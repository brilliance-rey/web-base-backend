package com.sunkaisens.ibss.system.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.StringBuilderFormattable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sun.tools.internal.xjc.generator.bean.ImplStructureStrategy.Result;
import com.sunkaisens.ibss.common.domain.IBSSConstant;
import com.sunkaisens.ibss.common.domain.Tree;
import com.sunkaisens.ibss.common.exception.SysInnerException;
import com.sunkaisens.ibss.common.utils.TreeUtil;
import com.sunkaisens.ibss.system.dao.MenuMapper;
import com.sunkaisens.ibss.system.domain.Menu;
import com.sunkaisens.ibss.system.manager.UserManager;
import com.sunkaisens.ibss.system.service.MenuService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("menuService")
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {

	private String message;
	@Autowired
	private UserManager userManager;

	@Override
	public List<Menu> findUserPermissions(String username) {
		return this.baseMapper.findUserPermissions(username);
	}

	@Override
	public List<Menu> findUserMenus(String username) {
		return this.baseMapper.findUserMenus(username);
	}

	@Override
	public Map<String, Object> findMenus(Menu menu) {
		Map<String, Object> result = new HashMap<>();
		try {
			LambdaQueryWrapper<Menu> queryWrapper = new LambdaQueryWrapper<>();
			findMenuCondition(queryWrapper, menu);
			List<Menu> menus = baseMapper.selectList(queryWrapper);

			List<Tree<Menu>> trees = new ArrayList<>();
			List<String> ids = new ArrayList<>();
			buildTrees(trees, menus, ids);

			// result.put("ids", ids);
			if (StringUtils.equals(menu.getType(), IBSSConstant.TYPE_BUTTON)) {
				result.put("rows", trees);
			} else {
				Tree<Menu> menuTree = TreeUtil.build(trees);
				result.put("rows", menuTree);
			}

			result.put("total", menus.size());
		} catch (NumberFormatException e) {
			log.error("查询菜单失败", e);
			result.put("rows", null);
			result.put("total", 0);
		}
		return result;
	}

	@Override
	public List<Menu> findMenuList(Menu menu) {
		LambdaQueryWrapper<Menu> queryWrapper = new LambdaQueryWrapper<>();
		findMenuCondition(queryWrapper, menu);
		queryWrapper.orderByAsc(Menu::getMenuId);
		return this.baseMapper.selectList(queryWrapper);
	}

	/**
	 * xsh 2019/8/8 新增一个菜单
	 * 
	 * @throws SysInnerException
	 */
	@Override
	@Transactional
	public void createMenu(Menu menu) throws Exception {
		menu.setCreateTime(new Date());
		setMenu(menu);
		this.save(menu);
	}

	/**
	 * xsh 2019/8/8 修改一个菜单
	 */
	@Override
	@Transactional
	public void updateMenu(Menu menu) throws Exception {
		menu.setModifyTime(new Date());
		setMenu(menu);
		baseMapper.updateById(menu);
		// 查找与这些菜单/按钮关联的用户
		List<String> userIds = this.baseMapper.findUserIdsByMenuId(String.valueOf(menu.getMenuId()));
		// 重新将这些用户的角色和权限缓存到 Redis中
		this.userManager.loadUserPermissionRoleRedisCache(userIds);
	}

	/**
	 * 
	 * xsh 2019/8/6 删除
	 */
	@Override
	@Transactional
	public void deleteMeuns(String[] menuIds) throws Exception {
		Menu menu = new Menu();// 实例化一个menu
		Long parentId = null;
		for (String menuId : menuIds) {
			menu = this.baseMapper.selectById(menuId);
			parentId = menu.getParentId();
			// 先判断是不是父级菜单 如果不是可删除
			if (parentId == 0) {
				message = "删除失败";
				throw new SysInnerException(message);
			} else {
				// 删除这些菜单/按钮
				int num = this.baseMapper.deleteMenus(menuId);
				if (num == 1) {
					// 查找与这些菜单/按钮关联的用户
					List<String> userIds = this.baseMapper.findUserIdsByMenuId(String.valueOf(menuId));
					// 重新将这些用户的角色和权限缓存到 Redis中
					this.userManager.loadUserPermissionRoleRedisCache(userIds);
				}
			}
		}
	}

	private void buildTrees(List<Tree<Menu>> trees, List<Menu> menus, List<String> ids) {
		menus.forEach(menu -> {
			ids.add(menu.getMenuId().toString());
			Tree<Menu> tree = new Tree<>();
			tree.setId(menu.getMenuId().toString());
			tree.setKey(tree.getId());
			tree.setParentId(menu.getParentId().toString());
			tree.setText(menu.getMenuName());
			tree.setTitle(tree.getText());
			tree.setIcon(menu.getIcon());
			tree.setComponent(menu.getComponent());
			tree.setCreateTime(menu.getCreateTime());
			tree.setModifyTime(menu.getModifyTime());
			tree.setPath(menu.getPath());
			tree.setOrder(menu.getOrderNum());
			tree.setPermission(menu.getPerms());
			tree.setType(menu.getType());
			trees.add(tree);
		});
	}

	private void setMenu(Menu menu) {
		if (menu.getParentId() == null)
			menu.setParentId(0L);
		if (Menu.TYPE_BUTTON.equals(menu.getType())) {
			menu.setPath(null);
			menu.setIcon(null);
			menu.setComponent(null);
		}
	}

	private void findMenuCondition(LambdaQueryWrapper<Menu> queryWrapper, Menu menu) {
		if (StringUtils.isNotBlank(menu.getMenuName())) {
			queryWrapper.eq(Menu::getMenuName, menu.getMenuName());
		}
		if (StringUtils.isNotBlank(menu.getType())) {
			queryWrapper.eq(Menu::getType, menu.getType());
		}
		if (StringUtils.isNotBlank(menu.getCreateTimeFrom()) && StringUtils.isNotBlank(menu.getCreateTimeTo())) {
			queryWrapper.ge(Menu::getCreateTime, menu.getCreateTimeFrom()).le(Menu::getCreateTime,
					menu.getCreateTimeTo());
		}
	}

}
