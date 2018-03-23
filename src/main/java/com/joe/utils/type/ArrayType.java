package com.joe.utils.type;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 系统数组类型（byte[]格式的数组转换而来的）
 * 
 * @author joe
 *
 */
@Getter
@Setter
@ToString
public class ArrayType extends JavaType {
	/*
	 * 数组的泛型，必须有，不能为空
	 */
	private Class<?> baseType;
}
