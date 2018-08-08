package hamiguazzz.database.converter;

import hamiguazzz.database.annotation.Converter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface StringToData<T> {
	@Contract(pure = true)
	@Nullable T stringToData(@NotNull String data);

	@Nullable
	@Converter
	StringToData<?> nullConvert = data -> null;
	@NotNull
	@Converter
	StringToData<String> noConvert = data -> data;
	@NotNull
	@Converter
	StringToData<Double> numberConvert = Double::valueOf;
}
