package com.tytogroup.mobitrack;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;
import com.tytogroup.mobitrack.location.FriendsLastLocationsOnMap;
import com.tytogroup.mobitrack.location.FriendsOnMap;
import com.tytogroup.mobitrack.location.MyLocation;
import com.tytogroup.mobitrack.location.NearbyLocationsOnMap;
import com.tytogroup.mobitrack.user.AllFriendships;
import com.tytogroup.mobitrack.user.AllUsers;
import com.tytogroup.mobitrack.user.User;
import com.tytogroup.mobitrack.user.UserAdapter;
import com.tytogroup.mobitrack.user.UserListFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity {
    public static final String PHONE="phone";
    public static MainActivity act;

    private ParseManager manager;
    private UserListFragment friendsFragment;
    private FriendsOnMap friendsMap;
    private SharedPreferences pref;
    private MenuItem toMap;
    private MenuItem toEmerg;
    private MenuItem toMes;
    private AllFriendships friendships;
    private FloatingActionButton fab;
    private boolean hasBack=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(MyLocation.checkLocationServices(getApplicationContext())){
            setContentView(R.layout.need_location_service);
            return;
        }

        setContentView(R.layout.activity_main);

        ActionBar bar=getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.primary)));

        act=this;
        friendships=AllFriendships.getInstance();
        manager = ParseManager.getInstance();
        pref= PreferenceManager.getDefaultSharedPreferences(this);
        String phone=pref.getString(PHONE,"");
        if(phone.length()>0){
            AllUsers.deviceUser.phone=phone;
        }else{
            createUser(phone);
            return;
        }

        manager.checkUser(AllUsers.deviceUser.phone, new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if(e!=null){
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "hata var", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(list.size()==0){
                    Toast.makeText(getApplicationContext(),"kayıtlı değil",Toast.LENGTH_SHORT).show();
                    createUser(AllUsers.deviceUser.phone);
                    return;
                }
                Toast.makeText(getApplicationContext(),"kayıtlı kullanıcı",Toast.LENGTH_SHORT).show();
                AllUsers.deviceUser.username=list.get(0).getString(User.USERNAME);
                initFriends();
            }
        });

        fab=(FloatingActionButton)findViewById(R.id.fab_activity_main);

        //do not remove these lines
        //manager.addRandom("+905437780683",40.76, -73.97, 100);
    }

    @Override
    protected void onStart() {
        super.onStart();
        MyLocation.getMyLocation(this).callHard();
    }

    private void initFriends(){
        friendsFragment=new UserListFragment();
        friendsFragment.setListener(friendSelected);
        if(hasBack)
            getSupportFragmentManager().beginTransaction().replace(R.id.main_activity_main, friendsFragment).commit();
        else
            getSupportFragmentManager().beginTransaction().add(R.id.main_activity_main, friendsFragment).commit();
        friendsFragment.setAction(fab);
        fab.setOnClickListener(actionClicked);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        toMap=menu.findItem(R.id.action_map_view);
        toEmerg=menu.findItem(R.id.action_emergency_places);
        toMes=menu.findItem(R.id.emergency_action);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id==R.id.emergency_action){
            if (friendsFragment.getListAdapter().getCount()>0) {
                if (friendships.getEmergencyFriends().size() == 0) {
                    CreatingEmergencyList();
                } else {
                    Intent sendMessage = new Intent(MainActivity.this, SendSMSActivity.class);
                    startActivity(sendMessage);
                }
            }else{
                Toast.makeText(getBaseContext(),"Please add a friend",Toast.LENGTH_LONG).show();
            }
        }else if(id==R.id.action_map_view){
            fab.hide();
            hideMenu();
            //if(friendsMap==null){
                friendsMap=new FriendsOnMap();
            //}
            getSupportFragmentManager().beginTransaction().replace(R.id.main_activity_main, friendsMap).commit();
            hasBack=true;

        }else if(id==R.id.action_emergency_places){
            fab.hide();
            hideMenu();
            NearbyLocationsOnMap nearLocations=new NearbyLocationsOnMap();
            getSupportFragmentManager().beginTransaction().replace(R.id.main_activity_main,nearLocations).commit();
            hasBack=true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void hideMenu(){
        toMes.setVisible(false);
        toEmerg.setVisible(false);
        toMap.setVisible(false);
    }

    private void showMenu(){
        toMes.setVisible(true);
        toEmerg.setVisible(true);
        toMap.setVisible(true);
    }

    @Override
    public void onBackPressed() {
        if(hasBack){
            showMenu();
            fab.show();
            initFriends();
            hasBack=false;
            return;
        }
        super.onBackPressed();
    }

    public void createUser(final String phone){
        final AlertDialog.Builder builder=new AlertDialog.Builder(this);
        final EditText text=new EditText(this);

        final EditText phoneText=new EditText(this);
        phoneText.setInputType(InputType.TYPE_CLASS_PHONE);
        if(phone.length()>0){
            phoneText.setText(phone);
        }else {
            phoneText.setHint(getResources().getString(R.string.your_number));
        }

        LinearLayout layout=new LinearLayout(this);
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(params);
        layout.addView(phoneText);
        layout.addView(text);

        text.setHint(getResources().getString(R.string.user_name_hint));
        builder.setMessage(getResources().getString(R.string.fill_your_info));
        builder.setView(layout);
        builder.setNegativeButton(getResources().getString(R.string.exit), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setPositiveButton(getResources().getString(R.string.create), null);
        final AlertDialog dialog=builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String as=text.getText().toString();
                final String phone=phoneText.getText().toString();
                if(as.equals(" ") || as.length()<1){
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.enter_user_name_warning),Toast.LENGTH_SHORT).show();
                }else{
                    manager.saveUser(phone,as,new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e!=null){
                                Toast.makeText(getApplicationContext(),getResources().getString(R.string.user_registration_error),Toast.LENGTH_LONG).show();
                                return;
                            }
                            Toast.makeText(getApplicationContext(),getResources().getString(R.string.user_registration_succesfull),Toast.LENGTH_LONG).show();
                            AllUsers.deviceUser.username=as;
                            SharedPreferences.Editor edit=pref.edit();
                            edit.putString(PHONE,phone);
                            edit.commit();
                            initFriends();
                        }
                    });
                    dialog.dismiss();
                }
            }
        });
    }

    public void addFriend(){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        final EditText edit=new EditText(this);
        edit.setInputType(InputType.TYPE_CLASS_PHONE);
        edit.setHint(getResources().getString(R.string.add_friend_hint));

        builder.setMessage(getResources().getString(R.string.add_friend_message));
        builder.setView(edit);
        builder.setNegativeButton(getResources().getString(R.string.cancel),null);
        builder.setPositiveButton(getResources().getString(R.string.add),null);


        final AlertDialog dialog=builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(AllUsers.getUserForPhone(edit.getText().toString())!=null){
                    Toast.makeText(getApplicationContext(),R.string.already_requested,Toast.LENGTH_LONG).show();
                    return;
                }
                manager.checkUser(edit.getText().toString(),new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> list, ParseException e) {
                        if(e!=null){
                            Toast.makeText(getApplicationContext(),getResources().getString(R.string.no_user),Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                            return;
                        }
                        if(list.size()==0){
                            Toast.makeText(getApplicationContext(),getResources().getString(R.string.no_user),Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String p=list.get(0).getString(User.PHONE);
                        final String n=list.get(0).getString(User.USERNAME);
                        manager.addFriend(p,n,new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if(e!=null){
                                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.add_error),Toast.LENGTH_LONG).show();
                                    e.printStackTrace();
                                    dialog.dismiss();
                                    return;
                                }
                                String s=String.format(getResources().getString(R.string.sended_friendship_request),n);
                                Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
                                dialog.dismiss();
                            }
                        });
                    }
                });
            }
        });
    }


    /***
     *
     *  shows the all friends to user and provides creating emergency friend list for sending message
     */
    private void CreatingEmergencyList() {

        AlertDialog.Builder multChoiceDialog = new AlertDialog.Builder(this);
        ArrayList<String> arrayList=new ArrayList<>();
        for(int i=0;i<friendsFragment.getListAdapter().getCount();i++){
            arrayList.add(friendships.getFriendName(i));
        }
        String [] arr=new String[arrayList.size()];
        arrayList.toArray(arr);
        multChoiceDialog.setTitle("Select your emergency friend list");

        boolean[] _selections = new boolean[arr.length];

        multChoiceDialog.setMultiChoiceItems(arr, _selections,	new DialogInterface.OnMultiChoiceClickListener() {
            public void onClick(DialogInterface dialog,
                                int whichButton, boolean isChecked) {
            }
        });

// add positive button here
        multChoiceDialog.setPositiveButton("OK",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // getting listview from alert box
                ListView list = ((AlertDialog) dialog).getListView();
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < list.getCount(); i++) {
                    boolean checked = list.isItemChecked(i);
                    // get checked list value
                    if (checked) {
                       friendships.addEmergencyFriend(i);
                        if (sb.length() > 0)
                            sb.append(",");
                        sb.append(list.getItemAtPosition(i));
                    }
                }
                 ArrayList a=new ArrayList();
                a=friendships.getEmergencyFriends();
                Toast.makeText(getApplicationContext(),"Selected:"
                        +sb.toString(),Toast.LENGTH_SHORT).show();
            }
        });

// add negative button
        multChoiceDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // cancel code here
                    }
                });
        AlertDialog alert1 = multChoiceDialog.create();
        alert1.show();
    }

    private AdapterView.OnItemClickListener friendSelected=new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(position>= UserAdapter.endFriend)
                return;
            FriendsLastLocationsOnMap last=new FriendsLastLocationsOnMap();
            last.position=position;
            hasBack=true;
            getSupportFragmentManager().beginTransaction().replace(R.id.main_activity_main,last).commit();

        }
    };

    private View.OnClickListener actionClicked=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            addFriend();
        }
    };
}
