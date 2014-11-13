package startfirst.smallapp.model;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public class SMSRepository extends BaseRepository<SMS>{

	public SMSRepository(ContentResolver contentResolver) {
		super(contentResolver);
	}

	@Override
	public ArrayList<SMS> getAll() {
		throw new UnsupportedOperationException("Unsupported Operation getAll");
	}

	@Override
	public SMS get(String Id) {
		throw new UnsupportedOperationException("Unsupported Operation get(String Id)");
	}
	
	public ArrayList<SMS> getAllSMSConversation(String thread_id){
		ArrayList<SMS> uti = new ArrayList<SMS>();
		Cursor smsCur = mContentResolver.query(Uri.parse("content://sms/"),
				new String[] { "_id", "address", "date", "body", "type","read" }, "thread_id=" + thread_id, null, null);
		
		while (smsCur.moveToNext()) {
			uti.add(new SMS(smsCur.getString(0), smsCur.getString(1), smsCur.getString(2), smsCur.getString(3),smsCur.getInt(4),smsCur.getInt(5)));
			if (smsCur.getInt(5) == 0) {
				markMessageRead(smsCur.getString(0));
			}
		}
		smsCur.close();
		return uti;
	}
	
	public ArrayList<SMS> getAllSMSConversationPhone(String phone){
		Uri mSmsinboxQueryUri = Uri.parse("content://sms/inbox");
        Cursor cursor1 = mContentResolver.query(mSmsinboxQueryUri,new String[] { "_id", "thread_id", "address", "date","body", "type","read" }, null, null, null);
		if (cursor1.getCount() > 0) {
			if (phone.startsWith("+")) {
				phone = phone.substring(3, phone.length());
			}else {
				phone = phone.substring(1, phone.length());
			}
			while (cursor1.moveToNext()) {
				String address = cursor1.getString(2);
				
				if (address.startsWith("+")) {
					address = address.substring(3, address.length());
				}else {
					address = address.substring(1, address.length());
				}
				if (address.equalsIgnoreCase(phone)) { // put your number here
					String thread_id = cursor1.getString(1);
					return getAllSMSConversation(thread_id);
//					String id = cursor1.getString(0);
//					String date = cursor1.getString(2);
//					String body = cursor1.getString(3);
//					int type = cursor1.getInt(4);
//					int read = cursor1.getInt(5);
//					uti.add(new SMS(id, address, date, body, type, read));
				}
			}
		}
        cursor1.close();
        return new ArrayList<SMS>();
	}
	
	
	
	
	
	
	private void markMessageRead(String id) {
		ContentValues values = new ContentValues();
		values.put("read", true);
		mContentResolver.update(Uri.parse("content://sms/inbox"), values,"_id=" + id, null);
	}
	
	
	

}
