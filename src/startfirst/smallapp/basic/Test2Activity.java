package startfirst.smallapp.basic;

import java.util.ArrayList;

import startfirst.smallapp.adapter.AwesomeAdapter;
import startfirst.smallapp.model.Conversation;
import startfirst.smallapp.model.SMS;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.ListView;

public class Test2Activity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat);
		
		ArrayList<SMS> data = (ArrayList<SMS>)getIntent().getSerializableExtra("DATA");
		
		((ListView)findViewById(R.id.ChatListView)).setAdapter(new AwesomeAdapter(this, data));
		
	}


}
