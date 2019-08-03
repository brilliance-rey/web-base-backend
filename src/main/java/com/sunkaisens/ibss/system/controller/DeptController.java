package com.sunkaisens.ibss.system.controller;


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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.fasterxml.jackson.annotation.JacksonInject.Value;
import com.sunkaisens.ibss.common.annotation.Log;
import com.sunkaisens.ibss.common.controller.BaseController;
import com.sunkaisens.ibss.common.domain.QueryRequest;
import com.sunkaisens.ibss.common.exception.SysInnerException;
import com.sunkaisens.ibss.system.dao.DeptMapper;
import com.sunkaisens.ibss.system.dao.UserMapper;
import com.sunkaisens.ibss.system.domain.Dept;
import com.sunkaisens.ibss.system.domain.User;
import com.sunkaisens.ibss.system.service.DeptService;
import com.wuwenze.poi.ExcelKit;

import lombok.extern.slf4j.Slf4j;
import springfox.documentation.swagger.web.SwaggerApiListingReader;

@Slf4j
@Validated
@RestController
@RequestMapping("dept")
public class DeptController extends BaseController {

    private String message;

    @Autowired
    private DeptService deptService;
    
    @Autowired
	private UserMapper userMapper;
    @Autowired
	private DeptMapper deptMapper;
    
    @GetMapping
    public Map<String, Object> deptList(QueryRequest request, Dept dept) {
        return this.deptService.findDepts(request, dept);
    }

    @Log("新增部门")
    @PostMapping
    @RequiresPermissions("dept:add")
    public void addDept(@Valid @RequestBody Dept dept) throws SysInnerException {
        try {
        	System.out.println();
            this.deptService.createDept(dept);
        } catch (Exception e) {
            message = "新增部门失败";
            log.error(message, e);
            throw new SysInnerException(message);
        }
    }
	/**
	 *  xsh 2019/8/2
	 * @param dept
	 * @throws SysInnerException
	 */
    @Log("删除部门")
    @DeleteMapping("deletNew/{deptIds}")
    @RequiresPermissions("dept:delete")
    public Map<String, Object> deleteDeptsNew(@NotBlank(message = "{required}") @PathVariable String deptIds) throws SysInnerException {
    	 Map<String,Object> result = new HashMap<>();
    	 Dept dept = new Dept(); //实例化一个部门
    	 String[] ids = deptIds.split(StringPool.COMMA);
    	 Integer  count   =null;
    	 Long parentId=null;
    	  //遍历获得用户部门id
    	 for (String string : ids) {
    		  Long deptId =Long.valueOf(string);
    		  dept =this.deptMapper.selectById(deptId);
    		  parentId =dept.getParentId();
    		  //通过部门id 获得在用的条数
    		  count=this.userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getDeptId, deptId));   		 
    	 }
    	 //先判断是不是父级菜单 如果不是可删除
    	 if (parentId!=0) {
    		 //在判断 如果有用户再用部门不让删 否则可以删除
    		 if (count!=0) {
    			 result.put("state",2);
    			 result.put("message", "有用户关联本部门，不可删除");
    		 }else {
    			 try {
    				 this.deptService.deleteDepts(ids);
    				 result.put("state", 1);
    				 result.put("message", "删除成功");
    			 } catch (Exception e) {
    				 message = "删除部门失败";
    				 log.error(message, e); 
    				 result.put("state", 3);
    				 result.put("message", "删除部门失败");
    				 throw new SysInnerException(message);
    			 }
    		 }
    	 }else {
    		 result.put("state", 0);
    		 result.put("message", "父级菜单不可删除");
    	 }
    	return result;
    }
    
    @Log("修改部门")
    @PutMapping
    @RequiresPermissions("dept:update")
    public void updateDept(@Valid @RequestBody Dept dept) throws SysInnerException {
        try {
            this.deptService.updateDept(dept);
        } catch (Exception e) {
            message = "修改部门失败";
            log.error(message, e);
            throw new SysInnerException(message);
        }
    }

    @PostMapping("excel")
    @RequiresPermissions("dept:export")
    public void export(Dept dept, QueryRequest request, HttpServletResponse response) throws SysInnerException {
        try {
            List<Dept> depts = this.deptService.findDepts(dept, request);
            ExcelKit.$Export(Dept.class, response).downXlsx(depts, false);
        } catch (Exception e) {
            message = "导出Excel失败";
            log.error(message, e);
            throw new SysInnerException(message);
        }
    }
}
