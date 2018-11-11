package com.joe.utils.scan;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 方法扫描
 *
 * @author joe
 */
public class MethodScanner implements Scanner<Method, MethodFilter> {
    private static final Object           lock = new Object();
    private static volatile MethodScanner methodScanner;

    private MethodScanner() {
    }

    public static MethodScanner getInstance() {
        if (methodScanner == null) {
            synchronized (lock) {
                if (methodScanner == null) {
                    methodScanner = new MethodScanner();
                }
            }
        }
        return methodScanner;
    }

    @Override
    public List<Method> scan(Object... args) {
        return null;
    }

    @Override
    public List<Method> scanByFilter(List<MethodFilter> excludeFilters, Object... args) {
        if (args == null || args.length == 0) {
            return Collections.emptyList();
        }
        List<Method> result = new ArrayList<>();
        for (Object obj : args) {
            if (obj == null) {
                continue;
            } else if (!(obj instanceof Class)) {
                obj = obj.getClass();
            }
            Class<?> clazz = (Class<?>) obj;
            Method[] methods = clazz.getMethods();
            if (excludeFilters == null || excludeFilters.isEmpty()) {
                result.addAll(Arrays.asList(methods));
            } else {
                for (Method method : methods) {
                    boolean flag = true;
                    for (MethodFilter filter : excludeFilters) {
                        if (!filter.filter(method)) {
                            flag = false;
                            break;
                        }
                    }
                    if (flag) {
                        result.add(method);
                    }
                }
            }

        }
        return result;
    }
}
