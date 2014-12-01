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
import startfirst.smallapp.adapter.ContanctAdapter;
//import startfirst.smallapp.adapter.ContanctAdapter;
import startfirst.smallapp.adapter.ConversationAdapter;
import startfirst.smallapp.model.Contact;
//import startfirst.smallapp.model.Contact;
import startfirst.smallapp.model.Conversation;
import startfirst.smallapp.model.ConversationRepository;
import startfirst.smallapp.model.SMS;
import startfirst.smallapp.widget.ApplicationConstants;
import startfirst.smallapp.widget.AsyncJob;
import startfirst.smallapp.widget.AsyncJob.OnMainThreadJob;
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
import android.graphics.drawable.Drawable;
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
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainSmallApplication extends SmallApplication implements
		OnPositionChangedListener {
	ContentResolver mContentResolver;
	ViewType mViewType = ViewType.ViewConversation;

	ExtendedListView lvConversation;
	ArrayList<Conversation> mConversations;
	ConversationAdapter mAdapter;
	ProgressBar mProgressBarMain;

	ArrayList<SMS> mSMSs;
	AwesomeAdapter adapter;

	@Override
	public void onCreate() {
		super.onCreate();
		ApplicationConstants.FormatDate = Utils.readSharePreferences(MainSmallApplication.this, "FORMAT", "dd/MM/yyyy");
		String theme = Utils.readSharePreferences(MainSmallApplication.this, "THEME", "Dark");
		if (theme.equals("Dark")) {
			setAppTheme(com.sony.smallapp.R.style.Theme.Dark);
			ApplicationConstants.ColorText = "#ffffff";
		}else {
			setAppTheme(com.sony.smallapp.R.style.Theme.Light);
			ApplicationConstants.ColorText = "#483D8B";
		}
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
		mProgressBarMain = (ProgressBar)findViewById(R.id.MainProgressBar);
		mProgressBarMain.setVisibility(View.VISIBLE);
		lvConversation.setCacheColorHint(Color.TRANSPARENT);
		lvConversation.setOnPositionChangedListener(this);
		lvConversation.setOnItemClickListener(ConversationClick);
//		if (ApplicationConstants.ColorText.equals("#ffffff")) {//Light
//			lvConversation.setDividerHeight(5);
//			lvConversation.setDivider(getResources().getDrawable(R.drawable.divider_light));
//		}else {
//			lvConversation.setDividerHeight(5);
//			lvConversation.setDivider(getResources().getDrawable(R.drawable.divider_light));
//		}
		
		
		if (mConversations != null) {
			lvConversation.setAdapter(mAdapter);
			mProgressBarMain.startAnimation(Utils.AnimationFadeOut());
			mProgressBarMain.setVisibility(View.GONE);
			lvConversation.setVisibility(View.VISIBLE);
			lvConversation.startAnimation(Utils.AnimationFadeIn());
		}
		AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {
			@Override
			public void doOnBackground() {
				final ArrayList<Conversation> result = ApplicationConstants
						.getConversationRepository(mContentResolver).getAll();
				AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
					@Override
					public void doInUIThread() {
						if (mConversations == null|| Utils.isCheckNew(mConversations, result)) {
							
							mProgressBarMain.startAnimation(Utils.AnimationFadeOut());
							mProgressBarMain.setVisibility(View.GONE);
							lvConversation.setVisibility(View.VISIBLE);
							lvConversation.startAnimation(Utils.AnimationFadeIn());
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
			Conversation item = (Conversation) adapter.getAdapter().getItem(position);
			mConversations.get(mConversations.indexOf(item)).setRead(1);
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
		setTitle(input.getName_Display() != null ? input.getName_Display(): input.getAddress());
		final ListView lv = (ListView) findViewById(R.id.ChatListView);
		if (input.getThread_Id() != null) {
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
		}else {
			AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {
				@Override
				public void doOnBackground() {
					mSMSs = ApplicationConstants.getSMSRepository(mContentResolver).getAllSMSConversationPhone(input.getAddress());
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
		}
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
			public void beforeTextChanged(CharSequence arg0, int arg1,int arg2, int arg3) {}

			@Override
			public void afterTextChanged(Editable arg0) {}
		});

		btnSend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				btnSend.setEnabled(false);
				try {
					AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {
						@Override
						public void doOnBackground() {
							try {
								Utils.sendSMSMessage(mContentResolver, input.getAddress(), editContent.getText().toString());
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
							} catch (final Exception e) {
								AsyncJob.doOnMainThread(new OnMainThreadJob() {
									@Override
									public void doInUIThread() {
										e.printStackTrace();
										new MyToast(getApplicationContext(),"SMS faild, please try again.").Show();
									}
								});
							}
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
		DateFormat dateFormat = new SimpleDateFormat(ApplicationConstants.FormatDate);
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

	 private List<Contact> listContact = new ArrayList<Contact>();
	 ContanctAdapter adapterContact;
	private void setContentViewFindContact() {
		mViewType = ViewType.ViewFindContact;
		optionBack.setVisibility(View.VISIBLE);
		optionNew.setVisibility(View.GONE);
		setTitle("Choice Contact");
		setContentView(R.layout.find_contacts);
		final EditText editText = (EditText) findViewById(R.id.editTextFindContact);
		final ListView lv = (ListView) findViewById(R.id.listViewFindContact);
		final LinearLayout layoutContent = (LinearLayout)findViewById(R.id.linearLayoutContentFindContact);
		final ProgressBar progrressBarLoading = (ProgressBar)findViewById(R.id.progressBarLoadingFindContact);
		
		AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {
			@Override
			public void doOnBackground() {
				
				Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null,null, null);
				while (phones.moveToNext()) {
					String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
					String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
					Contact objContact = new Contact();
					objContact.setName(name);
					objContact.setPhoneNo(phoneNumber);
					listContact.add(objContact);
				}
				phones.close();
				AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
					@Override
					public void doInUIThread() {
						progrressBarLoading.startAnimation(Utils.AnimationFadeOut());
						progrressBarLoading.setVisibility(View.GONE);
						layoutContent.setVisibility(View.VISIBLE);
						layoutContent.startAnimation(Utils.AnimationFadeIn());
						
						adapterContact = new ContanctAdapter(MainSmallApplication.this, R.layout.alluser_row, listContact);
						lv.setAdapter(adapterContact);
						lv.setOnItemClickListener(ContactItemClick);

						editText.addTextChangedListener(new TextWatcher() {
							@Override
							public void onTextChanged(CharSequence s, int start, int before,
									int count) {
								adapterContact.filter(s.toString());
							}

							@Override
							public void beforeTextChanged(CharSequence s, int start, int count,
									int after) {
							}

							@Override
							public void afterTextChanged(Editable arg0) {
							}
						});
					}
				});
			}
		});
		




	}
	OnItemClickListener ContactItemClick = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> lv, View v, int position, long id) {
			Contact item = ((ContanctAdapter)lv.getAdapter()).getItem(position);
			Conversation temp = new Conversation(item.getPhoneNo(), item.getName());
			setContentViewSMS(temp);
		}
	};

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
	
	private void setContentViewSettings(){
		mViewType = ViewType.ViewSettings;
		optionBack.setVisibility(View.VISIBLE);
		optionNew.setVisibility(View.GONE);
		setTitle("Settings");
		setContentView(R.layout.settings);
		TextView tvTitle = (TextView)findViewById(R.id.textViewTitleSettings);
		RadioGroup groupradio = (RadioGroup)findViewById(R.id.radioGroupSettingsFormatDate);
		RadioButton btnddMM = (RadioButton)findViewById(R.id.radioddMM);
		RadioButton btnMMdd = (RadioButton)findViewById(R.id.radioMMdd);
		tvTitle.setTextColor(Color.parseColor(ApplicationConstants.ColorText));
		btnddMM.setTextColor(Color.parseColor(ApplicationConstants.ColorText));
		btnMMdd.setTextColor(Color.parseColor(ApplicationConstants.ColorText));
		
		
		if (ApplicationConstants.FormatDate.equals("dd/MM/yyyy")) {
			btnddMM.setChecked(true);
		}else {
			btnMMdd.setChecked(true);
		}
		
		
		groupradio.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.radioddMM:
					Utils.writeSharePreferences(MainSmallApplication.this, "FORMAT", "dd/MM/yyyy");
					ApplicationConstants.FormatDate = "dd/MM/yyyy";
					break;
				case R.id.radioMMdd:
					Utils.writeSharePreferences(MainSmallApplication.this, "FORMAT", "MM/dd/yyyy");
					ApplicationConstants.FormatDate = "MM/dd/yyyy";
					break;
				default:
					break;
				}
			}
		});
		
		
	}
	
	
	

	View optionBack, optionMenu, optionNew;

	private void setupOptionMenu() {
		LayoutInflater li = LayoutInflater.from(this);
		View header = li.inflate(R.layout.header, null);
		optionNew = header.findViewById(R.id.option_new);
		optionNew.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				setContentViewFindContact();
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
							MainSmallApplication.this.setAppTheme(com.sony.smallapp.R.style.Theme.Dark);
							ApplicationConstants.ColorText = "#ffffff";
							Utils.writeSharePreferences(MainSmallApplication.this, "THEME", "Dark");
							if (mViewType == ViewType.ViewConversation) {
								setContentViewConversations();
							}
							if (mViewType == ViewType.ViewFindContact) {
								setContentViewFindContact();
							}
							
							break;
						case R.id.theme_light:
							MainSmallApplication.this.setAppTheme(com.sony.smallapp.R.style.Theme.Light);
							ApplicationConstants.ColorText = "#483D8B";
							Utils.writeSharePreferences(MainSmallApplication.this, "THEME", "Light");
							if (mViewType == ViewType.ViewConversation) {
								setContentViewConversations();
							}
							if (mViewType == ViewType.ViewFindContact) {
								setContentViewFindContact();
							}
							break;
						case R.id.settings:
							setContentViewSettings();
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
						switch (mViewType) {
						case ViewSMS:
							if (currentMessage.getDisplayOriginatingAddress().equals(mAddressCurrent)) {
								mSMSs.add(0,new SMS(null, mAddressCurrent,currentMessage.getTimestampMillis()+ "", currentMessage.getMessageBody(), 1, 1));
								adapter.notifyDataSetChanged();
							}
							break;
						case ViewConversation:
							new Handler().postDelayed(new Runnable() {
								@Override
								public void run() {
									setContentViewConversations();
								}
							}, 2000);
							
							break;

						default:
							break;
						}

					}
				} // bundle is null
			} catch (Exception e) {
				Log.e("SmsReceiver", "Exception smsReceiver" + e);
				e.printStackTrace();
			}
		}
	};

}
