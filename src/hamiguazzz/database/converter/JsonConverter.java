package hamiguazzz.database.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public final class JsonConverter {
	private static ObjectMapper mapper = new ObjectMapper();

	private static DataToString to = data -> {
		try {
			return mapper.writeValueAsString(data);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
	};

	public static <T> StringToData<T> getFrom(Class<T> fromClass) {
		return data -> {
			try {
				return mapper.readValue(data, fromClass);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		};
	}

	public static DataToString getTo() {
		return to;
	}
}
