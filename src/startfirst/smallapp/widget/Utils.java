package startfirst.smallapp.widget;

import java.util.ArrayList;
import java.util.Date;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.telephony.SmsManager;
import startfirst.smallapp.model.Conversation;

public class Utils {

	public static boolean isCheckNew(ArrayList<Conversation> des,
			ArrayList<Conversation> src) {
		int size = des.size() > src.size() ? src.size() : des.size();
		for (int i = 0; i < size; i++) {
			if (!src.get(i).getBody().equals(des.get(i).getBody())
					|| src.get(i).getRead() != des.get(i).getRead()) {
				return true;
			}
		}
		return false;
	}
	
	
	//********************************SEND SMS****************************************//
	public static void sendSMSMessage(ContentResolver contentResolver,String phone, String msg) {
		SmsManager smsManager = SmsManager.getDefault();
		smsManager.sendTextMessage(phone, null, msg, null, null);
		addMessageToSentIfPossible(contentResolver,phone, msg, new Date().getTime() + "");
	}
	
	private static final String TELEPHON_NUMBER_FIELD_NAME = "address";
	private static final String MESSAGE_BODY_FIELD_NAME = "body";
	private static final String DATE_MSG = "date";
	private static final Uri SENT_MSGS_CONTET_PROVIDER = Uri.parse("content://sms/sent");

	private static void addMessageToSentIfPossible(ContentResolver contentResolver,String telNumber,
			String messageBody, String time) {
		if (telNumber != null && messageBody != null && time != null) {
			addMessageToSent(contentResolver,telNumber, messageBody, time);
		}
	}

	private static void addMessageToSent(ContentResolver contentResolver,String telNumber, String messageBody, String date) {
		ContentValues sentSms = new ContentValues();
		sentSms.put(TELEPHON_NUMBER_FIELD_NAME, telNumber);
		sentSms.put(MESSAGE_BODY_FIELD_NAME, messageBody);
		sentSms.put(DATE_MSG, date);
		contentResolver.insert(SENT_MSGS_CONTET_PROVIDER, sentSms);
	}
	//**********************************************************************************//	
	
	
	
	public enum ViewType {
        ViewConversation(0),ViewSMS(1),ViewFindContact(2);
         
        private int value;
 
        ViewType(int value) { 
            this.value = value;
        }
 
        public int getValue() {
            return this.value;
        }
    }
}
