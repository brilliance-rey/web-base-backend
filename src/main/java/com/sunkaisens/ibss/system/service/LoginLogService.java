package com.sunkaisens.ibss.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sunkaisens.ibss.system.domain.LoginLog;

public interface LoginLogService extends IService<LoginLog> {

    void saveLoginLog (LoginLog loginLog);
}
