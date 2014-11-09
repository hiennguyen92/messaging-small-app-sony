/*
 * Copyright 2011, 2012 Sony Corporation
 * Copyright (C) 2012-2013 Sony Mobile Communications AB.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the names of the Sony Corporation / the Sony Mobile
 *       Communications AB nor the names of their contributors may be used
 *       to endorse or promote products derived from this software without
 *       specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package startfirst.smallapp.basic;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import startfirst.smallapp.adapter.AwesomeAdapter;
import startfirst.smallapp.adapter.ConversationAdapter;
import startfirst.smallapp.model.Conversation;
import startfirst.smallapp.model.SMS;
import startfirst.smallapp.widget.MyToast;
import startfirst.smallapp.basic.R;

import com.learnNcode.android.Clock;
import com.learnNcode.android.ExtendedListView;
import com.learnNcode.android.ExtendedListView.OnPositionChangedListener;
import com.sony.smallapp.SdkInfo;
import com.sony.smallapp.SmallAppWindow;
import com.sony.smallapp.SmallAppWindow.WindowState;
import com.sony.smallapp.SmallApplication;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

public class MainSmallApplication extends SmallApplication implements
		OnPositionChangedListener {

	ContentResolver contentResolver;
	ArrayList<Conversation> mConversations;
	ConversationAdapter mAdapter;
	
	ExtendedListView lvConversation;

	@Override
    public void onCreate() {
        super.onCreate();
        setAppTheme(com.sony.smallapp.R.style.Theme.Dark);
        registerReceiver(IncomingSms, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
        setMinimizedView(R.layout.minimized);
        
        
        
        SmallAppWindow.Attributes attr = getWindow().getAttributes();
        attr.width = getResources().getDimensionPixelSize(R.dimen.width);
        attr.height = getResources().getDimensionPixelSize(R.dimen.height);
        attr.flags |= SmallAppWindow.Attributes.FLAG_RESIZABLE;
        getWindow().setAttributes(attr);
        setupOptionMenu();
        
        contentResolver = getContentResolver();
		setContentViewConversations();
		
		
        
        
    }

	private void setContentViewSMS(final Conversation input) {
		optionBack.setVisibility(View.VISIBLE);
		setContentView(R.layout.chat);
		setTitle(input.getName_Display() != null ? input.getName_Display()
				: input.getAddress());
		ListView lv = (ListView) findViewById(R.id.ChatListView);
		Cursor smsCur = contentResolver.query(Uri.parse("content://sms/"),
				new String[] { "address", "date", "body", "type" },
				"thread_id=" + input.getThread_Id(), null, null);
		final ArrayList<SMS> smsofconversation = new ArrayList<SMS>();
		while (smsCur.moveToNext()) {
			smsofconversation.add(new SMS(smsCur.getString(0), smsCur
					.getString(1), smsCur.getString(2), smsCur.getInt(3)));
		}
		final AwesomeAdapter adapter = new AwesomeAdapter(this, smsofconversation);
		lv.setAdapter(adapter);
		final TextView btnSend = (TextView) findViewById(R.id.btnSend);
		btnSend.setEnabled(false);
		final EditText editContent = (EditText) findViewById(R.id.editTextContent);
		editContent.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (s.length()>0) {
					btnSend.setEnabled(true);
				}else {
					btnSend.setEnabled(false);
				}
			}
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,int arg3) {}
			@Override
			public void afterTextChanged(Editable arg0) {}
		});
		
		btnSend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				btnSend.setEnabled(false);
				try {
					sendSMSMessage(input.getAddress(), editContent.getText().toString());
					editContent.setText("");
					smsofconversation.clear();
					Cursor smsCur = contentResolver.query(Uri.parse("content://sms/"),
							new String[] { "address", "date", "body", "type" },
							"thread_id=" + input.getThread_Id(), null, null);
					while (smsCur.moveToNext()) {
						smsofconversation.add(new SMS(smsCur.getString(0), smsCur
								.getString(1), smsCur.getString(2), smsCur.getInt(3)));
					}
					adapter.notifyDataSetChanged();
				} catch (Exception e) {
					e.printStackTrace();
					new MyToast(getApplicationContext(), "SMS faild, please try again.").Show();
				}
				
			}
		});
	}

	private void sendSMSMessage(String phone, String msg) {
			SmsManager smsManager = SmsManager.getDefault();
			smsManager.sendTextMessage(phone, null, msg, null, null);
			new MyToast(getApplicationContext(), "SMS sent...").Show();
			addMessageToSentIfPossible(phone, msg, new Date().getTime() + "");
	}

	private void setContentViewConversations() {
		setTitle(R.string.app_name);
		optionBack.setVisibility(View.GONE);
		setContentView(R.layout.main);
		lvConversation = (ExtendedListView) findViewById(R.id.MainListView);
		lvConversation.setCacheColorHint(Color.TRANSPARENT);
		lvConversation.setOnPositionChangedListener(this);
		lvConversation.setOnItemClickListener(ConversationClick);
		if (mConversations != null) {
			lvConversation.setAdapter(mAdapter);
		}
		new AsyntaskMsg().execute(contentResolver);
	}
	
	class AsyntaskMsg extends AsyncTask<ContentResolver, Void, ArrayList<Conversation>>{
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
		}
		
		@Override
		protected ArrayList<Conversation> doInBackground(ContentResolver... param) {
			ArrayList<Conversation> uti = new ArrayList<Conversation>();
			
			Uri uri = Uri.parse("content://mms-sms/conversations/");
			Cursor query = param[0].query(uri, new String[] {"thread_id", "address", "date", "body", "read" }, null, null, " _id desc");
			while (query.moveToNext()) {
				String numberPhone = query.getString(1);
				uti.add(new Conversation(query.getString(0),numberPhone, getContactName(param[0], numberPhone), query.getString(2), query.getString(3), query.getInt(4)));
			}
			return uti;
		}
		
		@Override
		protected void onPostExecute(ArrayList<Conversation> result) {
			super.onPostExecute(result);
			if (mConversations == null || isCheckNew(mConversations, result)) {
				mConversations = result;
				mAdapter = new ConversationAdapter(MainSmallApplication.this, mConversations);
				lvConversation.setAdapter(mAdapter);
			}

		}

		private boolean isCheckNew(ArrayList<Conversation> des, ArrayList<Conversation> src){
			int size = des.size()>src.size()?src.size():des.size();
			for (int i = 0; i < size; i++) {
				if (!src.get(i).getBody().equals(des.get(i).getBody()) || src.get(i).getRead() != des.get(i).getRead()) {
					return true;
				}
			}
			return false;
		}
		
		
		public String getContactName(ContentResolver cr, String phoneNumber) {
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
	


	private OnItemClickListener ConversationClick = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> adapter, View view,
				int position, long id) {
			Conversation item = (Conversation) adapter.getAdapter().getItem(
					position);
			setContentViewSMS(item);
		}
	};

	@SuppressLint("SimpleDateFormat") @SuppressWarnings("deprecation")
	@Override
	public void onPositionChanged(ExtendedListView listView, int position,
			View scrollBarPanel) {
		Conversation obj = (Conversation) listView.getAdapter().getItem(
				position);
		Date time;
		try {
			time = new Date(Long.parseLong(obj.getDate()));
		} catch (Exception e) {
			time = new Date(obj.getDate());
		}

		Clock mClock = (Clock) scrollBarPanel
				.findViewById(R.id.analogClockScroller);
		TextView tv = (TextView) scrollBarPanel.findViewById(R.id.timeTextView);
		TextView tvdate = (TextView) scrollBarPanel
				.findViewById(R.id.datetextView);
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		DateFormat timeFormat = new SimpleDateFormat("HH:mm");
		tv.setText(timeFormat.format(time));
		tvdate.setText(dateFormat.format(time));

		Time timeObj = new Time();
		mClock.setSecondHandVisibility(false);
		mClock.setVisibility(View.VISIBLE);
		timeObj.set(time.getSeconds(), time.getMinutes(), time.getHours(),
				time.getDay(), time.getMonth(), time.getYear());
		mClock.onTimeChanged(timeObj);
	}



	private void setAppTheme(int theme) {
		if (SdkInfo.VERSION.API_LEVEL >= 2) {
			getWindow().setWindowTheme(theme);
		} else {
			new MyToast(getApplicationContext(), "Api not supported").Show();
		}
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(IncomingSms);
	}

	View optionBack, optionMenu;

	private void setupOptionMenu() {
		LayoutInflater li = LayoutInflater.from(this);
		View header = li.inflate(R.layout.header, null);
		optionBack = header.findViewById(R.id.option_back);
		optionBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				setContentViewConversations();
			}
		});

		optionMenu = header.findViewById(R.id.option_menu);
		optionMenu.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PopupMenu popup = new PopupMenu(MainSmallApplication.this,
						optionMenu);
				popup.getMenuInflater().inflate(R.menu.menus, popup.getMenu());
				popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {

						switch (item.getItemId()) {
						case R.id.theme_Dark:
							MainSmallApplication.this.setAppTheme(com.sony.smallapp.R.style.Theme.Dark);
							break;
						case R.id.theme_light:
							MainSmallApplication.this.setAppTheme(com.sony.smallapp.R.style.Theme.Light);
							break;
						default:
							break;
						}
						return true;

					}
				});
				popup.show();
			}
		});

		getWindow().setHeaderView(header);
	}

	private static final String TELEPHON_NUMBER_FIELD_NAME = "address";
	private static final String MESSAGE_BODY_FIELD_NAME = "body";
	private static final String DATE_MSG = "date";
	private static final Uri SENT_MSGS_CONTET_PROVIDER = Uri
			.parse("content://sms/sent");

	private void addMessageToSentIfPossible(String telNumber,
			String messageBody, String time) {
		if (telNumber != null && messageBody != null && time != null) {
			addMessageToSent(telNumber, messageBody, time);
		}
	}

	private void addMessageToSent(String telNumber, String messageBody,
			String date) {
		ContentValues sentSms = new ContentValues();
		sentSms.put(TELEPHON_NUMBER_FIELD_NAME, telNumber);
		sentSms.put(MESSAGE_BODY_FIELD_NAME, messageBody);
		sentSms.put(DATE_MSG, date);
		contentResolver.insert(SENT_MSGS_CONTET_PROVIDER, sentSms);
	}


	private BroadcastReceiver IncomingSms = new BroadcastReceiver() {
		@Override
		public void onReceive(Context arg0, Intent intent) {
			final SmsManager sms = SmsManager.getDefault();
			final Bundle bundle = intent.getExtras();
			try {
				if (bundle != null) {
					final Object[] pdusObj = (Object[]) bundle.get("pdus");
					for (int i = 0; i < pdusObj.length; i++) {

						SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
						String phoneNumber = currentMessage.getDisplayOriginatingAddress();

						String senderNum = phoneNumber;
						String message = currentMessage.getMessageBody();

						Log.i("SmsReceiver", "senderNum: " + senderNum+ "; message: " + message);

						if (getWindow().getWindowState() == WindowState.MINIMIZED) {
							getWindow().setWindowState(WindowState.NORMAL);
						}
						// Show Alert
						new MyToast(MainSmallApplication.this, senderNum+": "+ "message").Show();
					} // end for loop
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							lvConversation.setAdapter(mAdapter);
							new AsyntaskMsg().execute(contentResolver);
						}
					}, 3000);
					
				} // bundle is null

			} catch (Exception e) {
				Log.e("SmsReceiver", "Exception smsReceiver" + e);
				e.printStackTrace();
			}
		}
	};

}
