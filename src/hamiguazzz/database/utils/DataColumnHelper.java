package hamiguazzz.database.utils;

import hamiguazzz.database.LinkProperty;
import hamiguazzz.database.annotation.DataColumn;
import hamiguazzz.database.annotation.DataTable;
import hamiguazzz.database.converter.DataToString;
import hamiguazzz.database.converter.JsonConverter;
import hamiguazzz.database.converter.StringToData;
import hamiguazzz.database.core.DataColumnType;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@SuppressWarnings("WeakerAccess")
public class DataColumnHelper<T> {

	//region Fields
	protected LinkProperty property;
	protected Class<?> helpClass;
	protected Field keyField;
	protected Map<Field, DataColumn> annMap;
	protected Constructor<? extends T> constructor;
	protected Map<String, String> nameMap;
	protected String tableName;
	protected String baseName;
	protected Map<Field, DataToString> toMap;
	protected Map<Field, StringToData> fromMap;
	protected Map<Field, DataColumnHelper<Object>> beanMap;
	//endregion

	//region Constructor
	public DataColumnHelper(LinkProperty property, Class<? extends T> helpClass) throws ParserConfigurationException,
			SAXException,
			IOException, NoSuchMethodException {
		this.property = property;
		this.helpClass = helpClass;

		var tableAnn = helpClass.getAnnotation(DataTable.class);
		SQLXMLReader reader = new SQLXMLReader(tableAnn.xmlPath());
		this.baseName = reader.getDatabaseName();
		this.tableName = reader.getTableName(tableAnn.codeName());
		this.nameMap = reader.getColumnsNameMap(tableAnn.codeName());
		this.constructor = helpClass.getConstructor();
		intiMap();
	}


	private void intiMap() {
		this.annMap = new HashMap<>();
		var tempFields = helpClass.getDeclaredFields();
		for (Field field : tempFields) {
			if (field.getAnnotation(DataColumn.class) != null) {
				annMap.put(field, field.getAnnotation(DataColumn.class));
				if (keyField == null && field.getAnnotation(DataColumn.class).key())
					keyField = field;
			}
		}
		beanMap = new HashMap<>();
		annMap.forEach((field, dataColumn) -> {
			if (dataColumn.type() == DataColumnType.BEAN) {
				try {
					beanMap.put(field, new DataColumnHelper<>(property, field.getType()));
				} catch (ParserConfigurationException | SAXException | NoSuchMethodException | IOException e) {
					e.printStackTrace();
				}
			}
		});
		beanMap.forEach((field, objectDataColumnHelper) -> annMap.remove(field));
		annMap.keySet().forEach(field -> field.setAccessible(true));
		beanMap.keySet().forEach(field -> field.setAccessible(true));
		this.fromMap = new HashMap<>(annMap.size());
		this.toMap = new HashMap<>(annMap.size());
		this.annMap.forEach((field, ann) -> {
			switch (ann.converter()) {
				case IGNORE:
				case BEAN:
					break;
				case NULL:
					fromMap.put(field, StringToData.nullConvert);
					toMap.put(field, DataToString.nullConvert);
					break;
				case JSON:
					fromMap.put(field, JsonConverter.getFrom(field.getType()));
					toMap.put(field, JsonConverter.getTo());
					break;
				case SIMPLE:
					if (field.getType().equals(LocalDateTime.class))
						toMap.put(field, DataToString.standardDataTimeConverter);
					else if (field.getType().equals(LocalDate.class))
						toMap.put(field, DataToString.standardDataConverter);
					else toMap.put(field, DataToString.simpleConvert);
					try {
						fromMap.put(field, simpleConvert(field.getType()));
					} catch (NoSuchMethodException e) {
						e.printStackTrace();
					}
					break;
				case BIND_METHOD:
					try {
						Field fieldTo = helpClass.getDeclaredField(ann.customConverter().dataToString());
						fieldTo.setAccessible(true);
						toMap.put(field, (DataToString)
								fieldTo.get(helpClass));
						Field fieldFrom = helpClass.getDeclaredField(ann.customConverter().stringToData());
						fieldFrom.setAccessible(true);
						fromMap.put(field, (StringToData)
								fieldFrom.get(helpClass));
					} catch (IllegalAccessException | NoSuchFieldException e) {
						e.printStackTrace();
					}
					break;
			}
		});
	}

	//endregion

	//key->@notnull Obj throw when not founded
	public final T read(Object key) {
		try {
			constructor.setAccessible(true);
			var obj = constructor.newInstance();
			for (Map.Entry<Field, DataColumnHelper<Object>> entry : beanMap.entrySet()) {
				entry.getKey().set(obj, entry.getValue().read(key));
			}
			String st = String.format("SELECT * FROM `%s` WHERE `%s`='%s'", tableName,
					nameMap.get(keyField.getAnnotation(DataColumn.class).codeName()), key);
			var con = getCon();
			try (PreparedStatement statement = con.prepareStatement(st)) {
				ResultSet resultSet = statement.executeQuery();
				return get(resultSet, obj);
			} catch (SQLException | IllegalAccessException e) {
				e.printStackTrace();
			}
		} catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
			e.printStackTrace();
		}
		throw new NoSuchElementException(key + " is not founded!");
	}

	public final boolean write(T obj) {
		beanMap.forEach((field, objectDataColumnHelper) -> {
			try {
				objectDataColumnHelper.write(field.get(obj));
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		});
		String sql;
		if (isExist(obj)) sql = update(obj);
		else sql = insert(obj);
		var con = getCon();
		try (PreparedStatement statement = con.prepareStatement(sql)) {
			int i = statement.executeUpdate();
			return i > 0;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean isExist(T obj) {
		Object key = null;
		try {
			key = keyField.get(obj);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return isExistByKey(key);
	}

	public final boolean isExistByKey(Object key) {
		if (key == null) return false;
		String sql = String.format("SELECT `%s` FROM `%s` WHERE `%s`='%s'",
				nameMap.get(annMap.get(keyField).codeName()), tableName,
				nameMap.get(annMap.get(keyField).codeName()), toMap.get(keyField).dataToString(key));
		Connection con = getCon();
		try (PreparedStatement statement = con.prepareStatement(sql)) {
			ResultSet resultSet = statement.executeQuery();
			return resultSet.next();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public List<T> readAll(List keys) {
		List<T> re = new ArrayList<>();
		//noinspection unchecked
		keys.forEach(key -> re.add(read(key)));
		return re;
	}

	public List searchAll() {
		String sql = String.format("SELECT `%s` FROM `%s`",
				nameMap.get(annMap.get(keyField).codeName()), tableName);
		var con = getCon();
		try (PreparedStatement statement = con.prepareStatement(sql)) {
			ResultSet resultSet = statement.executeQuery();
			List re = new ArrayList();
			StringToData stringToData = fromMap.get(keyField);
			while (resultSet.next())
				//noinspection unchecked
				re.add(stringToData.stringToData(resultSet.getString(1)));
			return re;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return new ArrayList();
	}

	public boolean delete(T obj) {
		Object o = null;
		try {
			o = keyField.get(obj);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return deleteByKey(o);
	}

	public final boolean deleteByKey(Object key) {
		if (key == null) return false;
		String sql = String.format("DELETE FROM `%s` WHERE `%s`='%s'", tableName
				, nameMap.get(annMap.get(keyField).codeName()), toMap.get(keyField).dataToString(key));
		var con = getCon();
		try (PreparedStatement statement = con.prepareStatement(sql)) {
			int i = statement.executeUpdate();
			return i > 0;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	//region Utils
	protected String insert(T obj) {
		StringBuilder sb1 = new StringBuilder("INSERT INTO `");
		sb1.append(tableName).append("` (");
		StringBuilder sb2 = new StringBuilder(") VALUES (");
		annMap.forEach((field, dataColumn) -> {
			sb1.append("`").append(nameMap.get(dataColumn.codeName())).append("`,");
			try {
				sb2.append("'").append(toMap.get(field).dataToString(field.get(obj))).append("',");
			} catch (IllegalAccessException e) {
				//would not occur
				e.printStackTrace();
			}
		});
		if (annMap.size() > 1) {
			sb1.deleteCharAt(sb1.length() - 1);
			sb2.deleteCharAt(sb2.length() - 1);
		}
		sb2.append(")");
		sb1.append(sb2);
		return sb1.toString();
	}

	protected String update(T obj) {
		StringBuilder re = new StringBuilder("UPDATE `");
		re.append(tableName).append("` SET ");
		toMap.forEach((field, dataToString) -> {
			re.append("`").append(nameMap.get(annMap.get(field).codeName())).append("`='");
			try {
				re.append(dataToString.dataToString(field.get(obj))).append("',");
			} catch (IllegalAccessException e) {
				//would not occur
				e.printStackTrace();
			}
		});
		if (toMap.size() > 1) re.deleteCharAt(re.length() - 1);
		try {
			re.append(" WHERE `").append(nameMap.get(annMap.get(keyField).codeName())).append("`='");
			re.append(toMap.get(keyField).dataToString(keyField.get(obj))).append("'");
		} catch (IllegalAccessException e) {
			//would not occur
			e.printStackTrace();
			return null;
		}
		return re.toString();
	}

	//consume result null when empty
	protected T get(ResultSet result, T obj) throws SQLException, IllegalAccessException {
		if (result.next()) {
			for (Map.Entry<Field, DataColumn> entry : annMap.entrySet()) {
				entry.getKey().set(obj, fromMap.get(entry.getKey()).stringToData(
						result.getString(nameMap.get(entry.getValue().codeName()))
				));
			}
			return obj;
		}
		throw new SQLException("no element");
	}

	private StringToData simpleConvert(Class<?> type) throws NoSuchMethodException {
		if (type.equals(String.class)) return String::toString;
		if (type.equals(Integer.class) || type.equals(int.class)) return Integer::valueOf;
		if (type.equals(Double.class) || type.equals(double.class)) return Double::valueOf;
		if (type.equals(Long.class) || type.equals(long.class)) return Long::valueOf;
		if (type.equals(LocalDateTime.class)) return data -> LocalDateTime.parse(data, property.dateTimeFormatter);
		if (type.equals(LocalDate.class)) return data -> LocalDate.parse(data, property.dateFormatter);
		Constructor constructor = type.getConstructor(String.class);
		constructor.setAccessible(true);
		return data -> {
			try {
				return constructor.newInstance(data);
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
				e.printStackTrace();
			}
			return null;
		};
	}

	private Connection connection;

	protected final Connection getCon() {
		if (connection == null) {
			try {
				Class.forName(property.driverName);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			try {
				connection = DriverManager.getConnection(property.linkurl + baseName + property.linkSetting,
						property.userName, property.userPassword);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return connection;
	}
	//endregion
}
