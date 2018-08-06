package hamiguazzz.database.converter;

import hamiguazzz.database.annotation.Converter;

@FunctionalInterface
public interface StringToData<T> {
	T stringToData(String data);

	@Converter
	StringToData<?> nullConvert = data -> null;
	@Converter
	StringToData<String> noConvert = data -> data;
	@Converter
	StringToData<Double> numberConvert = Double::valueOf;
}
