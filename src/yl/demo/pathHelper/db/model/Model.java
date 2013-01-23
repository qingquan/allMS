package yl.demo.pathHelper.db.model;

import java.lang.reflect.Field;
import android.content.ContentValues;
import android.database.Cursor;
import yl.demo.pathHelper.db.util.Column;
import yl.demo.pathHelper.db.util.Table;
import yl.demo.pathHelper.db.util.Column.DataType;

abstract public class Model {
	@Column(name = "_id", type = DataType.INTEGER, pk = true)
	private Integer id;

	public Model() {
	}

	public Model(Integer id) {
		this.id = id;
	}

	/**
	 * 返回当前持久化对象所对应的数据库表名
	 * @return
	 */
	public String tableName() {
		return getClass().getAnnotation(Table.class).name();
	}

	/**
	 * 返回当前持久化对象指定属性所对应数据库表的列名
	 * @param fieldName 属性名
	 * @return
	 */
	public String columnName(String fieldName) {
		Field f = null;
		try {
			f = getClass().getDeclaredField(fieldName);;
		} catch (NoSuchFieldException e) {
			try {
				f = getClass().getSuperclass().getDeclaredField(fieldName);
			} catch (NoSuchFieldException e1) {
				e1.printStackTrace();
			}
		}
		String name = null;
		if (f.isAnnotationPresent(Column.class)){
			name = f.getAnnotation(Column.class).name();
			if (name.equals(Column.AUTO_NAMED)){
				name = f.getName();
			}
		}
		return name;
	}

	/**
	 * 返回当前持久化对象id变量所对应数据库表的列名
	 * @return
	 */
	public String idColumnName() {
		return columnName("id");
	}

	/**
	 * 返回当前持久化对象所对应的数据库表的创建SQL语句
	 * @return
	 */
	public String toCreateSQL() {
		StringBuilder sb = new StringBuilder(100);
		sb.append("CREATE TABLE IF NOT EXISTS ");
		sb.append(tableName()).append("(").append(idColumnName())
				.append(" INTEGER PRIMARY KEY AUTOINCREMENT");
		for (Field f : getClass().getDeclaredFields()) {
			if (f.isAnnotationPresent(Column.class)) {
				Column c = f.getAnnotation(Column.class);
				if (!c.pk()) {
					DataType type = c.type();
					if (type.equals(DataType.AUTO)){ // 自动判断类型
						type = classToDBType(f.getType());
					}
					String name = c.name();
					if (name.equals(Column.AUTO_NAMED)){
						name = f.getName();
					}
					sb.append(",").append(name).append(" ").append(type.toString());
				}
			}
		}
		sb.append(");");
		return sb.toString();
	}

	/**
	 * 返回当前持久化对象所对应的数据库表的删除SQL语句
	 * @return
	 */
	public String toDropSQL() {
		StringBuilder sb = new StringBuilder(35);
		sb.append("DROP TABLE IF EXISTS").append(tableName()).append(";");
		return sb.toString();
	}

	/**
	 * 返回一个ContentValues，它包含当前持久化对象中所有需要持久化的属性的 列名-列值 对（不包含主键列）。
	 * @return
	 */
	public ContentValues toContentValues() {
		ContentValues values = new ContentValues();
		for (Field f : getClass().getDeclaredFields()) {
			Column c = f.getAnnotation(Column.class);
			if (c != null && !c.pk()) {
				Object value = null;
				try {
					value = valueOfField(f);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (value != null) {
					String name = c.name();
					if (name.equals(Column.AUTO_NAMED)){
						name = f.getName();
					}
					values.put(name, value.toString());
				}
			}
		}
		return values;
	}

	/**
	 * 根据游标所指示的第一行数据来设置当前对象各个属性的值。如果游标为空或没有指示数据，则什么都不做，返回false；否则返回true;
	 * @param cursor
	 */
	public boolean setFieldsByCursor(Cursor cursor) {
		setId(cursor.getInt(cursor.getColumnIndex("_id")));
		for (Field f : getClass().getDeclaredFields()) {
			if (f.isAnnotationPresent(Column.class)) { 
				Column c = f.getAnnotation(Column.class);
				String fieldName = c.name();
				if (fieldName.equals(Column.AUTO_NAMED)) {
					fieldName = f.getName(); 
				}
				int index = cursor.getColumnIndex(fieldName); 
				Class<?> fieldClass = f.getType();
				f.setAccessible(true); // 临时转为public
				// 根据成员变量的类型，将从表中读取的数据转换
				try {
					if (fieldClass == Integer.class || fieldClass == int.class) {
						f.set(this, cursor.getInt(index));
					} else if (fieldClass == Long.class || fieldClass == long.class) {
						f.set(this, cursor.getLong(index));
					} else if (fieldClass == Boolean.class || fieldClass == boolean.class) {
						f.set(this, cursor.getInt(index) != 0);
					} else if (fieldClass == Double.class || fieldClass == double.class) {
						f.set(this, cursor.getDouble(index));
					} else if (fieldClass == Float.class || fieldClass == float.class) {
						f.set(this, cursor.getFloat(index));
					} else if (fieldClass == String.class) {
						f.set(this, cursor.getString(index));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}

	/**
	 * 直接取当前对象指定属性的值（不通过Get方法，无视访问控制）
	 * @param field 属性域
	 * @return
	 * @throws Exception
	 */
	private Object valueOfField(Field field) throws Exception {
		field.setAccessible(true);
		return field.get(this);
	}

//	public Object valueOfField(String fieldName) {
//		Method getMethod;
//		try {
//			getMethod = this.getClass().getDeclaredMethod("get" + firstLetterToUpper(fieldName));
//			return getMethod.invoke(this);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return null;
//	}

//	private static String firstLetterToUpper(String string) {
//		char[] buffer = string.toCharArray();
//		buffer[0] = Character.toUpperCase(string.charAt(0));
//		return new String(buffer);
//	}

	private static DataType classToDBType(Class<?> cls) {
		if (cls == String.class || cls == Character.class || cls == char.class) {
			return DataType.TEXT;
		} else if (cls == Integer.class || cls == Long.class || cls == int.class
				|| cls == long.class || cls == Short.class || cls == Byte.class
				|| cls == short.class || cls == byte.class) {
			return DataType.INTEGER;
		} else if (cls == Double.class || cls == Float.class || cls == double.class
				|| cls == float.class) {
			return DataType.REAL;
		} else {
			throw new IllegalArgumentException("Unknown type!");
		}
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
}
