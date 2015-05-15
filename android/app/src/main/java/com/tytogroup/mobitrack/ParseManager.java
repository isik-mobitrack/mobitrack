package com.tytogroup.mobitrack;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.tytogroup.mobitrack.location.MyLocation;
import com.tytogroup.mobitrack.user.AllFriendships;
import com.tytogroup.mobitrack.user.AllUsers;
import com.tytogroup.mobitrack.user.Friendship;
import com.tytogroup.mobitrack.user.User;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ParseManager {
    public static final String USER="user";
    public static final String FRIENDSHIP="friendship";
    public static final String LOCATIONS="locations";


    private static ParseManager instance;

    public ParseManager() {}

    /**
     * Use this method to create a ParseManager
     * @return ParseManager
     */
    public static ParseManager getInstance() {
        if(instance==null)
            instance=new ParseManager();
        return instance;
    }

    /**
     * This method checks if there is a user with the given phone
     * @param phone phone to find a user
     * @param callback the callback to be called when server returned an answer
     */
    public void checkUser(String phone, FindCallback callback){
        ParseQuery query=new ParseQuery(USER);
        query.whereEqualTo(User.PHONE, phone);
        query.findInBackground(callback);
    }


    /**
     * This method saves a user with given values
     * @param phone phone of user
     * @param username user name of user
     * @param callback the callback to be called when server returned an answer
     */
    public void saveUser(String phone, String username,SaveCallback callback){
        ParseObject object=new ParseObject(USER);
        object.put(User.PHONE,phone);
        object.put(User.USERNAME, username);
        object.saveInBackground(callback);
    }

    /**
     * this method creates a friendship request with given phone to given phone
     * @param sender phone of friendship requester user
     * @param reciever phone of friendship requested user
     * @param callback the callback to be called when server returned an answer
     */
    public void createFriendshipRequest(String sender, String reciever, SaveCallback callback){
        ParseObject object=new ParseObject(FRIENDSHIP);
        object.put(Friendship.SENDER, sender);
        object.put(Friendship.RECIEVER, reciever);
        object.put(Friendship.STATUS, Friendship.REQUEST_FRIENDSHIP);
        object.saveInBackground(callback);
    }


    /**
     * this method returns all friendship with the given user
     * @param  phone of user
     * @param callback the callback to be called when server returned an answer
     */
    public void getFriendships(final String phone, final FindCallback<ParseObject> callback){
        ParseQuery query=ParseQuery.getQuery(FRIENDSHIP);
        query.whereEqualTo(Friendship.SENDER, phone);
        final AllFriendships friendships=AllFriendships.getInstance();
        friendships.clear();
        AllUsers.getInstance().clear();

         query.findInBackground(new FindCallback<ParseObject>() {
             @Override
             public void done(final List<ParseObject> list, ParseException e) {
                 if (e != null) {
                     callback.done(list, e);
                     return;
                 }

                 for (int i = 0; i < list.size(); i++) {
                     ParseObject p = list.get(i);
                     friendships.addFriendship(AllUsers.deviceUser.phone, p.getString(Friendship.RECIEVER), p.getInt(Friendship.STATUS));
                 }

                 ParseQuery query1 = ParseQuery.getQuery(FRIENDSHIP);
                 query1.whereEqualTo(Friendship.RECIEVER, phone);
                 query1.findInBackground(new FindCallback<ParseObject>() {
                     @Override
                     public void done(List<ParseObject> liste, ParseException e) {
                         if (e != null) {
                             callback.done(list, e);
                             return;
                         }
                         for (int i = 0; i < liste.size(); i++) {
                             list.add(liste.get(i));
                             ParseObject p = liste.get(i);
                             friendships.addFriendship(p.getString(Friendship.SENDER), AllUsers.deviceUser.phone, p.getInt(Friendship.STATUS));
                         }
                         friendships.reorder();
                         setUsersForFriendship(callback);
                     }
                 });
             }
         });
    }

    private void setUsersForFriendship(final FindCallback callback){
        final AllUsers users=AllUsers.getInstance();
        final AllFriendships friendships=AllFriendships.getInstance();
        ArrayList<String> phones=friendships.getFriendPhones();

        ParseQuery<ParseObject> query=new ParseQuery(USER);
        query.whereContainedIn(User.PHONE, phones);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if(e!=null){
                    callback.done(parseObjects, e);
                }
                for(int i=0;i<parseObjects.size();i++){
                    ParseObject p=parseObjects.get(i);
                    User u=new User();
                    u.phone=p.getString(User.PHONE);
                    u.username=p.getString(User.USERNAME);
                    ParseGeoPoint g=p.getParseGeoPoint(User.LAST_LOCATION);
                    LatLng l=new LatLng(g.getLatitude(),g.getLongitude());
                    u.location=l;
                    Location.distanceBetween(MyLocation.getlatitude(), MyLocation.getlongitude(), l.latitude, l.longitude, u.distance);
                    users.addUser(u);
                }
                callback.done(parseObjects, e);
            }
        });
    }

    /**
     * this method replies an existing friendship
     * @param parseObject a friendship to reply. must include an objectId
     * @param reply should be equal ParseManager.ACCEPT_FRIENDSHIP or REJECT_FRIENDSHIP
     * @param callback the callback to be called when server returned an answer
     */
    public void replyFriendship(ParseObject parseObject, int reply, SaveCallback callback){
        parseObject.put(Friendship.STATUS, reply);
        parseObject.saveInBackground(callback);
    }

    public void replyFriendship(boolean sender, String phone, final int reply, final SaveCallback callback){
        ParseQuery q=ParseQuery.getQuery(FRIENDSHIP);
        if(sender)
            q.whereEqualTo(Friendship.SENDER,phone);
        else
            q.whereEqualTo(Friendship.RECIEVER,phone);
        q.getFirstInBackground(new GetCallback() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if(e!=null){
                    e.printStackTrace();
                    return;
                }
                parseObject.put(Friendship.STATUS,reply);
                parseObject.saveInBackground(callback);
            }
        });
    }

    /**
     * this method finds last "count" location of "phone"
     * @param phone phone of user to find locations
     * @param count how many locations should be found
     * @param callback the callback to be called when server returned an answer
     */
    public void getLastLocations(String phone, int count, FindCallback<ParseObject> callback){
        ParseQuery query=ParseQuery.getQuery(LOCATIONS);
        query.whereEqualTo(USER, phone);
        query.setLimit(count);
        query.findInBackground(callback);
    }


    /**
     * add friendship request to parse database
     * @param phone phone of people who is beeing added for friendship
     * @param name name of people who is beeing added for friendship
     * @param callback the callback to be called when server returned an answer
     */
    public void addFriend(String phone, String name, SaveCallback callback){
        ParseObject o=ParseObject.create(FRIENDSHIP);
        o.put(Friendship.SENDER,AllUsers.deviceUser.phone);
        o.put(Friendship.RECIEVER,phone);
        o.put(Friendship.STATUS,Friendship.REQUEST_FRIENDSHIP);
        o.saveInBackground(callback);
    }


    /**
     * if you do not know what you do, do not use this method
     * @param phone
     * @param lat
     * @param lon
     * @param limit
     */
    public void addRandom(String phone, double lat, double lon, int limit){
        System.out.println("starting to send");
        for(int i=0;i<limit;i++) {
            ParseObject o = ParseObject.create(LOCATIONS);
            o.put(USER, phone);
            double a = ((Math.random() * 9000) + 1000);
            double lo = (lon + (a / 1000000));
            a = ((Math.random() * 9000) + 1000);
            double la = (lat + (a / 1000000));
            DecimalFormat format=new DecimalFormat("##.000000");
            o.put(LOCATIONS, new ParseGeoPoint(Double.parseDouble(format.format(la)), Double.parseDouble(format.format(lo))));
            o.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if(e!=null){
                        e.printStackTrace();
                        return;
                    }
                    System.out.println("sended");
                }
            });
        }
    }
}