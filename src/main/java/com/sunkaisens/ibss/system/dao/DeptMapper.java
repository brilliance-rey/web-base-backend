package com.sunkaisens.ibss.system.dao;

import java.util.Map;

import org.springframework.boot.autoconfigure.h2.H2ConsoleProperties.Settings;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fasterxml.jackson.annotation.JacksonInject.Value;
import com.sunkaisens.ibss.system.domain.Dept;

public interface DeptMapper extends BaseMapper<Dept> {

	/**
	 * 删除部门
	 *
	 * @param deptId deptId
	 */
	  void deleteDepts(String deptId);
	/*  //删除部门提示   xsh 2019/8/2
	public Map<Settings, Value> deleteDepts(String deptId);
	*/
}