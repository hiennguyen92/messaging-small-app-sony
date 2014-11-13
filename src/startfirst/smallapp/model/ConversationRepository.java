package startfirst.smallapp.model;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.PhoneLookup;

public class ConversationRepository extends BaseRepository<Conversation>{

	public ConversationRepository(ContentResolver contentResolver) {
		super(contentResolver);
	}

	@Override
	public ArrayList<Conversation> getAll() {
		ArrayList<Conversation> uti = new ArrayList<Conversation>();
		Uri uri = Uri.parse("content://mms-sms/conversations/");
		Cursor query = mContentResolver.query(uri, new String[] { "_id","thread_id", "address", "date", "body", "read" }, null, null, " _id desc");
		while (query.moveToNext()) {
			String numberPhone = query.getString(2);
			uti.add(new Conversation(query.getString(0),query.getString(1),numberPhone, getContactName(numberPhone), query.getString(3), query.getString(4), query.getInt(5)));
		}
		if (query != null && !query.isClosed()) {
			query.close();
		}
		return uti;
	}
	
	

	
	

	@Override
	public Conversation get(String Id) {
		Conversation uti = null;
		Uri uri = Uri.parse("content://mms-sms/conversations/");
		Cursor query = mContentResolver.query(uri, new String[] { "_id","thread_id", "address", "date", "body", "read" }, "_id=" + Id, null, " _id desc");
		if (query.moveToFirst()) {
			String numberPhone = query.getString(2);
			uti = new Conversation(query.getString(0),query.getString(1),numberPhone, getContactName(numberPhone), query.getString(3), query.getString(4), query.getInt(5));
		}
		if (query != null && !query.isClosed()) {
			query.close();
		}
		return uti;
	}
	
	
	private String getContactName(String phoneNumber) {
		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,Uri.encode(phoneNumber));
		Cursor cursor = mContentResolver.query(uri,new String[] { PhoneLookup.DISPLAY_NAME }, null, null, null);
		if (cursor == null) {
			return null;
		}
		String contactName = null;
		if (cursor.moveToFirst()) {
			contactName = cursor.getString(0);
		}
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		return contactName;
	}
	
	
	
	

}
