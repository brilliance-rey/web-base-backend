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
import com.sunkaisens.ibss.common.annotation.Log;
import com.sunkaisens.ibss.common.controller.BaseController;
import com.sunkaisens.ibss.common.domain.QueryRequest;
import com.sunkaisens.ibss.common.domain.RetrueCode;
import com.sunkaisens.ibss.common.domain.SunkResponse;
import com.sunkaisens.ibss.common.exception.SysInnerException;
import com.sunkaisens.ibss.system.dao.DeptMapper;
import com.sunkaisens.ibss.system.dao.UserMapper;
import com.sunkaisens.ibss.system.domain.Dept;
import com.sunkaisens.ibss.system.domain.Role;
import com.sunkaisens.ibss.system.domain.User;
import com.sunkaisens.ibss.system.service.DeptService;
import com.wuwenze.poi.ExcelKit;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping("dept")
@Api(tags="部门管理的实现")
public class DeptController extends BaseController {

    private String message;

    @Autowired
    private DeptService deptService;
    
    @Autowired
	private UserMapper userMapper;
    //xsh 2019/8/21
    @Autowired
	private DeptMapper deptMapper;
   
    @GetMapping
    @ApiOperation(value="分页获得全部的部门信息和条件的获取部门信息")
    public Map<String, Object> deptList(QueryRequest request, Dept dept) {
        return this.deptService.findDepts(request, dept);
    }

    /**
	 *  xsh 2019/8/2 新增部门的修改  添加一个返回值
	 * @param dept
	 * @throws SysInnerException
	 */
    @Log("新增部门")
    @PostMapping
    @RequiresPermissions("dept:add")
    @ApiOperation(value="部门添加", notes="dept 部门实体类")
    public Map<String, Object> addDept(@Valid @RequestBody Dept dept) throws SysInnerException {
    	//判断部门名是否存在  存在了就不让加 让重新名命名   xsh 2019/8/21
        String deptName=dept.getDeptName();
        //获取正在使用的条数 xsh 2019/8/21
        Integer num  =this.deptMapper.selectCount(new LambdaQueryWrapper<Dept>().eq(Dept::getDeptName, deptName) );
        //如果不为空  说明有用户在使用  xsh 2019/8/21
        if (num!=0) {
        	throw new SysInnerException("部门名称已存在");
        }else {
        	try {
        		this.deptService.createDept(dept);
        		//SunkResponse 向前台传状态值  RetrueCode.OK 0成功  ;   RetrueCode.ERROR(1) 1：失败
        		return new SunkResponse().retureCode(RetrueCode.OK).message("添加成功");
        	} catch (Exception e) {
        		message = "添加失败";
        		log.error(message, e);
        		throw new SysInnerException(message);
        	}
        }
    }
    
    
    /**
	 *  xsh 2019/8/2修改部门的修改
	 * @param dept
	 * @throws SysInnerException
	 */
    @Log("修改部门")
    @PutMapping
    @RequiresPermissions("dept:update")
    @ApiOperation(value="部门修改", notes="dept 部门实体")
    public Map<String, Object> updateDept(@Valid @RequestBody Dept dept) throws SysInnerException {
    	try {
    		//判断是否更新成功
           Integer num = this.deptService.updateDept(dept);
           if (num==0) {
        	 //SunkResponse 向前台传状态值  RetrueCode.OK 0成功  ;   RetrueCode.ERROR(1) 1：失败
		     return new SunkResponse().retureCode(RetrueCode.ERROR).message("数据已被修改，请获取最新数据");
		    }
            //SunkResponse 向前台传状态值  RetrueCode.OK 0成功  ;   RetrueCode.ERROR(1) 1：失败
	         return new SunkResponse().retureCode(RetrueCode.OK).message("修改成功");
        } catch (Exception e) {
            message = "修改失败";
            log.error(message, e);
            throw new SysInnerException(message);
        }
    }
    
	/**
	 *  xsh 2019/8/2 删除部门的调整
	 * @param dept
	 * @throws SysInnerException
	 */
    @Log("删除部门")
    @DeleteMapping("deletNew/{deptIds}")
    @RequiresPermissions("dept:delete")
    @ApiOperation(value="部门删除", notes="deptId 部门ID")
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
    			 //SunkResponse 向前台传状态值  RetrueCode.OK 0成功  ;   RetrueCode.ERROR(1) 1：失败
		         return new SunkResponse().retureCode(RetrueCode.ERROR).message("有用户关联本部门，不可删除");
    		 }else {
    			 try {
    				 this.deptService.deleteDepts(ids);
    				 //SunkResponse 向前台传状态值  RetrueCode.OK 0成功  ;   RetrueCode.ERROR(1) 1：失败
    		         return new SunkResponse().retureCode(RetrueCode.OK).message("删除成功");
    			 } catch (Exception e) {
    				 message = "删除失败";
    				 log.error(message, e); 
    				 throw new SysInnerException(message);
    			 }
    		 }
    	 }else {
    		 message = "父级菜单不可删除";
			 throw new SysInnerException(message);
    	 }
    }
    
    

    // xsh 暂时保留
    /*@PostMapping("excel")
    @RequiresPermissions("dept:export")
    @ApiOperation(value="部门导出")
    public void export(Dept dept, QueryRequest request, HttpServletResponse response) throws SysInnerException {
        try {
            List<Dept> depts = this.deptService.findDepts(dept, request);
            ExcelKit.$Export(Dept.class, response).downXlsx(depts, false);
        } catch (Exception e) {
            message = "导出Excel失败";
            log.error(message, e);
            throw new SysInnerException(message);
        }
    }*/
    
    /**
     * xsh  2019/8/16 导出 部门
     * @param dept
     * @param request
     * @return
     * @throws SysInnerException
     */
    @GetMapping("excel")
    @RequiresPermissions("dept:export")
    @ApiOperation(value="部门导出")
    public List<Dept> export(Dept dept,QueryRequest request) throws SysInnerException {
     	return this.deptService.findDepts(dept, request);
    }
    
}
