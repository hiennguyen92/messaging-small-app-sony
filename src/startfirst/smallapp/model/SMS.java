package startfirst.smallapp.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SMS implements Serializable{
	//"address", "date", "body","type"
	private String mAddress;
	private String mDate;
	private String mBody;
	private int mType;
	
	public SMS() {
	}
	
	public SMS(String addr, String date, String body, int type) {
		this.mAddress = addr;
		this.mDate = date;
		this.mBody = body;
		this.mType = type;
	}
	
	public String getAddress() {
		return mAddress;
	}
	public void setAddress(String address) {
		mAddress = address;
	}
	public String getDate() {
		return mDate;
	}
	public void setDate(String date) {
		mDate = date;
	}
	public String getBody() {
		return mBody;
	}
	public void setBody(String body) {
		mBody = body;
	}
	public int getType() {
		return mType;
	}
	public void setType(int type) {
		mType = type;
	}
}
