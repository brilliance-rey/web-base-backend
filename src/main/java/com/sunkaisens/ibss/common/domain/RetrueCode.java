/**
 * Title:  RetrueCode.java
 * Package com.sunkaisens.isolated.common.domain
 * Description:    TODO
 * Date:   2019年8月11日
 * @author: RenEryan
 * @version V1.0
 * Copyright (c) 2019 www.sunkaisens.com Inc. All rights reserved.
 */
package com.sunkaisens.ibss.common.domain;

/**
 * Description:TODO
 * 
 * @author RenEryan
 * @since 2019年8月11日
 */
public enum RetrueCode {
	OK(0),
	ERROR(1);
	
	private int value;
	/**   
	 * Description: TODO <br/>
	 * @param:    
	 */
	private RetrueCode(int value) {
		this.value = value;
	}
	
	public int value() {
		return this.value;
	}
}
