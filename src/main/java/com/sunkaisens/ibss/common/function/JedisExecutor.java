package com.sunkaisens.ibss.common.function;

import com.sunkaisens.ibss.common.exception.RedisConnectException;

@FunctionalInterface
public interface JedisExecutor<T, R> {
    R excute(T t) throws RedisConnectException;
}
