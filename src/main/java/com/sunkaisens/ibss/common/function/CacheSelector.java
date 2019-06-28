package com.sunkaisens.ibss.common.function;

@FunctionalInterface
public interface CacheSelector<T> {
    T select() throws Exception;
}
