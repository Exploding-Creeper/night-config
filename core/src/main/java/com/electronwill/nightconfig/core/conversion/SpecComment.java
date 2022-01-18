package com.electronwill.nightconfig.core.conversion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/***
 * This allows you to add Comments to a CommentedConfig file when using ObjectConverter
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SpecComment {
	String value();
}
