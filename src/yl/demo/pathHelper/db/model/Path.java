package yl.demo.pathHelper.db.model;

import yl.demo.pathHelper.db.util.Column;
import yl.demo.pathHelper.db.util.Table;
import yl.demo.pathHelper.db.util.Column.DataType;

@Table(name = "path")
public class Path extends Model {
	@Column(name = "corner_id_from", type = DataType.INTEGER)
	private Integer cornerIdFrom;
	
	@Column(name = "corner_id_to", type = DataType.INTEGER)
	private Integer cornerIdTo;
	
	@Column(name = "length", type = DataType.REAL)
	private Double length;
	
	@Column(name = "floor_id", type = DataType.INTEGER)
	private Integer floorId;

	// public static final String TABLE = "path";
	// public static final String COL_ID = "_id";
	// public static final String COL_CORNER_ID_FROM = "corner_id_from";
	// public static final String COL_CORNER_ID_TO = "corner_id_to";
	//
	// public static final String SQL_CREATE_TABLE =
	// "CREATE TABLE IF NOT EXISTS "
	// + TABLE + " ( " + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
	// + COL_CORNER_ID_FROM + " INTEGER, " + COL_CORNER_ID_TO + " INTEGER"
	// + ");";

	// public static final String SQL_CREATE_TABLE_FK =
	// "CREATE TABLE IF NOT EXISTS "
	// + TABLE
	// + " ( "+COL_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "
	// + COL_CORNER_ID_FROM + " INTEGER, "
	// + COL_CORNER_ID_TO + " INTEGER,"
	// +
	// "FOREIGN KEY("+COL_CORNER_ID_FROM+") REFERENCES "+Corner.TABLE+"("+Corner.COL_ID+"),"
	// +
	// "FOREIGN KEY("+COL_CORNER_ID_TO+") REFERENCES "+Corner.TABLE+"("+Corner.COL_ID+")"
	// + ");";

	public Path() {
	}

	public Path(Integer id, Integer cornerIdFrom, Integer cornerIdTo) {
		super(id);
		this.cornerIdFrom = cornerIdFrom;
		this.cornerIdTo = cornerIdTo;
	}

	public Integer getCornerIdFrom() {
		return cornerIdFrom;
	}

	public void setCornerIdFrom(Integer cornerIdFrom) {
		this.cornerIdFrom = cornerIdFrom;
	}

	public Integer getCornerIdTo() {
		return cornerIdTo;
	}

	public void setCornerIdTo(Integer cornerIdTo) {
		this.cornerIdTo = cornerIdTo;
	}
	
	public void setLength(Double length) {
		this.length = length;
	}
	
	public Double getLength() {
		return length;
	}
	
	public void setFloorId(Integer floorId) {
		this.floorId = floorId;
	}
	
	public Integer getFloorId() {
		return floorId;
	}

}
