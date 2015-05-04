package com.tytogroup.mobitrack;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tytogroup.mobitrack.user.AllFriendships;
import com.tytogroup.mobitrack.user.AllUsers;

import java.util.ArrayList;

public class SendSMSActivity extends ActionBarActivity {
	Button buttonSend;
	EditText textPhoneNo;
	EditText textSMS;
    private AllFriendships friendships;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.emergency_message);
        friendships=AllFriendships.getInstance();
		buttonSend = (Button) findViewById(R.id.buttonSend);
		textSMS = (EditText) findViewById(R.id.editTextSMS);
        textSMS.setText("I need your help! Please contact me!");
        textPhoneNo=(EditText)findViewById(R.id.EditTextPhoneNo);
        textPhoneNo.setEnabled(false);
        final ArrayList<Integer> telephoneNums;
        telephoneNums=friendships.getEmergencyFriends();
        StringBuilder builder = new StringBuilder();
        for(int i=0;i<telephoneNums.size();i++) {
            builder.append(String.valueOf(friendships.getFriendPhone(telephoneNums.get(i))));
            builder.append(";");
        }
        textPhoneNo.setText(builder.toString());
		buttonSend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String sms = textSMS.getText().toString();
                if (friendships.getEmergencyFriends().size() == 0) {
                    Toast.makeText(getBaseContext(), "There is no number to send message", Toast.LENGTH_LONG).show();
                }
                try {
                    SmsManager smsManager = SmsManager.getDefault();
                    StringBuilder builder2 = new StringBuilder();
                    String delim = "";
                    int i;
                    for (i = 0; i < telephoneNums.size(); i++) {
                        builder2.append(delim).append(String.valueOf(friendships.getFriendPhone(telephoneNums.get(i))));
                        delim = ";";
                        smsManager.sendTextMessage(builder2.toString(), null, sms, null, null);
                        delim = "";
                        builder2.setLength(0);
                    }
                    Toast.makeText(getApplicationContext(), "SMS Sent!",
                            Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(),
                            "SMS failed, please try again later!",
                            Toast.LENGTH_LONG).show();
                    e.printStackTrace();//içindeki yeni dışındaki eski
                }// iç içe iki tane var onu napacaz mobitrack mi 2 tanehe bak şimdi
            }
        });
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN| WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
	}
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.emergency_message_menu, menu);
        setTitle("Emergency Messaging");
        return super.onCreateOptionsMenu(menu);
    }
}