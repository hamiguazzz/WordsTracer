package hamiguazzz.database.annotation;

import hamiguazzz.database.converter.DataConverterType;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataColumn {
	@NotNull String codeName();

	@NotNull DataColumnType type() default DataColumnType.VARCHAR;

	@NotNull DataConverterType converter() default DataConverterType.SIMPLE;

	boolean replace() default true;

	@NotNull ConvertMethod customConverter() default @ConvertMethod;

	boolean key() default false;
}
