package hamiguazzz.database.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public final class JsonConverter {
	@NotNull
	private static ObjectMapper mapper = new ObjectMapper();

	@NotNull
	private static DataToString to = data -> {
		try {
			return mapper.writeValueAsString(data);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
	};

	@NotNull
	@Contract(pure = true)
	public static <T> StringToData<T> from(@NotNull Class<T> fromClass) {
		return data -> {
			try {
				return mapper.readValue(data, fromClass);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		};
	}

	@NotNull
	@Contract(pure = true)
	public static DataToString to() {
		return to;
	}
}
