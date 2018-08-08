package hamiguazzz.database.utils;

import hamiguazzz.database.Exception.*;
import hamiguazzz.database.LinkProperty;
import hamiguazzz.database.annotation.DataBean;
import hamiguazzz.database.annotation.DataColumn;
import hamiguazzz.database.annotation.DataTable;
import hamiguazzz.database.converter.DataToString;
import hamiguazzz.database.converter.JsonConverter;
import hamiguazzz.database.converter.StringToData;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

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
	protected Constructor<? extends T> constructor;
	protected Map<String, String> nameMap;
	protected String tableName;
	protected String baseName;
	protected Map<Field, DataColumn> annMap;
	protected Map<Field, DataToString> toMap;
	protected Map<Field, StringToData> fromMap;
	protected Map<Field, Field> extraKeyMap;
	protected Map<Field, DataColumnHelper> extraHelperMap;
	protected Map<Field, DataBean> extraAnnMap;
	//endregion

	//region Constructor
	public DataColumnHelper(@NotNull LinkProperty property, @NotNull Class<? extends T> helpClass) throws XMLException,
			NoEmptyConstructorException, FieldBindException, NoConverterException {
		this.property = property;
		this.helpClass = helpClass;
		var tableAnn = helpClass.getAnnotation(DataTable.class);
		if (tableAnn == null)
			throw new NoTableFoundException("can't find table class where class=" + helpClass.getName());
		SQLXMLReader reader;
		try {
			reader = new SQLXMLReader(tableAnn.xmlPath());
		} catch (XMLException e) {
			throw new XMLException("can't read database xml in " + helpClass.getName() + " at " + tableAnn.xmlPath(),
					e);
		}
		this.baseName = reader.getDatabaseName();
		this.tableName = reader.getTableName(tableAnn.codeName());
		this.nameMap = reader.getColumnsNameMap(tableAnn.codeName());
		try {
			this.constructor = helpClass.getConstructor();
			constructor.setAccessible(true);
		} catch (@NotNull NoSuchMethodException | SecurityException e) {
			throw new NoEmptyConstructorException("No empty constructor found in " + helpClass.getName(), e);
		}
		intiMap();
		if (keyField == null) throw new NoKeyException("No key found in " + helpClass.getName());
	}

	private void intiMap() throws NoEmptyConstructorException, FieldBindException, NoConverterException, XMLException {
		annMap = new HashMap<>();
		extraHelperMap = new HashMap<>(0);
		extraKeyMap = new HashMap<>(0);
		extraAnnMap = new HashMap<>(0);
		var tempFields = helpClass.getDeclaredFields();
		for (Field field : tempFields) {
			if (field.getAnnotation(DataColumn.class) != null) {
				var dataColumn = field.getAnnotation(DataColumn.class);
				annMap.put(field, dataColumn);
				if (keyField == null && field.getAnnotation(DataColumn.class).key())
					keyField = field;
			} else if (field.getAnnotation(DataBean.class) != null) {
				extraAnnMap.put(field, field.getAnnotation(DataBean.class));
				try {
					extraKeyMap.put(field, helpClass.getDeclaredField(field.getAnnotation(DataBean.class).keyField()));
				} catch (NoSuchFieldException e) {
					throw new FieldBindException("can't find bind extraBean in " + helpClass.getName() + " at " + field
							.getAnnotation(DataBean.class).keyField());
				}
				var s = field.getType().getAnnotation(DataTable.class).propertyPath();
				if (s.equals("")) {
					extraHelperMap.put(field, new DataColumnHelper<>(property, field.getType()));
				} else {
					extraHelperMap.put(field, new DataColumnHelper<>(field.getType()));
				}
			}
		}
		annMap.keySet().forEach(field -> field.setAccessible(true));
		extraAnnMap.keySet().forEach(field -> field.setAccessible(true));
		extraKeyMap.values().forEach(field -> field.setAccessible(true));
		this.fromMap = new HashMap<>(annMap.size());
		this.toMap = new HashMap<>(annMap.size());
		for (Map.Entry<Field, DataColumn> entry : this.annMap.entrySet()) {
			var field = entry.getKey();
			var ann = entry.getValue();
			switch (ann.converter()) {
				case IGNORE:
					break;
				case NULL:
					fromMap.put(field, StringToData.nullConvert);
					toMap.put(field, DataToString.nullConvert);
					break;
				case JSON:
					fromMap.put(field, JsonConverter.from(field.getType()));
					toMap.put(field, JsonConverter.to());
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
						throw new NoConverterException("can't find converter for " + field.getName() + " in " + helpClass.getName());
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
					} catch (@NotNull NoSuchFieldException | IllegalAccessException e) {
						throw new FieldBindException("can't find bind converter for " + field.getName() + " in " +
								"" + helpClass.getName());
					}
					break;
			}
		}
		toMap.keySet().forEach(field -> {
			if (annMap.get(field).replace()) {
				toMap.replace(field, DataToString.replace(toMap.get(field)));
			}
		});
	}

	public DataColumnHelper(@NotNull Class<? extends T> helpClass) throws XMLException, NoEmptyConstructorException,
			FieldBindException, NoConverterException {
		this(
				helpClass.getAnnotation(DataTable.class).propertyPath().equals("") ?
						new LinkProperty() :
						LinkProperty.get(helpClass.getAnnotation(DataTable.class).propertyPath())
				, helpClass);
	}

	//endregion

	@NotNull
	public T read(@NotNull Object key) {
		try {
			var obj = constructor.newInstance();
			//readColumn
			String st = String.format("SELECT * FROM `%s` WHERE `%s`='%s'", tableName,
					nameMap.get(keyField.getAnnotation(DataColumn.class).codeName()), key);
			var con = getCon();
			try (PreparedStatement statement = con.prepareStatement(st)) {
				ResultSet resultSet = statement.executeQuery();
				obj = get(resultSet, obj);
			} catch (SQLException | IllegalAccessException e) {
				e.printStackTrace();
			}
			//readExtra
			for (Map.Entry<Field, DataColumnHelper> entry : extraHelperMap.entrySet()) {
				if (extraAnnMap.get(entry.getKey()).read()) {
					Object ek = extraKeyMap.get(entry.getKey()).get(obj);
					entry.getKey().set(obj, entry.getValue().read(ek));
				}
			}
			return obj;
		} catch (@NotNull IllegalAccessException | InstantiationException | InvocationTargetException e) {
			e.printStackTrace();
		}
		throw new NoSuchElementException(key + " is not read!");
	}

	public boolean write(@NotNull T obj) {
		//writeBean
		boolean flag = true;
		try {
			String sql;
			if (isExist(obj)) sql = update(obj);
			else sql = insert(obj);
			var con = getCon();
			try (PreparedStatement statement = con.prepareStatement(sql)) {
				int i = statement.executeUpdate();
				flag = i > 0;
			} catch (SQLException e) {
				e.printStackTrace();
			}
			if (!flag) return false;
			for (Map.Entry<Field, DataColumnHelper> entry : extraHelperMap.entrySet()) {
				if (flag && extraAnnMap.get(entry.getKey()).write()) {
					//noinspection unchecked
					flag = entry.getValue().write(entry.getKey().get(obj));
				}
			}
			return flag;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean isExist(@NotNull T obj) {
		Object key = null;
		try {
			key = keyField.get(obj);
		} catch (IllegalAccessException e) {
			//never occur
			e.printStackTrace();
		}
		assert key != null;
		return isExistByKey(key);
	}

	public final boolean isExistByKey(@NotNull Object key) {
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

	@NotNull
	public List<T> readAll(@NotNull List keys) {
		List<T> re = new ArrayList<>();
		//noinspection unchecked
		keys.forEach(key -> re.add(read(key)));
		return re;
	}

	@NotNull
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

	public boolean delete(@NotNull T obj) {
		Object o = null;
		try {
			o = keyField.get(obj);
		} catch (IllegalAccessException e) {
			//never occur
			e.printStackTrace();
		}
		assert o != null;
		return deleteByKey(o);
	}

	public final boolean deleteByKey(@NotNull Object key) {
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
	@NotNull
	@Contract(pure = true)
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

	@NotNull
	@Contract(pure = true)
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
		}
		return re.toString();
	}

	//consume result null when empty
	protected T get(@NotNull ResultSet result, T obj) throws SQLException, IllegalAccessException {
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

	@Contract(pure = true)
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
				//fixme an unexpected compiling error
				/*
				Never forget!
				When add @NotNull to this Exception,
				an assert error which lost its error code and not be recorded in bugs database will occur!
				Firstly,I think this annotation adds code like {#code assert e!=null}into class,
				but this thought was fast denied by test.
				In another word,the Exception thrown by library method(or any method) is notnull definitely,
				so no one knows why assert it notnull will be judged error.
				What a fuck error!
				*/
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
				connection = DriverManager.getConnection(property.linkUrl + baseName + property.linkSetting,
						property.userName, property.userPassword);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return connection;
	}
	//endregion
}
