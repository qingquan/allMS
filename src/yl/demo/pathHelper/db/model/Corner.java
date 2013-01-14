package yl.demo.pathHelper.db.model;

import yl.demo.pathHelper.db.util.Column;
import yl.demo.pathHelper.db.util.Table;
import yl.demo.pathHelper.db.util.Column.DataType;

@Table(name = "corner")
public class Corner extends Model {
	@Column(name = "floor_id", type = DataType.INTEGER)
	private Integer floorId;

	@Column(name = "x", type = DataType.REAL)
	private Double x;

	@Column(name = "y", type = DataType.REAL)
	private Double y;

	public static final String TABLE = "corner";
	public static final String COL_ID = "_id";
	public static final String COL_FLOOR_ID = "floor_id";
	public static final String COL_X = "x";
	public static final String COL_Y = "y";

	public static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE
			+ " ( " + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_FLOOR_ID
			+ " INTEGER, " + COL_X + " REAL, " + COL_Y + " REAL" + ");";

	// °üº¬Íâ¼ü
	// public static final String SQL_CREATE_TABLE_FK =
	// "CREATE TABLE IF NOT EXISTS "
	// + TABLE
	// + " ( "+COL_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "
	// + COL_FLOOR_ID + " INTEGER, "
	// + COL_X + " REAL, "
	// + COL_Y + " REAL,"
	// +
	// "FOREIGN KEY("+COL_FLOOR_ID+") REFERENCES "+Floor.TABLE+"("+Floor.COL_ID+")"
	// + ");";

	public Corner() {
	}

	public Corner(Integer id, Integer floorId, Double x, Double y) {
		super(id);
		this.floorId = floorId;
		this.x = x;
		this.y = y;
	}

	public Integer getFloorId() {
		return floorId;
	}

	public void setFloorId(Integer floorId) {
		this.floorId = floorId;
	}

	public Double getX() {
		return x;
	}

	public void setX(Double x) {
		this.x = x;
	}

	public Double getY() {
		return y;
	}

	public void setY(Double y) {
		this.y = y;
	}

}
