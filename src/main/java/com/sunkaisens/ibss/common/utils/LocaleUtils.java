package com.sunkaisens.ibss.common.utils;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
/**
 * xsh 国际化的工具类  2019/8/16
 */
public class LocaleUtils {

	  public static Locale getLocale() {
	        return Locale.getDefault();
	    }

	    public static Locale getLocale(String language) {
	        Locale locale = getLocale();
	        if (StringUtils.isNotBlank(language)) {
	            String[] arr = language.split("_");
	            switch (arr.length) {
	                case 1:
	                    locale = new Locale(arr[0]);
	                    break;
	                case 2:
	                    locale = new Locale(arr[0], arr[1]);
	                    break;
	                case 3:
	                    locale = new Locale(arr[0], arr[1], arr[2]);
	                    break;
	            }
	        }
	        return locale;
	    }
	
	
	
}
