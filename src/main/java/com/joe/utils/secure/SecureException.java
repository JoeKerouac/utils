package com.joe.utils.secure;

/**
 * @author joe
 * @version 2018.05.10 15:36
 */
public class SecureException extends RuntimeException{
    public SecureException(Throwable error) {
        super(error);
    }
}
