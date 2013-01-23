package yl.demo.pathHelper.db.model;

import yl.demo.pathHelper.db.util.Column;
import yl.demo.pathHelper.db.util.Table;
import yl.demo.pathHelper.db.util.Column.DataType;

@Table(name="building")
public class Building extends Model{	
	@Column(name="latitude",type=DataType.REAL)
	private Double latitude;

	@Column(name="longtitude",type=DataType.REAL)
	private Double longtitude;

	@Column(name="name",type=DataType.TEXT)
	private String name;

//	public static final String TABLE = "building";
//	public static final String COL_ID = "_id";
//	public static final String COL_LATITUDE = "latitude";
//	public static final String COL_LONGTITUDE = "longtitude";
//	public static final String COL_NAME = "name";
	
//	public static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "
//			+ TABLE
//			+ " ( "+COL_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "
//			+ COL_LATITUDE + " REAL, " 
//			+ COL_LONGTITUDE + " REAL, " 
//			+ COL_NAME + " TEXT " 
//			+ ");";

	public Building(){
		
	}
	
	public Building(Integer id) {
		super(id);
	}
	public Building(Integer id, Double latitude, Double longtitude, String name) {
		super(id);
		this.latitude = latitude;
		this.longtitude = longtitude;
		this.name = name;
	}
	public Double getLatitude() {
		return latitude;
	}
	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}
	public Double getLongtitude() {
		return longtitude;
	}
	public void setLongtitude(Double longtitude) {
		this.longtitude = longtitude;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
}
