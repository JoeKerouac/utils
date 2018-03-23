package com.joe.utils.type;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
/**
 * 泛型
 * 表示一个泛型，例如List&lt;T&gt;的T
 * @author joe
 *
 */
@Getter
@Setter
@ToString
public class GenericType extends JavaType{
	/*
	 * 该类型的父类型，当泛型为（T extends JavaType）这种形式时存在该值
	 */
	private JavaType parent;
	/*
	 * 该类型的子类型，当泛型为（T super JavaType）这种形式时存在该值
	 */
	private JavaType child;
}
