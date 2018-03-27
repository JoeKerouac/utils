package com.joe.utils;

import java.lang.reflect.Method;

/**
 * @author joe
 * @version 2018.03.27 16:11
 */
public class Test {
    public static void main(String[] args) throws Exception{
        Method method = User.class.getMethod("add", int[].class);
        System.out.println((method.getGenericParameterTypes()[0].equals(int[].class)));
    }

    class User{
        public void add( int[] a){

        }
    }
}
