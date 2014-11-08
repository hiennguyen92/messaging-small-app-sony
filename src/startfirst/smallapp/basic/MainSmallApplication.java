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

import java.util.ArrayList;

import startfirst.smallapp.adapter.AwesomeAdapter;
import startfirst.smallapp.adapter.ConversationAdapter;
import startfirst.smallapp.model.Conversation;
import startfirst.smallapp.model.SMS;
import startfirst.smallapp.basic.R;

import com.sony.smallapp.SmallAppWindow;
import com.sony.smallapp.SmallApplication;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient.CustomViewCallback;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

public class MainSmallApplication extends SmallApplication {

	ContentResolver contentResolver;
	
	ArrayList<Conversation> mConversations;
    @Override
    public void onCreate() {
        super.onCreate();
        
        setMinimizedView(R.layout.minimized);
        setTitle(R.string.app_name);
        SmallAppWindow.Attributes attr = getWindow().getAttributes();
        attr.width = getResources().getDimensionPixelSize(R.dimen.width);
        attr.height = getResources().getDimensionPixelSize(R.dimen.height);
        attr.flags |= SmallAppWindow.Attributes.FLAG_RESIZABLE;
        getWindow().setAttributes(attr);
        setupOptionMenu();
        
        contentResolver = getContentResolver();
		setContentViewConversations();
		
//		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//			@Override
//			public void onItemClick(AdapterView<?> l, View v,int pos, long id) {
//				Conversation temp = (Conversation)l.getAdapter().getItem(pos);
//				Cursor smsCur = contentResolver.query(Uri.parse("content://sms/"), new String[] { "address", "date", "body","type" },  "thread_id=" + temp.getThread_Id(), null, null);
//				ArrayList<SMS> smsofconversation = new ArrayList<SMS>();
//				while (smsCur.moveToNext()) {
//					smsofconversation.add(new SMS(smsCur.getString(0), smsCur.getString(1), smsCur.getString(2), smsCur.getInt(3)));
//				}
//				setContentView(R.layout.chat);
////				Intent iSMS = new Intent(MainSmallApplication.this, Test2Activity.class);
////				iSMS.putExtra("DATA", smsofconversation);
////				startActivity(iSMS);
////				ListView lv2 = (ListView)findViewById(R.id.fragment_chat_listview_chat);
////				lv2.setAdapter(new AwesomeAdapter(getBaseContext(), smsofconversation));
//			}
//		});
        
        
    }
    
    private void setContentViewSMS(Conversation input){
    	setContentView(R.layout.chat);
    	ListView lv = (ListView)findViewById(R.id.ChatListView);
    	Button btnBack = (Button)findViewById(R.id.ChatBtnBack);
    	Cursor smsCur = contentResolver.query(Uri.parse("content://sms/"), new String[] { "address", "date", "body","type" },  "thread_id=" + input.getThread_Id(), null, null);
		ArrayList<SMS> smsofconversation = new ArrayList<SMS>();
		while (smsCur.moveToNext()) {
			smsofconversation.add(new SMS(smsCur.getString(0), smsCur.getString(1), smsCur.getString(2), smsCur.getInt(3)));
		}
		lv.setAdapter(new AwesomeAdapter(this, smsofconversation));
    	btnBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				setContentViewConversations();
			}
		});
    }
    
    
    private void setContentViewConversations(){
    	setContentView(R.layout.main);
    	ListView lv = (ListView)findViewById(R.id.MainListView);
    	lv.setOnItemClickListener(ConversationClick);
    	if (mConversations == null) {
    		Uri uri = Uri.parse("content://mms-sms/conversations/");
    		Cursor query = contentResolver.query(uri, new String[] { "thread_id", "address", "date", "body","read" }, null, null, " _id desc");
    		mConversations = new ArrayList<Conversation>();
    		while (query.moveToNext()) {
    			String numberPhone = query.getString(1);
    			mConversations.add(new Conversation(query.getString(0), numberPhone,getContactName(this, numberPhone), query.getString(2), query.getString(3),query.getInt(4)));	
    		}
    		ConversationAdapter adapter = new ConversationAdapter(this, mConversations);
			lv.setAdapter(adapter);
		}else {
			ConversationAdapter adapter = new ConversationAdapter(this, mConversations);
			lv.setAdapter(adapter);
		}
    }

    
    private OnItemClickListener ConversationClick = new OnItemClickListener() {
    	@Override
    	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
    		setContentViewSMS((Conversation)adapter.getAdapter().getItem(position));
    	}
	};
	

    
    
    
    

    
    
    
	public String getContactName(Context context, String phoneNumber) {
		ContentResolver cr = context.getContentResolver();
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
    }

    private void setupOptionMenu() {
        View header = LayoutInflater.from(this).inflate(R.layout.header, null);

        final View optionMenu = header.findViewById(R.id.option_menu);
        optionMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(MainSmallApplication.this, optionMenu);
                popup.getMenuInflater().inflate(R.menu.menus, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        Toast.makeText(MainSmallApplication.this,
                                R.string.menu_clicked, Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });
                popup.show();
            }
        });

        /* Deploy the option menu in the header area of the titlebar */
        getWindow().setHeaderView(header);
    }

}