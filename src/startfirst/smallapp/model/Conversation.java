package startfirst.smallapp.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Conversation implements Serializable{

	private String mThread_Id;
	private String mAddress;
	private String mName_Display;
	private String mDate;
	private String mBody;
	private int mRead;
	
	public Conversation() {
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return this.mBody;
	}
	
	public Conversation(String id, String addr, String name,String date, String body, int read) {
		this.mThread_Id = id;
		this.mAddress = addr;
		this.mName_Display = name;
		this.mDate = date;
		this.mBody = body;
		this.mRead = read;
	}
	
	
	public int getRead() {
		return mRead;
	}
	public void setRead(int isRead) {
		this.mRead = isRead;
	}
	public String getThread_Id() {
		return mThread_Id;
	}
	public void setThread_Id(String thread_Id) {
		mThread_Id = thread_Id;
	}
	public String getAddress() {
		return mAddress;
	}
	public void setAddress(String address) {
		mAddress = address;
	}
	public String getName_Display() {
		return mName_Display;
	}
	public void setName_Display(String name_Display) {
		mName_Display = name_Display;
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
	
	

}
