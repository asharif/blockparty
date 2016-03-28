package com.orphanware.blockparty.test.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


public class TestUtils {

	public static void setValueOnPrivateField(Object i, String sf, Object v) throws Exception {
		Field f = i.getClass().getDeclaredField(sf);
		f.setAccessible(true);
		f.set(i, v);
	}

	public static void callPrivateMethod(Object i, String sm, Object[] args) throws Exception {

		Class<?>[] ca = new Class<?>[args.length];
		for(int index=0; index< args.length; index++) {
			ca[index] = args[index].getClass();
		}

		Method m = i.getClass().getDeclaredMethod(sm, ca);
		m.setAccessible(true);
		m.invoke(i, args);
	}
	
}
