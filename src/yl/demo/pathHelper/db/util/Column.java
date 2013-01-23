package yl.demo.pathHelper.db.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据库表的列注解，带有此注解代表该属性将被持久化（数据库表的一列）
 *
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {
	String name() default AUTO_NAMED;
	DataType type() default DataType.AUTO;
	boolean pk() default false;
	
	enum DataType {	AUTO,INTEGER,REAL,TEXT }
	String AUTO_NAMED = "##"; 
}
