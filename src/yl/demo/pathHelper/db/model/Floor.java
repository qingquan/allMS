package yl.demo.pathHelper.db.model;

import yl.demo.pathHelper.db.util.Column;
import yl.demo.pathHelper.db.util.Table;
import yl.demo.pathHelper.db.util.Column.DataType;

@Table(name="floor")
public class Floor extends Model{
	@Column(name="building_id",type=DataType.INTEGER)
	private Integer buildingId;

	@Column(name="number",type=DataType.INTEGER)
	private Integer number;

//	public static final String TABLE = "floor";
//	public static final String COL_ID = "_id";
//	public static final String COL_BUILDING_ID = "building_id";
//	public static final String COL_NUMBER = "number";
//	
//	public static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "
//			+ TABLE
//			+ " ( "+COL_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "
//			+ COL_BUILDING_ID + " INTEGER, " 
//			+ COL_NUMBER + " INTEGER" 
//			+ ");";
	
//	public static final String SQL_CREATE_TABLE_FK = "CREATE TABLE IF NOT EXISTS "
//			+ TABLE
//			+ " ( "+COL_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "
//			+ COL_BUILDING_ID + " INTEGER, " 
//			+ COL_NUMBER + " INTEGER," 
//			+ "FOREIGN KEY("+COL_BUILDING_ID+") REFERENCES "+Building.TABLE+"("+Building.COL_ID+")"
//			+ ");";

	public Floor(Integer id, Integer buildingId, Integer number) {
		super(id);
		this.buildingId = buildingId;
		this.number = number;
	}

	public Floor() {
	}

	public Integer getBuildingId() {
		return buildingId;
	}

	public void setBuildingId(Integer buildingId) {
		this.buildingId = buildingId;
	}

	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
	}

	
}
