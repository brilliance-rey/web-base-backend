package com.sunkaisens.ibss.common.domain;

import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;

import com.sunkaisens.ibss.common.utils.LocaleUtils;

public enum RetureMeg {
	SUCCESS("state.success"), 
    FAIL("state.fail");

    private String msg;

    private RetureMeg(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return getMsg(null);
    }

    public String getMsg(String language) {
        return getMsg(language, null);
    }

    /**
     * @param language  语言_区域信息
     * @param basename  国际化配置文件路径/名称
     */
    public String getMsg(String language, String basename) {
        Locale locale = LocaleUtils.getLocale(language);
        basename = StringUtils.isBlank(basename) ? "i18n/msg" : basename;
        ResourceBundle bundle = ResourceBundle.getBundle(basename, locale);
        return bundle.getString(this.msg);
    }
}
