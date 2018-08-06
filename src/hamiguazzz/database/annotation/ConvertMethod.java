package hamiguazzz.database.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConvertMethod {

	/**
	 * two method,needed if converter is {@code DataConverterType.BIND_METHOD}
	 *
	 * @return (DataToString, StringToData)
	 */
	String dataToString() default "";

	String stringToData() default "";
}
