package hamiguazzz.database.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hamiguazzz.database.Exception.ConvertFailedException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListConverter {

	public static DataToString getTo() {
		return ListConverter::listToString;
	}

	public static <A> StringToData getFrom(Class<A> types) {
		return data -> StringToList(types, data);
	}

	private static ObjectMapper mapper = new ObjectMapper();

	@SuppressWarnings("unchecked")
	private static String listToString(Object obj) {
		if (!(obj instanceof List)) return null;
		List list = (List) obj;
		Object[] array = list.toArray();
		try {
			return mapper.writeValueAsString(array);
		} catch (JsonProcessingException e) {
			throw new ConvertFailedException(e);
		}
	}

	private static <A> List StringToList(Class<A> types, String string) {
		if (string == null || string.equals("")) return new ArrayList<>();
		A array;
		try {
			array = mapper.readValue(string, types);
		} catch (IOException e) {
			throw new ConvertFailedException(e);
		}
		//noinspection ArraysAsListWithZeroOrOneArgument
		return Arrays.asList((Object[]) array);
	}

}
