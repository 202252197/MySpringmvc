package com.lsh.lvshihao.annoation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value= {ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LshAutowired {
	String value() default "";
}
