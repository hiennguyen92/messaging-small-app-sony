package startfirst.smallapp.adapter;



import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import startfirst.smallapp.basic.R;
import startfirst.smallapp.model.Conversation;
import startfirst.smallapp.model.SMS;
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
		ViewHolder holder; 
		if(convertView == null)
		{
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.sms_row, parent, false);
			holder.layoutroot = (LinearLayout)convertView.findViewById(R.id.sms_row_root);
			holder.layout = (LinearLayout)convertView.findViewById(R.id.sms_row_layout);
			holder.message = (TextView) convertView.findViewById(R.id.message_text);
			holder.time = (TextView)convertView.findViewById(R.id.sms_row_time);
			convertView.setTag(holder);
		}
		else
			holder = (ViewHolder) convertView.getTag();
		
		holder.message.setText(message.getBody());
		
		LayoutParams lp = (LayoutParams) holder.layout.getLayoutParams();
		LayoutParams lptime = (LayoutParams) holder.time.getLayoutParams();
		if(message.getType() == Telephony.TextBasedSmsColumns.MESSAGE_TYPE_SENT)
		{
			holder.layoutroot.setBackgroundResource(R.drawable.round_view_white_no_border);
			lp.gravity = Gravity.RIGHT;
			holder.message.setTextColor(Color.WHITE);
		}
		else
		{
			holder.layoutroot.setBackgroundResource(R.drawable.round_view_white);
			lp.gravity = Gravity.LEFT;
			holder.message.setTextColor(Color.BLACK);
		}
		lptime.gravity = Gravity.CENTER_HORIZONTAL;
		holder.time.setLayoutParams(lptime);
//		if (!message.get_ChatTime().equals("null")) {
//			long val = Long.parseLong(message.get_ChatTime().substring(6, 19));
//			Date date = new Date(val);
//			SimpleDateFormat df2 = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
//			holder.time.setText(df2.format(date));
//		}
		long val;
		try {
			val = Long.parseLong(message.getDate());
		} catch (Exception e) {
			val = new Date(message.getDate()).getTime();
		}
		 
		Date date = new Date(val);
		SimpleDateFormat df2 = new SimpleDateFormat("HH:mm dd/MM/yyyy");
		holder.time.setText(df2.format(date));
		
		
		holder.layout.setLayoutParams(lp);
		//holder.message.setTextColor(mContext.getResources().getColor(R.color.textColor));
		
		return convertView;
	}
	private static class ViewHolder
	{
		LinearLayout layoutroot;
		LinearLayout layout;
		TextView message;
		TextView time;
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
