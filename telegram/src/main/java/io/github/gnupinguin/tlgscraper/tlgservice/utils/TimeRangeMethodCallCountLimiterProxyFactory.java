package io.github.gnupinguin.tlgscraper.tlgservice.utils;

import java.lang.reflect.Proxy;

@SuppressWarnings("unchecked")
public class TimeRangeMethodCallCountLimiterProxyFactory {

    public static <T> T getProxy(T o, Class<? super T> baseInterface) {
        return (T) Proxy.newProxyInstance(o.getClass().getClassLoader(),
                new Class[]{ baseInterface },
                new TimeRangeMethodCallsCountLimiterInvocationHandler(o,200L, 1));
    }

}
