package yl.demo.pathHelper.db;

import java.util.HashSet;
import java.util.Set;

import yl.demo.pathHelper.db.model.Building;
import yl.demo.pathHelper.db.model.Corner;
import yl.demo.pathHelper.db.model.Floor;
import yl.demo.pathHelper.db.model.Model;
import yl.demo.pathHelper.db.model.Path;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

public class DBManager extends SQLiteOpenHelper {
	private static final String DB_NAME = "ap_data.db";
	private static final int DB_VERSION = 2;

	private static DBManager instance;

	private static Context context;

	private SQLiteDatabase db;
	/**
	 * ���ݿ��
	 */
	private static Model[] models = new Model[] {new Building(), new Corner(), new Floor(), new Path()};

	private DBManager(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	public static synchronized DBManager getInstance() {
		if (instance == null) {
			if (context == null)
				throw new NullPointerException(
						"context is null, please use DBManager.setContext() first.");
			instance = new DBManager(context);
		}
		return instance;
	}

	public static void setContext(Context c) {
		context = c;
	}

	public static Context getContext() {
		return context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		for (Model m : models) {
			db.execSQL(m.toCreateSQL());
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion != newVersion) {
			for (Model m : models) {
				db.execSQL(m.toDropSQL());
			}
		}
		onCreate(db);
	}

	public DBManager open() {
		if (db == null) {
			getInstance();
			db = getWritableDatabase();
		}
		return this;
	}

	public void close() {
		if (db != null) {
			db.close();
			db = null;
		}
	}

	/**
	 * �������ݣ��Զ�ʶ��־û���������ͣ�
	 * 
	 * @param entity
	 * @return ���ݵı�ʶ��
	 */
	public long save(Model entity) {
		ContentValues values = entity.toContentValues();
		return db.insert(entity.tableName(), null, values);
	}

	/**
	 * �������ݣ��Զ�ʶ��־û���������ͣ�
	 * 
	 * @param entity
	 * @return
	 */
	public void update(Model entity) {
		ContentValues values = entity.toContentValues();
		String where = entity.idColumnName() + " = ?";
		String[] whereArgs = new String[] { entity.getId() + "" };
		db.update(entity.tableName(), values, where, whereArgs);
	}

	/**
	 * ɾ�����ݣ��Զ�ʶ��־û���������ͣ�
	 * 
	 * @param entity
	 * @return
	 */
	public void delete(Model entity) {
		String where = entity.idColumnName() + " = ?";
		String[] whereArgs = new String[] { entity.getId() + "" };
		db.delete(entity.tableName(), where, whereArgs);
	}

	/**
	 * ����ָ��id�ͳ־û���������Ͳ�ѯ����
	 * 
	 * @param entity
	 * @return �־û����󣨿�ǿ��ת��Ϊָ�����ͣ�
	 */
	public Model findById(Integer id, Class<? extends Model> cls) {
		Set<Model> searchResult = findByFieldName(id, cls, "id");
		if (!searchResult.isEmpty() && searchResult.size() == 1) {
			return searchResult.iterator().next();
		} else {
			return null;
		}
	}

	/**
	 * ����ָ��field�ͳ־û���������Ͳ�ѯ����
	 * 
	 * @param entity
	 * @return �־û����󣨿�ǿ��ת��Ϊָ�����ͣ�
	 */
	public Set<Model> findByFieldName(Object value, Class<? extends Model> cls,
			String fieldName) {
		Set<Model> resultSet = new HashSet<Model>();
		Model entity = null;
		try {
			entity = cls.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		String where = entity.columnName(fieldName) + " = ?";
		String[] whereArgs = new String[] { value.toString() };
		Cursor c = db.query(entity.tableName(), null, where, whereArgs, null,
				null, null);

		if (c.moveToFirst()) {
			while (!c.isAfterLast()) {
				try {
					entity = cls.newInstance();
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
				if (!entity.setFieldsByCursor(c)) {
					entity = null;
				} else {
					resultSet.add(entity);
				}
				c.moveToNext();
			}
		}
		c.close();
		return resultSet;
	}

	// public Cursor query(Model entity) {
	// String where = entity.idColumnName() + " = ?";
	// String[] whereArgs = new String[] { entity.getId() + "" };
	// return db.query(entity.tableName(), null, where, whereArgs, null, null, null);
	// }

	/**
	 * �Զ����ѯ
	 * 
	 * @param sqb
	 *            ��ѯ�����������������ȣ�
	 * @param where
	 *            where���
	 * @param whereArgs
	 *            where���Ĳ���
	 * @return ��ѯ������α�
	 */
	public Cursor query(SQLiteQueryBuilder sqb, String where, String[] whereArgs) {
		return sqb.query(db, null, where, whereArgs, null, null, null);
	}
}
