package hamiguazzz.database.annotation;

import hamiguazzz.database.converter.DataConverterType;
import hamiguazzz.database.core.DataColumnType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataColumn {
	String codeName();

	DataColumnType type() default DataColumnType.VARCHAR;

	DataConverterType converter() default DataConverterType.SIMPLE;

	ConvertMethod customConverter() default @ConvertMethod;

	boolean key() default false;
}
