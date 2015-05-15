package com.tytogroup.mobitrack.user;


import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.SaveCallback;
import com.tytogroup.mobitrack.ParseManager;
import com.tytogroup.mobitrack.R;

import java.util.ArrayList;


/**
 * Created by hsyn on 4/3/2015.
 */
public class UserAdapter extends ArrayAdapter {
    private AllUsers users;
    private ArrayList<Friendship> friendships;
    private int headCount=0;
    public static int endFriend=0;

    public UserAdapter(Context context) {
        super(context, R.layout.user_list_item);
    }

    public void reloadFriendships(){
        friendships=AllFriendships.friends();
        headCount=0;
    }

    @Override
    public int getCount() {
        if(friendships==null)
            return 0;
        if(friendships.size()>0)
            return friendships.size()+2;
        else
            return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView==null){
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.user_list_item, null);
        }

        TextView nameView=(TextView)convertView.findViewById(R.id.username_user_list);
        TextView distView=(TextView)convertView.findViewById(R.id.distance_user_list);
        final ImageView img=(ImageView)convertView.findViewById(R.id.emergency_user_list);

        if(isTitle(position)){
            distView.setVisibility(View.GONE);
            img.setVisibility(View.GONE);
            nameView.setTextColor(Color.parseColor("#FF1B77EA"));
            if(headCount==0){
                nameView.setText("Your friends");
            }else {
                nameView.setText("Friendship requests");
            }
            endFriend=position;
            headCount++;
            return convertView;
        }

        distView.setVisibility(View.VISIBLE);
        img.setVisibility(View.VISIBLE);
        nameView.setTextColor(Color.parseColor("#000000"));
        final int index=position-headCount;
        User u=AllUsers.getUserForPhone(friendships.get(index).getFriend());

        //return AllUsers.getUserForPhone(friendships.get(index).getFriend()).username;
        nameView.setText(u.username);
        distView.setText(u.getDistanceString());
        if(AllFriendships.isEmergency(friendships.get(index).getFriend())){
            img.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_action_warning_light));
        }
        if(friendships.get(index).status==Friendship.REQUEST_FRIENDSHIP){
            img.setImageResource(R.drawable.ic_action_tick);
            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    friendships.get(index).status=Friendship.ACCEPT_FRIENDSHIP;
                    reloadFriendships();
                    notifyDataSetChanged();
                    boolean sender=false;
                    String p=friendships.get(index).getFriend();
                    if(p.equals(AllUsers.deviceUser.phone))
                        sender=true;
                    ParseManager.getInstance().replyFriendship(sender, p, Friendship.ACCEPT_FRIENDSHIP, new SaveCallback() {
                        @Override
                        public void done(ParseException e) {

                        }
                    });
                }
            });
        }else {
            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (AllFriendships.isEmergency(friendships.get(index).getFriend())) {
                        img.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_action_warning_dark));
                        AllFriendships.getInstance().removeFriendFromEmergency(index);
                    } else {
                        img.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_action_warning_light));
                        AllFriendships.getInstance().addEmergencyFriend(index);
                    }
                }
            });
        }

        return convertView;
    }

    public boolean isTitle(int index){
        if(index==0)
            return true;
        index-=headCount;
        if(index>friendships.size()-1 || index==0 || headCount>=2)
            return false;
        if(friendships.get(index).status!=friendships.get(index-1).status ){
            return true;
        }else {
            return false;
        }
    }

    public void remove(int index){
        int head=0;
        for(int i=0;i<index;i++){
            if(isTitle(i))
                head++;
        }
        index-=head;
        friendships.remove(index);
        boolean sender=false;
        String p=friendships.get(index).getFriend();
        if(p.equals(AllUsers.deviceUser.phone))
            sender=true;
        /*ParseManager.getInstance().replyFriendship(sender, p, Friendship.REJECT_FRIENDSHIP, new SaveCallback() {
            @Override
            public void done(ParseException e) {

            }
        });*/
    }
}
