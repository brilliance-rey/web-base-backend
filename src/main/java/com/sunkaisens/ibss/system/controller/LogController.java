package com.sunkaisens.ibss.system.controller;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.sunkaisens.ibss.common.annotation.Log;
import com.sunkaisens.ibss.common.controller.BaseController;
import com.sunkaisens.ibss.common.domain.QueryRequest;
import com.sunkaisens.ibss.common.exception.SysInnerException;
import com.sunkaisens.ibss.system.domain.SysLog;
import com.sunkaisens.ibss.system.service.LogService;
import com.wuwenze.poi.ExcelKit;
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
public class LogController extends BaseController {

    private String message;

    @Autowired
    private LogService logService;

    @GetMapping
    @RequiresPermissions("log:view")
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
    public Map<String, Object> deleteLogss(@NotBlank(message = "{required}") @PathVariable String ids) throws SysInnerException {
    	//定义一个map 向前台传状态值  state： 1成功  ;0失败
    	Map<String, Object> result = new HashMap<>();
    	try {
            String[] logIds = ids.split(StringPool.COMMA);
            this.logService.deleteLogs(logIds);
            result.put("state", 1);
            result.put("message", "删除成功");
        } catch (Exception e) {
            message = "删除日志失败";
            log.error(message, e);
            result.put("state", 0);
            result.put("message", "删除失败");
            throw new SysInnerException(message);
        }
    	return result;
    }

    @PostMapping("excel")
    @RequiresPermissions("log:export")
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
