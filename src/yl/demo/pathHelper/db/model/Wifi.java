package yl.demo.pathHelper.db.model;

import yl.demo.pathHelper.db.util.Column;
import yl.demo.pathHelper.db.util.Table;
import yl.demo.pathHelper.db.util.Column.DataType;

@Table(name="wifi")
public class Wifi extends Model{	
	@Column(name="floor_id",type=DataType.INTEGER)
	private Integer floorId;
	
	@Column(name="mac_address",type=DataType.TEXT)
	private String macAddress;
	
	@Column(name="ssid",type=DataType.TEXT)
	private String ssid;
	
	@Column(name="threshold",type=DataType.REAL)
	private Double threshold;
	
	@Column(name="x",type=DataType.REAL)
	private Double x;
	
	@Column(name="y",type=DataType.REAL)
	private Double y;

	public Wifi(Integer id, Integer floorId, String macAddress, String ssid,
			Double threshold, Double x, Double y) {
		super(id);
		this.floorId = floorId;
		this.macAddress = macAddress;
		this.ssid = ssid;
		this.threshold = threshold;
		this.x = x;
		this.y = y;
	}

	public Wifi() {
	}

	public Integer getFloorId() {
		return floorId;
	}

	public void setFloorId(Integer floorId) {
		this.floorId = floorId;
	}

	public String getMacAddress() {
		return macAddress;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

	public String getSsid() {
		return ssid;
	}

	public void setSsid(String ssid) {
		this.ssid = ssid;
	}

	public Double getThreshold() {
		return threshold;
	}

	public void setThreshold(Double threshold) {
		this.threshold = threshold;
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
