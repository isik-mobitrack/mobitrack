package com.tytogroup.mobitrack.user;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tytogroup.mobitrack.R;


/**
 * Created by hsyn on 4/3/2015.
 */
public class UserAdapter extends ArrayAdapter {
    private AllUsers users;
    private AllFriendships friendships;

    public UserAdapter(Context context) {
        super(context, R.layout.user_list_item);
        users=AllUsers.getInstance();
        friendships=AllFriendships.getInstance();
        friendships.readyEmergencies();
    }

    @Override
    public int getCount() {
        return friendships.getCount();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if(convertView==null){
            LayoutInflater inflater=(LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView=inflater.inflate(R.layout.user_list_item,null);

        }

        TextView nameView=(TextView)convertView.findViewById(R.id.username_user_list);
        TextView distView=(TextView)convertView.findViewById(R.id.distance_user_list);
        final ImageView img=(ImageView)convertView.findViewById(R.id.emergency_user_list);
        User u=AllUsers.getUserForPhone(friendships.getFriendPhone(position));

        //return AllUsers.getUserForPhone(friendships.get(index).getFriend()).username;
        nameView.setText(u.username);
        distView.setText(u.getDistanceString());
        if(friendships.isEmergency(position)){
            img.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_action_warning_light));
        }
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(friendships.isEmergency(position)){
                    img.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_action_warning_dark));
                    friendships.removeFriendFromEmergency(position);
                }else {
                    img.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_action_warning_light));
                    friendships.addEmergencyFriend(position);
                }
            }
        });

        return convertView;
    }
}
