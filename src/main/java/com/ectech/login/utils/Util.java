package com.ectech.login.utils;

public class Util {
	public static boolean isEmpty(String value) {
		if(value == null) {
			return true;
		}
		if( value.trim().length() == 0) {
			return true;
		}
		return false;
	}
}
