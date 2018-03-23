package com.joe.utils.scan;

/**
 * 扫描类时的异常
 * 
 * @author joe
 *
 */
public class ScannerException extends RuntimeException {
	private static final long serialVersionUID = -2914885047283866123L;

	public ScannerException(String message) {
		super(message);
	}

	public ScannerException(String message, Throwable cause) {
		super(message, cause);
	}
}
