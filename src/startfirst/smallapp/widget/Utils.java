package startfirst.smallapp.widget;

import java.util.ArrayList;
import java.util.Date;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.telephony.SmsManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import startfirst.smallapp.model.Conversation;

public class Utils {

	
	public static Animation AnimationFadeIn() {
		Animation fadeIn = new AlphaAnimation(0, 1);
		fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
		fadeIn.setDuration(1000);
		return fadeIn;
	}
	
	public static Animation AnimationFadeOut() {
		Animation fadeOut = new AlphaAnimation(1, 0);
		fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
		fadeOut.setDuration(1000);
		return fadeOut;
	}
	
	
	
	public static boolean isCheckNew(ArrayList<Conversation> src,
			ArrayList<Conversation> des) {
		if (des.size() != src.size()) {
			return true;
		}else {
			for (int i = 0; i < des.size(); i++) {
				if (!src.get(i).getBody().equals(des.get(i).getBody()) || !src.get(i).getAddress().equals(des.get(i).getAddress()) || des.get(i).getRead() != des.get(i).getRead()) {
					return true;
				}
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
