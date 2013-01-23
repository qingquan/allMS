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
	 * 数据库表
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
	 * 插入数据（自动识别持久化对象的类型）
	 * 
	 * @param entity
	 * @return 数据的标识号
	 */
	public long save(Model entity) {
		ContentValues values = entity.toContentValues();
		return db.insert(entity.tableName(), null, values);
	}

	/**
	 * 更新数据（自动识别持久化对象的类型）
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
	 * 删除数据（自动识别持久化对象的类型）
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
	 * 根据指定id和持久化对象的类型查询数据
	 * 
	 * @param entity
	 * @return 持久化对象（可强制转型为指定类型）
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
	 * 根据指定field和持久化对象的类型查询数据
	 * 
	 * @param entity
	 * @return 持久化对象（可强制转型为指定类型）
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
	 * 自定义查询
	 * 
	 * @param sqb
	 *            查询条件（表名、列名等）
	 * @param where
	 *            where语句
	 * @param whereArgs
	 *            where语句的参数
	 * @return 查询结果的游标
	 */
	public Cursor query(SQLiteQueryBuilder sqb, String where, String[] whereArgs) {
		return sqb.query(db, null, where, whereArgs, null, null, null);
	}
}
