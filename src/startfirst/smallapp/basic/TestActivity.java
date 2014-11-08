package startfirst.smallapp.basic;

import java.util.ArrayList;

import startfirst.smallapp.adapter.ConversationAdapter;
import startfirst.smallapp.model.Conversation;
import startfirst.smallapp.model.SMS;
import startfirst.smallapp.basic.R;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class TestActivity extends Activity {

	
	ArrayList<Conversation> mConversations;
	
	ListView lv;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
		lv = (ListView)findViewById(R.id.listView1);
		
		final ContentResolver contentResolver = getContentResolver();
		Uri uri = Uri.parse("content://mms-sms/conversations/");
		Cursor query = contentResolver.query(uri, new String[] { "thread_id", "address", "date", "body","read" }, null, null, " _id desc");
		mConversations = new ArrayList<Conversation>();
		while (query.moveToNext()) {
			String numberPhone = query.getString(1);
			mConversations.add(new Conversation(query.getString(0), numberPhone,getContactName(this, numberPhone), query.getString(2), query.getString(3),query.getInt(4)));	
		}
		ConversationAdapter adapter = new ConversationAdapter(this, mConversations);
		lv.setAdapter(adapter);
		
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> l, View v,int pos, long id) {
				Conversation temp = (Conversation)l.getAdapter().getItem(pos);
				
				Cursor smsCur = contentResolver.query(Uri.parse("content://sms/"), new String[] { "address", "date", "body","type" },  "thread_id=" + temp.getThread_Id(), null, null);
				
				ArrayList<SMS> smsofconversation = new ArrayList<SMS>();
				while (smsCur.moveToNext()) {
					smsofconversation.add(new SMS(smsCur.getString(0), smsCur.getString(1), smsCur.getString(2), smsCur.getInt(3)));
				}
				Intent iSMS = new Intent(TestActivity.this, Test2Activity.class);
				iSMS.putExtra("DATA", smsofconversation);
				startActivity(iSMS);
			}
		});
		
		
		
		
		
	}
	
	
	public String getContactName(Context context, String phoneNumber) {
		ContentResolver cr = context.getContentResolver();
		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,Uri.encode(phoneNumber));
		Cursor cursor = cr.query(uri,new String[] { PhoneLookup.DISPLAY_NAME }, null, null, null);
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
