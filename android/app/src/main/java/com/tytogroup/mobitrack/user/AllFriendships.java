package com.tytogroup.mobitrack.user;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.tytogroup.mobitrack.MainActivity;

import java.util.ArrayList;

public class AllFriendships {
    public static final String EMERGENCY_FRIENDS="emergincies";
    private static AllFriendships instance;
    private ArrayList<Friendship> friendships=new ArrayList<>();
    private ArrayList<Integer> emergencies=new ArrayList<>();

    private AllFriendships() {}

    public static AllFriendships getInstance() {
        if(instance==null){
            instance=new AllFriendships();
        }
        return instance;
    }

    public static ArrayList<Friendship> friends(){
        ArrayList<Friendship> z=new ArrayList<>();
        ArrayList<Friendship> t=new ArrayList<>();
        for(int i=0;i<instance.friendships.size();i++){
            switch (instance.getFriendStatus(i)){
                case 0:
                    z.add(instance.friendships.get(i));
                    break;
                case 1:

                    break;
                case 2:
                    t.add(instance.friendships.get(i));
                    break;
            }
        }
        ArrayList<Friendship> arr=new ArrayList<>();
        for(int i=0;i<t.size();i++){
            arr.add(t.get(i));
        }
        for(int i=0;i<z.size();i++){
            arr.add(z.get(i));
        }
        return arr;
    }

    public void addFriendship(String sender, String reciever, int status){
        Friendship f=new Friendship(sender,reciever, status);
        friendships.add(f);
    }

    public void reorder(){
        ArrayList<Friendship> z=new ArrayList<>();
        ArrayList<Friendship> o=new ArrayList<>();
        ArrayList<Friendship> t=new ArrayList<>();
        for(int i=0;i<friendships.size();i++){
            switch (getFriendStatus(i)){
                case 0:
                    z.add(friendships.get(i));
                    break;
                case 1:
                    o.add(friendships.get(i));
                    break;
                case 2:
                    t.add(friendships.get(i));
                    break;
            }
        }
        friendships.clear();
        for(int i=0;i<t.size();i++){
            friendships.add(t.get(i));
        }
        for(int i=0;i<o.size();i++){
            friendships.add(o.get(i));
        }
        for(int i=0;i<z.size();i++){
            friendships.add(z.get(i));
        }
    }

    /**
     *
     * @return returns how many friends does user have
     */
    public int getCount(){
        return friendships.size();
    }

    public void clear(){
        friendships.clear();
    }

    public String getFriendName(int index) {
        return AllUsers.getUserForPhone(friendships.get(index).getFriend()).username;
    }

    public int getFriendStatus(int index){
        return friendships.get(index).status;
    }
    
    /**
     *
     * @returns selected friends phone number
     */
    public String getFriendPhone(int index){
        return friendships.get(index).getFriend();
    }

    public void addEmergencyFriend(int index){
        for(int i=0;i<emergencies.size();i++){
            if(emergencies.get(i)==index)
                return;
        }
        SharedPreferences pref= PreferenceManager.getDefaultSharedPreferences(MainActivity.act.getApplicationContext());
        String emergency=pref.getString(EMERGENCY_FRIENDS, "");
        if(emergency.length()>0)
            emergency+=",";
        emergency+=index;
        emergencies.add(index);
        SharedPreferences.Editor edit=pref.edit();
        edit.putString(EMERGENCY_FRIENDS,emergency);
        edit.commit();
    }

    /**
     * removes a friend from emergency list if exist in
     * @param index index number of emergency friend inside emergency array.
     */
    public void removeEmergencyFriend(int index){
        if(index<0 || index>friendships.size()){
            return;
        }

        int in=emergencies.get(index);
        emergencies.remove(index);
        SharedPreferences pref= PreferenceManager.getDefaultSharedPreferences(MainActivity.act.getApplicationContext());
        String str="";
        if(emergencies.size()>0)
            str=""+emergencies.get(0);
        for(int i=1;i<emergencies.size();i++){
            str+=","+emergencies.get(i);
        }

        SharedPreferences.Editor edit=pref.edit();
        edit.putString(EMERGENCY_FRIENDS, str);
        edit.commit();
    }

    /**
     * removes a friend from emergency list if exist in
     * @param index index number of friend inside friendship array which is default friend array
     */
    public void removeFriendFromEmergency(int index){
        for(int i=0;i<emergencies.size();i++){
            if(emergencies.get(i)==index){
                removeEmergencyFriend(i);
            }
        }
    }

    /**
     * @return returns indexes of all emergency friends.
     * These indexes can be used for other methods in this class.
     */
    public ArrayList<Integer> getEmergencyFriends(){
        if(emergencies.size()==0)
            readyEmergencies();
        return emergencies;
    }

    public void readyEmergencies(){
        if(emergencies.size()!=0)
            return;
        SharedPreferences pref=PreferenceManager.getDefaultSharedPreferences(MainActivity.act.getApplicationContext());
        String e=pref.getString(EMERGENCY_FRIENDS,"");
        if(e.length()==0)
            return;
        String[] a=e.split(",");
        for(int i=0;i<a.length;i++){
            emergencies.add(Integer.parseInt(a[i]));
        }
    }

    public static boolean isEmergency(String phone){
        int index=0;
        for(int i=0;i<instance.getCount();i++){
            if(instance.getFriendPhone(i).equals(phone)){
                index=i;
                break;
            }
        }
        for(int i=0;i<instance.emergencies.size();i++){
            if(instance.emergencies.get(i)==index)
                return true;
        }
        return false;
    }

    public ArrayList<String> getFriendPhones(){
        ArrayList<String> p=new ArrayList<>();
        for(int i=0;i<friendships.size();i++){
            p.add(friendships.get(i).getFriend());
        }
        return p;
    }

}
