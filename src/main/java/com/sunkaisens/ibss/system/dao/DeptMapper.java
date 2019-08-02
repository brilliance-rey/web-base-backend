package com.sunkaisens.ibss.system.dao;

import java.util.Map;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sun.tools.javac.util.List;
import com.sunkaisens.ibss.system.domain.Dept;

public interface DeptMapper extends BaseMapper<Dept> {
   
	  //条件查询带子级的数据  xsh 2019/8/2 
	public  Map<String, Object> findDeptsNew(Dept dept);
	
	/**
	 *删除带子集的数据  徐胜浩  2019/8/2
	 *
	 * @param deptId deptId
	 */
	 void deleteDepts(String deptId);
}