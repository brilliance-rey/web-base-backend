package com.sunkaisens.ibss.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sunkaisens.ibss.system.domain.Test;

import java.util.List;

public interface TestService extends IService<Test> {

    List<Test> findTests();

    /**
     * 批量插入
     * @param list List<Test>
     */
    void batchInsert(List<Test> list);
}
