package com.joe.utils.object;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2019年02月18日 13:46 JoeKerouac Exp $
 */
public class CustomObjectImpl<T> implements CustomObject<T> {

    /**
     * 包装的对象，可以为null
     */
    private T object;

    public CustomObjectImpl(T object) {
        this.object = object;
    }

    @Override
    public boolean in(T... args) {
        if (args == null) {
            return false;
        }
        return Stream.of(args).filter(arg -> Objects.equals(arg, object)).limit(1).count() > 0;
    }
}
