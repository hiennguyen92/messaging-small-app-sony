package startfirst.smallapp.adapter;

import java.util.ArrayList;

import startfirst.smallapp.model.Conversation;
import startfirst.smallapp.widget.ApplicationConstants;
import startfirst.smallapp.widget.ViewHolder;
import startfirst.smallapp.basic.R;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ConversationAdapter extends BaseAdapter{

	Context mContext;
	ArrayList<Conversation> mList;
	public ConversationAdapter(Context context,ArrayList<Conversation> data) {
		mList = data;
		mContext = context;
	}
	
	
	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int arg0) {
		return mList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View converView, ViewGroup parent) {
		Conversation item = (Conversation)getItem(position);
		
		if (converView == null) {
			LayoutInflater l = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			converView = l.inflate(R.layout.item_conversation, parent, false);
		}
		
		TextView tvTitle = ViewHolder.get(converView, R.id.item_conversation_name);
		TextView tvContent = ViewHolder.get(converView, R.id.item_conversation_body);
		tvTitle.setTextColor(Color.parseColor(ApplicationConstants.ColorText));
		tvContent.setTextColor(Color.parseColor(ApplicationConstants.ColorText));
		tvTitle.setText(item.getName_Display() != null?item.getName_Display():item.getAddress());
		tvContent.setText(item.getBody());
		if (item.getRead() == 0) {
			tvTitle.setTypeface(null, Typeface.BOLD);
			tvContent.setTypeface(null, Typeface.BOLD);
		}else {
			tvTitle.setTypeface(null, Typeface.NORMAL);
			tvContent.setTypeface(null, Typeface.NORMAL);
		}
		
		return converView;
	}

	

}
