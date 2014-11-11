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
import java.util.List;

import startfirst.smallapp.adapter.AwesomeAdapter;
//import startfirst.smallapp.adapter.ContanctAdapter;
import startfirst.smallapp.adapter.ConversationAdapter;
//import startfirst.smallapp.model.Contact;
import startfirst.smallapp.model.Conversation;
import startfirst.smallapp.model.ConversationRepository;
import startfirst.smallapp.model.SMS;
import startfirst.smallapp.widget.ApplicationConstants;
import startfirst.smallapp.widget.AsyncJob;
import startfirst.smallapp.widget.MyToast;
import startfirst.smallapp.widget.Utils;
import startfirst.smallapp.widget.Utils.ViewType;
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
import android.provider.ContactsContract;
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
	ContentResolver mContentResolver;
	ViewType mViewType = ViewType.ViewConversation;

	ExtendedListView lvConversation;
	ArrayList<Conversation> mConversations;
	ConversationAdapter mAdapter;

	ArrayList<SMS> mSMSs;
	AwesomeAdapter adapter;

	@Override
	public void onCreate() {
		super.onCreate();
		setAppTheme(com.sony.smallapp.R.style.Theme.Dark);
		registerReceiver(IncomingSms, new IntentFilter(
				"android.provider.Telephony.SMS_RECEIVED"));
		setMinimizedView(R.layout.minimized);
		SmallAppWindow.Attributes attr = getWindow().getAttributes();
		attr.width = getResources().getDimensionPixelSize(R.dimen.width);
		attr.height = getResources().getDimensionPixelSize(R.dimen.height);
		attr.flags |= SmallAppWindow.Attributes.FLAG_RESIZABLE;
		getWindow().setAttributes(attr);
		setupOptionMenu();
		mContentResolver = getContentResolver();
		setContentViewConversations();

	}

	private void setContentViewConversations() {
		mViewType = ViewType.ViewConversation;
		setTitle(R.string.app_name);
		optionNew.setVisibility(View.VISIBLE);
		optionBack.setVisibility(View.GONE);
		setContentView(R.layout.main);
		lvConversation = (ExtendedListView) findViewById(R.id.MainListView);
		lvConversation.setCacheColorHint(Color.TRANSPARENT);
		lvConversation.setOnPositionChangedListener(this);
		lvConversation.setOnItemClickListener(ConversationClick);
		if (mConversations != null) {
			lvConversation.setAdapter(mAdapter);
		}
		AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {
			@Override
			public void doOnBackground() {
				final ArrayList<Conversation> result = ApplicationConstants
						.getConversationRepository(mContentResolver).getAll();
				AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
					@Override
					public void doInUIThread() {
						if (mConversations == null
								|| Utils.isCheckNew(mConversations, result)) {
							mConversations = result;
							mAdapter = new ConversationAdapter(
									MainSmallApplication.this, mConversations);
							lvConversation.setAdapter(mAdapter);
						}
					}
				});
			}
		});
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

	String mAddressCurrent;

	private void setContentViewSMS(final Conversation input) {
		mAddressCurrent = input.getAddress();
		mViewType = ViewType.ViewSMS;
		optionBack.setVisibility(View.VISIBLE);
		optionNew.setVisibility(View.GONE);
		setContentView(R.layout.chat);
		setTitle(input.getName_Display() != null ? input.getName_Display()
				: input.getAddress());
		final ListView lv = (ListView) findViewById(R.id.ChatListView);
		AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {
			@Override
			public void doOnBackground() {
				mSMSs = ApplicationConstants.getSMSRepository(mContentResolver)
						.getAllSMSConversation(input.getThread_Id());
				AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
					@Override
					public void doInUIThread() {
						adapter = new AwesomeAdapter(MainSmallApplication.this,
								mSMSs);
						lv.setAdapter(adapter);
					}
				});
			}
		});
		final TextView btnSend = (TextView) findViewById(R.id.btnSend);
		btnSend.setEnabled(false);
		final EditText editContent = (EditText) findViewById(R.id.editTextContent);
		editContent.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (s.length() > 0) {
					btnSend.setEnabled(true);
				} else {
					btnSend.setEnabled(false);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
			}

			@Override
			public void afterTextChanged(Editable arg0) {
			}
		});

		btnSend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				btnSend.setEnabled(false);
				try {
					AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {
						@Override
						public void doOnBackground() {
							Utils.sendSMSMessage(mContentResolver, input
									.getAddress(), editContent.getText()
									.toString());
							AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
								@Override
								public void doInUIThread() {
									mSMSs.add(0,
											new SMS(null, input.getAddress(),
													new Date().getTime() + "",
													editContent.getText()
															.toString(), 2, 1));
									new MyToast(getApplicationContext(),
											"SMS Send...").Show();
									editContent.setText("");
									adapter.notifyDataSetChanged();
								}
							});
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
					new MyToast(getApplicationContext(),
							"SMS faild, please try again.").Show();
				}
			}
		});
	}

	// class AsyntaskMsg extends AsyncTask<ContentResolver, Void,
	// ArrayList<Conversation>>{
	//
	// @Override
	// protected void onPreExecute() {
	// super.onPreExecute();
	//
	// }
	//
	// @Override
	// protected ArrayList<Conversation> doInBackground(ContentResolver...
	// param) {
	// ArrayList<Conversation> uti = new ArrayList<Conversation>();
	//
	// Uri uri = Uri.parse("content://mms-sms/conversations/");
	// Cursor query = param[0].query(uri, new String[] {"thread_id", "address",
	// "date", "body", "read" }, null, null, " _id desc");
	// while (query.moveToNext()) {
	// String numberPhone = query.getString(1);
	// uti.add(new Conversation(query.getString(0),numberPhone,
	// getContactName(param[0], numberPhone), query.getString(2),
	// query.getString(3), query.getInt(4)));
	// }
	// query.close();
	// return uti;
	// }
	//
	// @Override
	// protected void onPostExecute(ArrayList<Conversation> result) {
	// super.onPostExecute(result);
	// if (mConversations == null || isCheckNew(mConversations, result)) {
	// mConversations = result;
	// mAdapter = new ConversationAdapter(MainSmallApplication.this,
	// mConversations);
	// lvConversation.setAdapter(mAdapter);
	// }
	//
	// }
	//
	// private boolean isCheckNew(ArrayList<Conversation> des,
	// ArrayList<Conversation> src){
	// int size = des.size()>src.size()?src.size():des.size();
	// for (int i = 0; i < size; i++) {
	// if (!src.get(i).getBody().equals(des.get(i).getBody()) ||
	// src.get(i).getRead() != des.get(i).getRead()) {
	// return true;
	// }
	// }
	// return false;
	// }
	//
	//
	// public String getContactName(ContentResolver cr, String phoneNumber) {
	// Uri uri =
	// Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,Uri.encode(phoneNumber));
	// Cursor cursor = cr.query(uri,new String[] { PhoneLookup.DISPLAY_NAME },
	// null, null, null);
	// if (cursor == null) {
	// return null;
	// }
	// String contactName = null;
	// if (cursor.moveToFirst()) {
	// contactName = cursor.getString(0);
	// }
	// if (cursor != null && !cursor.isClosed()) {
	// cursor.close();
	// }
	// return contactName;
	// }
	//
	//
	//
	// }

	@SuppressLint("SimpleDateFormat")
	@SuppressWarnings("deprecation")
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

	// private List<Contact> list = new ArrayList<Contact>();
	// private void setContentViewFindContact(){
	// optionBack.setVisibility(View.VISIBLE);
	// optionNew.setVisibility(View.GONE);
	// setTitle("Choice Contact");
	// setContentView(R.layout.find_contacts);
	// EditText editText = (EditText)findViewById(R.id.editTextFindContact);
	// ListView lv = (ListView)findViewById(R.id.listViewFindContact);
	//
	// Cursor phones =
	// getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
	// null, null,null, null);
	// while (phones.moveToNext()) {
	//
	// String name =
	// phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
	//
	// String phoneNumber =
	// phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
	//
	// Contact objContact = new Contact();
	// objContact.setName(name);
	// objContact.setPhoneNo(phoneNumber);
	// list.add(objContact);
	//
	// }
	// phones.close();
	//
	//
	// final ContanctAdapter objAdapter = new
	// ContanctAdapter(MainSmallApplication.this, R.layout.alluser_row, list);
	//
	// lv.setAdapter(objAdapter);
	//
	// editText.addTextChangedListener(new TextWatcher() {
	// @Override
	// public void onTextChanged(CharSequence s, int start, int before, int
	// count) {
	// objAdapter.filter(s.toString());
	// }
	// @Override
	// public void beforeTextChanged(CharSequence s, int start, int count,int
	// after) {}
	// @Override
	// public void afterTextChanged(Editable arg0) {}
	// });
	//
	//
	//
	//
	// }

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

	View optionBack, optionMenu, optionNew;

	private void setupOptionMenu() {
		LayoutInflater li = LayoutInflater.from(this);
		View header = li.inflate(R.layout.header, null);
		optionNew = header.findViewById(R.id.option_new);
		optionNew.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// setContentViewFindContact();
			}
		});
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
							MainSmallApplication.this
									.setAppTheme(com.sony.smallapp.R.style.Theme.Dark);
							break;
						case R.id.theme_light:
							MainSmallApplication.this
									.setAppTheme(com.sony.smallapp.R.style.Theme.Light);
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

	//
	// private void markMessageRead(Context context, String number, String body)
	// {
	//
	// Uri uri = Uri.parse("content://sms/inbox");
	// Cursor cursor = context.getContentResolver().query(uri, new String[] {
	// "_id", "address", "body","read" }, null,
	// null, null);
	// try {
	//
	// while (cursor.moveToNext()) {
	// if ((cursor.getString(cursor.getColumnIndex("address")).equals(number))
	// && (cursor.getInt(cursor.getColumnIndex("read")) == 0)) {
	// if (cursor.getString(cursor.getColumnIndex("body")).startsWith(body)) {
	// String SmsMessageId = cursor.getString(cursor.getColumnIndex("_id"));
	// ContentValues values = new ContentValues();
	// values.put("read", true);
	// context.getContentResolver().update(
	// Uri.parse("content://sms/inbox"), values,
	// "_id=" + SmsMessageId, null);
	// return;
	// }
	// }
	// }
	// cursor.close();
	// } catch (Exception e) {
	// e.printStackTrace();
	// Log.e("Mark Read", "Error in Read: " + e.toString());
	// }
	// }

	private BroadcastReceiver IncomingSms = new BroadcastReceiver() {
		@Override
		public void onReceive(Context arg0, Intent intent) {
			final SmsManager sms = SmsManager.getDefault();
			final Bundle bundle = intent.getExtras();
			try {
				if (bundle != null) {

					final Object[] pdusObj = (Object[]) bundle.get("pdus");
					for (int i = 0; i < pdusObj.length; i++) {
						SmsMessage currentMessage = SmsMessage
								.createFromPdu((byte[]) pdusObj[i]);
						String phoneNumber = currentMessage
								.getDisplayOriginatingAddress();

						String senderNum = phoneNumber;
						String message = currentMessage.getMessageBody();

						Log.i("SmsReceiver", "senderNum: " + senderNum
								+ "; message: " + message);

						if (getWindow().getWindowState() == WindowState.MINIMIZED) {
							getWindow().setWindowState(WindowState.NORMAL);
						}
						if (mViewType == ViewType.ViewSMS) {
							if (currentMessage.getDisplayOriginatingAddress()
									.equals(mAddressCurrent)) {
								mSMSs.add(
										0,
										new SMS(null, mAddressCurrent,
												currentMessage
														.getTimestampMillis()
														+ "", currentMessage
														.getMessageBody(), 1, 1));
								adapter.notifyDataSetChanged();
							}
						} else {
							if (mViewType == ViewType.ViewConversation) {
								lvConversation.setAdapter(mAdapter);
								new Handler().postAtTime(new Runnable() {
									@Override
									public void run() {
										AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {
											@Override
											public void doOnBackground() {
												mConversations = ApplicationConstants.getConversationRepository(mContentResolver).getAll();
												AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
													@Override
													public void doInUIThread() {
														mAdapter.notifyDataSetChanged();
													}
												});
											}
										});
									}
								}, 3000);
							}
						}

					} // end for loop
					// new Handler().postDelayed(new Runnable() {
					// @Override
					// public void run() {
					// lvConversation.setAdapter(mAdapter);
					// new AsyntaskMsg().execute(contentResolver);
					// }
					// }, 3000);
					// smsofconversation.clear();
					// Cursor smsCur =
					// contentResolver.query(Uri.parse("content://sms/"),
					// new String[] { "address", "date", "body", "type","read"
					// },
					// "thread_id=" + IDThreadCurrent, null, null);
					// while (smsCur.moveToNext()) {
					// smsofconversation.add(new SMS(smsCur.getString(0),
					// smsCur.getString(1), smsCur.getString(2),
					// smsCur.getInt(3)));
					// if (smsCur.getInt(4) == 0) {
					// markMessageRead(MainSmallApplication.this,
					// smsCur.getString(0), smsCur.getString(2));
					// }
					//
					// }
					// smsCur.close();
					// adapter.notifyDataSetChanged();
				} // bundle is null
			} catch (Exception e) {
				Log.e("SmsReceiver", "Exception smsReceiver" + e);
				e.printStackTrace();
			}
		}
	};

}
