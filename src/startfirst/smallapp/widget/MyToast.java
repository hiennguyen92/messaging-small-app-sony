package startfirst.smallapp.widget;

import startfirst.smallapp.basic.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MyToast {
	Toast notify;
	TextView textview_notify;
	public MyToast(Context context, String content) {
		notify = new Toast(context);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		View view_toast = inflater.inflate(R.layout.toast_view, null);
		textview_notify = (TextView)view_toast.findViewById(R.id.toast_view_textview);
		textview_notify.setText(content);
		notify.setDuration(Toast.LENGTH_SHORT);
		notify.setView(view_toast);
	}
	
	public void Show() {
		notify.show();
	}
	public void Cancel(){
		notify.cancel();
	}
	

}
