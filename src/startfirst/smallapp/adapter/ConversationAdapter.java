package startfirst.smallapp.adapter;

import java.util.ArrayList;

import startfirst.smallapp.model.Conversation;
import startfirst.smallapp.basic.R;
import android.content.Context;
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
		HoverView hoverview;
		Conversation item = (Conversation)getItem(position);
		if (converView == null) {
			LayoutInflater l = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			converView = l.inflate(R.layout.item_conversation, parent, false);
			hoverview = new HoverView();
			assert hoverview != null;
			hoverview.tv = (TextView)converView.findViewById(R.id.item_conversation_name);
			hoverview.tv1 = (TextView)converView.findViewById(R.id.item_conversation_body);
			converView.setTag(hoverview);
		} else {
			hoverview = (HoverView) converView.getTag();
		}
		hoverview.tv.setText(item.getName_Display() != null?item.getName_Display():item.getAddress());
		hoverview.tv1.setText(item.getBody());
		if (item.getRead() == 0) {
			hoverview.tv.setTypeface(null, Typeface.BOLD);
			hoverview.tv1.setTypeface(null, Typeface.BOLD);
		}else {
			hoverview.tv.setTypeface(null, Typeface.NORMAL);
			hoverview.tv1.setTypeface(null, Typeface.NORMAL);
		}
		
		return converView;
	}

	class HoverView{
		TextView tv;
		TextView tv1;
	}
	

}
