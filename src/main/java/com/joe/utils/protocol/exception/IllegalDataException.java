package com.joe.utils.protocol.exception;

/**
 * 非法数据
 *
 * @author joe
 */
public class IllegalDataException extends ProtocolException {
    private static final long serialVersionUID = 6845734111883020226L;

    public IllegalDataException(String data) {
        super("非法数据：" + data);
    }
}
