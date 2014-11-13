package startfirst.smallapp.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import startfirst.smallapp.basic.R;
import startfirst.smallapp.model.Contact;
import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ContanctAdapter extends ArrayAdapter<Contact> {

	private Context activity;
	private List<Contact> items;
	private List<Contact> _list;
	private int row;
	private Contact objBean;

	public ContanctAdapter(Context act, int row, List<Contact> items) {
		super(act, row, items);

		this.activity = act;
		this.row = row;
		this.items = items;
		
		_list = new ArrayList<Contact>();
		_list.addAll(items);

	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View view = convertView;
		ViewHolder holder;
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) activity
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(row, null);

			holder = new ViewHolder();
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

		if ((items == null) || ((position + 1) > items.size()))
			return view;

		objBean = items.get(position);

		holder.tvname = (TextView) view.findViewById(R.id.tvname);
		holder.tvPhoneNo = (TextView) view.findViewById(R.id.tvphone);

		if (holder.tvname != null && null != objBean.getName()
				&& objBean.getName().trim().length() > 0) {
			holder.tvname.setText(Html.fromHtml(objBean.getName()));
		}
		if (holder.tvPhoneNo != null && null != objBean.getPhoneNo()
				&& objBean.getPhoneNo().trim().length() > 0) {
			holder.tvPhoneNo.setText(Html.fromHtml(objBean.getPhoneNo()));
		}
		return view;
	}

	public class ViewHolder {
		public TextView tvname, tvPhoneNo;
	}
	
	
	
	
    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        items.clear();
        if (charText.length() == 0) {
        	items.addAll(_list);
        }
        else
        {
            for (Contact wp : _list)
            {
                if ((wp.getName().toLowerCase(Locale.getDefault()).startsWith(charText)) || wp.getPhoneNo().startsWith(charText))
                {
                	items.add(wp);
                }
            }
        }
        notifyDataSetChanged();
    }

}
