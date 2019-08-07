package com.sunkaisens.ibss.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sunkaisens.ibss.system.dao.UserRoleMapper;
import com.sunkaisens.ibss.system.domain.UserRole;
import com.sunkaisens.ibss.system.service.UserRoleService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service("userRoleService")
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements UserRoleService {

	@Override
	@Transactional
	public void deleteUserRolesByRoleId(String[] roleIds) {
		Arrays.stream(roleIds).forEach(id -> baseMapper.deleteByRoleId(Long.valueOf(id)));
	}

	@Override
	@Transactional
	public void deleteUserRolesByUserId(String[] userIds) {
		Arrays.stream(userIds).forEach(id -> baseMapper.deleteByUserId(Long.valueOf(id)));
	}

	
	/**
	 * 查找关联的用户  xsh  2019/8/7
	 */
	@Override
	public List<String> findUserIdsByRoleId(String[] roleIds) {
	   //遍历的到roleIds 之前没有遍历的到的roleIds
      List<UserRole> list=new ArrayList<UserRole>();
      for (String roleId : roleIds) {
    	  list = baseMapper.selectList(new LambdaQueryWrapper<UserRole>().in(UserRole::getRoleId, (Object) roleId));
	    }
      return list.stream().map(userRole -> String.valueOf(userRole.getUserId())).collect(Collectors.toList());
	}

}
