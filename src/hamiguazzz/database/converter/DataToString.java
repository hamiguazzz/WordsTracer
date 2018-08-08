package hamiguazzz.database.converter;

import hamiguazzz.database.annotation.Converter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@FunctionalInterface
public interface DataToString {
	@Contract(pure = true)
	@Nullable String dataToString(@NotNull Object data);

	@Nullable
	@Converter
	DataToString nullConvert = data -> null;
	@NotNull
	@Converter
	DataToString simpleConvert = String::valueOf;
	@NotNull
	@Converter
	DataToString replaceConvert = data -> ((String) data).replace("'", "''");
	@NotNull
	@Converter
	DataToString standardDataTimeConverter = data ->
			Timestamp.valueOf((LocalDateTime) data).toString().substring(0, 19);
	@NotNull
	@Converter
	DataToString standardDataConverter = data ->
			((LocalDate) data).toString();

	@NotNull
	static DataToString replace(@NotNull DataToString m) {
		return data -> replaceConvert.dataToString(Objects.requireNonNull(m.dataToString(data)));
	}

}
