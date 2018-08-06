package hamiguazzz.database.converter;

import hamiguazzz.database.annotation.Converter;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@FunctionalInterface
public interface DataToString {
	String dataToString(Object data);

	@Converter
	DataToString nullConvert = data -> null;
	@Converter
	DataToString simpleConvert = String::valueOf;
	@Converter
	DataToString standardDataTimeConverter = data ->
			Timestamp.valueOf((LocalDateTime) data).toString().substring(0, 19);
	@Converter
	DataToString standardDataConverter = data ->
			((LocalDate) data).toString();
}
