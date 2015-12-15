package com.lody.plugin.reflect;

public class NULL {
	public NULL(Class<?> cls) {
		this.clsName = cls;
	}
	public NULL(Reflect cls) {
		this.clsName = cls.get();
	}
	public Class<?> clsName;
}
