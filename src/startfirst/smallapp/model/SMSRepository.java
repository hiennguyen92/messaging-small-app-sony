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
	
	
	private void markMessageRead(String id) {
		ContentValues values = new ContentValues();
		values.put("read", true);
		mContentResolver.update(Uri.parse("content://sms/inbox"), values,"_id=" + id, null);
	}
	
	
	

}
