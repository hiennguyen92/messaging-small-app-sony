package startfirst.smallapp.adapter;



import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import startfirst.smallapp.basic.R;
import startfirst.smallapp.model.SMS;
import startfirst.smallapp.widget.ApplicationConstants;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.provider.Telephony;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;


public class AwesomeAdapter extends BaseAdapter{
	private Context mContext;
	private ArrayList<SMS> mMessages;

	public AwesomeAdapter(Context context, ArrayList<SMS> messages) {
		super();
		this.mContext = context;
		this.mMessages = messages;
	}
	@Override
	public int getCount() {
		return mMessages.size();
	}
	@Override
	public Object getItem(int position) {		
		return mMessages.get(position);
	}
	@SuppressWarnings("deprecation")
	@SuppressLint("SimpleDateFormat")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		SMS message = (SMS) this.getItem(mMessages.size()-1-position); 
		if(convertView == null)
		{
			LayoutInflater l = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = l.inflate(R.layout.sms_row, parent, false);
		}

		LinearLayout llLayoutRoot = startfirst.smallapp.widget.ViewHolder.get(convertView, R.id.sms_row_root);
		LinearLayout llLayout = startfirst.smallapp.widget.ViewHolder.get(convertView, R.id.sms_row_layout);
		TextView tvMessage = startfirst.smallapp.widget.ViewHolder.get(convertView, R.id.message_text);
		TextView tvTime = startfirst.smallapp.widget.ViewHolder.get(convertView, R.id.sms_row_time);
		
		tvMessage.setText(message.getBody());
		
		LayoutParams lp = (LayoutParams) llLayout.getLayoutParams();
		LayoutParams lptime = (LayoutParams) tvTime.getLayoutParams();
		if(message.getType() == Telephony.TextBasedSmsColumns.MESSAGE_TYPE_SENT)
		{
			llLayoutRoot.setBackgroundResource(R.drawable.round_view_white);
			lp.gravity = Gravity.RIGHT;
			tvMessage.setTextColor(Color.BLACK);
		}
		else
		{
			llLayoutRoot.setBackgroundResource(R.drawable.round_view_white_no_border);
			lp.gravity = Gravity.LEFT;
			tvMessage.setTextColor(Color.WHITE);
		}
		lptime.gravity = Gravity.CENTER_HORIZONTAL;
		tvTime.setLayoutParams(lptime);
		long val;
		try {
			val = Long.parseLong(message.getDate());
		} catch (Exception e) {
			val = new Date(message.getDate()).getTime();
		}
		Date date = new Date(val);
		SimpleDateFormat df2 = new SimpleDateFormat("HH:mm "+ApplicationConstants.FormatDate);
		tvTime.setText(df2.format(date));
		llLayout.setLayoutParams(lp);
		return convertView;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public void appendItems(ArrayList<SMS> list_chat){
		mMessages.addAll(list_chat);
		notifyDataSetChanged();
	}
	
	public void addItem(SMS input){
		mMessages.add(0, input);
		notifyDataSetChanged();
	}
	public void addItems(ArrayList<SMS> list) {
		for (int i = 0; i < list.size(); i++) {
			mMessages.add(i,list.get(i));
		}
		notifyDataSetChanged();
	}
	

}
