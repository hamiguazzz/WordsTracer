package hamiguazzz.database.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hamiguazzz.database.Exception.ConvertFailedException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListConverter {
	@Contract(pure = true)
	public static DataToString to() {
		return ListConverter::listToString;
	}

	@Contract(pure = true)
	public static <A> StringToData from(@NotNull Class<A> types) {
		return data -> StringToList(types, data);
	}

	@NotNull
	private static ObjectMapper mapper = new ObjectMapper();

	@Nullable
	@Contract(pure = true)
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

	@Contract(pure = true)
	private static <A> List StringToList(@NotNull Class<A> types, @Nullable String string) {
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
