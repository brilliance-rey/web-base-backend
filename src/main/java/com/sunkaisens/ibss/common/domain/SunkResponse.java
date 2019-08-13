package com.sunkaisens.ibss.common.domain;

import java.util.HashMap;

public class SunkResponse extends HashMap<String, Object> {

    private static final long serialVersionUID = -8713837118340960775L;

    public SunkResponse message(String message) {
        this.put("message", message);
        return this;
    }

    public SunkResponse data(Object data) {
        this.put("data", data);
        return this;
    }

    @Override
    public SunkResponse put(String key, Object value) {
        super.put(key, value);
        return this;
    }

	public SunkResponse retureCode(RetrueCode rc) {
		this.put("retureCode", rc.value());
		return this;
	}
}
