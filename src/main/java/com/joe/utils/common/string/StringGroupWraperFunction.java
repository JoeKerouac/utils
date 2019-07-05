package com.joe.utils.common.string;

import java.util.List;

/**
 * 字符串组函数包装
 *
 * @author JoeKerouac
 * @version 2019年07月05日 16:58
 */
public interface StringGroupWraperFunction<R> extends StringGroupFunction<R> {
    @Override
    default R apply(List<String> strings) {
        return apply(strings.get(0));
    }

    R apply(String arg);
}
