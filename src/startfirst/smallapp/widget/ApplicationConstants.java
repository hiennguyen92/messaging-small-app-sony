package startfirst.smallapp.widget;

import android.content.ContentResolver;
import startfirst.smallapp.model.ConversationRepository;
import startfirst.smallapp.model.SMSRepository;

public class ApplicationConstants {

	public static String ColorText = "#ffffff";
	public static String FormatDate = "dd/MM/yyyy";
	
	private static ConversationRepository mConversationRepository;

	public static ConversationRepository getConversationRepository(ContentResolver contentResolver) {
		if (mConversationRepository == null) {
			mConversationRepository = new ConversationRepository(contentResolver);
		}
		return mConversationRepository;
	}
	
	private static SMSRepository mSMSRepository;
	
	public static SMSRepository getSMSRepository(ContentResolver contentResolver) {
		if (mSMSRepository == null) {
			mSMSRepository = new SMSRepository(contentResolver);
		}
		return mSMSRepository;
	}
	
}
