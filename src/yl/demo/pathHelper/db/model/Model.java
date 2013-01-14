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
	 * ���ص�ǰ�־û���������Ӧ�����ݿ����
	 * @return
	 */
	public String tableName() {
		return getClass().getAnnotation(Table.class).name();
	}

	/**
	 * ���ص�ǰ�־û�����ָ����������Ӧ���ݿ�������
	 * @param fieldName ������
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
	 * ���ص�ǰ�־û�����id��������Ӧ���ݿ�������
	 * @return
	 */
	public String idColumnName() {
		return columnName("id");
	}

	/**
	 * ���ص�ǰ�־û���������Ӧ�����ݿ��Ĵ���SQL���
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
					if (type.equals(DataType.AUTO)){ // �Զ��ж�����
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
	 * ���ص�ǰ�־û���������Ӧ�����ݿ���ɾ��SQL���
	 * @return
	 */
	public String toDropSQL() {
		StringBuilder sb = new StringBuilder(35);
		sb.append("DROP TABLE IF EXISTS").append(tableName()).append(";");
		return sb.toString();
	}

	/**
	 * ����һ��ContentValues����������ǰ�־û�������������Ҫ�־û������Ե� ����-��ֵ �ԣ������������У���
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
	 * �����α���ָʾ�ĵ�һ�����������õ�ǰ����������Ե�ֵ������α�Ϊ�ջ�û��ָʾ���ݣ���ʲô������������false�����򷵻�true;
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
				f.setAccessible(true); // ��ʱתΪpublic
				// ���ݳ�Ա���������ͣ����ӱ��ж�ȡ������ת��
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
	 * ֱ��ȡ��ǰ����ָ�����Ե�ֵ����ͨ��Get���������ӷ��ʿ��ƣ�
	 * @param field ������
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
