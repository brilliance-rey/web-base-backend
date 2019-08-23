package com.sunkaisens.ibss.system.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.sunkaisens.ibss.common.domain.QueryRequest;
import com.sunkaisens.ibss.system.domain.Dept;

import java.util.List;
import java.util.Map;

public interface DeptService extends IService<Dept> {

    Map<String, Object> findDepts(QueryRequest request, Dept dept);

    List<Dept> findDepts(Dept dept, QueryRequest request);

    void createDept(Dept dept);

    Integer updateDept(Dept dept);

    void deleteDepts(String[] deptIds);
    
}
