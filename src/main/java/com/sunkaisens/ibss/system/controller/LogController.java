package com.sunkaisens.ibss.system.controller;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.sunkaisens.ibss.common.annotation.Log;
import com.sunkaisens.ibss.common.controller.BaseController;
import com.sunkaisens.ibss.common.domain.QueryRequest;
import com.sunkaisens.ibss.common.domain.RetrueCode;
import com.sunkaisens.ibss.common.domain.SunkResponse;
import com.sunkaisens.ibss.common.exception.SysInnerException;
import com.sunkaisens.ibss.system.domain.SysLog;
import com.sunkaisens.ibss.system.service.LogService;
import com.wuwenze.poi.ExcelKit;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Validated
@RestController
@RequestMapping("log")
@Api(tags="日志管理")
public class LogController extends BaseController {

    private String message;

    @Autowired
    private LogService logService;

    @GetMapping
    @RequiresPermissions("log:view")
    @ApiOperation(value="分页获得全部的日志信息和条件的获取日志信息")
    public Map<String, Object> logList(QueryRequest request, SysLog sysLog) {
        return getDataTable(logService.findLogs(request, sysLog));
    }
   
    /**
     * xsh 2019/8/8 日志删除的修改 添加一个返回值
     * @param user
     * @return
     * @throws SysInnerException
     */
    @Log("删除系统日志")
    @DeleteMapping("/{ids}")
    @RequiresPermissions("log:delete")
    @ApiOperation(value="删除日志信息",notes="传入 日志id")
    public Map<String, Object> deleteLogss(@NotBlank(message = "{required}") @PathVariable String ids) throws SysInnerException {
    	//定义一个map 向前台传状态值  returnCode： 0成功  ; 1失败
//    	Map<String, Object> result = new HashMap<>();
    	try {
            String[] logIds = ids.split(StringPool.COMMA);
            this.logService.deleteLogs(logIds);
            return new SunkResponse().retureCode(RetrueCode.OK).message("删除成功");
        } catch (Exception e) {
            message = "删除日志失败";
            log.error(message, e);
            throw new SysInnerException(message);
        }
    }

    @PostMapping("excel")
    @RequiresPermissions("log:export")
    @ApiOperation(value="导出日志信息")
    public void export(QueryRequest request, SysLog sysLog, HttpServletResponse response) throws SysInnerException {
        try {
            List<SysLog> sysLogs = this.logService.findLogs(request, sysLog).getRecords();
            ExcelKit.$Export(SysLog.class, response).downXlsx(sysLogs, false);
        } catch (Exception e) {
            message = "导出Excel失败";
            log.error(message, e);
            throw new SysInnerException(message);
        }
    }
}
