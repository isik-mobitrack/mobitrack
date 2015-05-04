package com.tytogroup.mobitrack.user;


public class Friendship {
    public static final int REQUEST_FRIENDSHIP=0;
    public static final int REJECT_FRIENDSHIP=1;
    public static final int ACCEPT_FRIENDSHIP=2;
    public static final String SENDER="sender";
    public static final String RECIEVER="reciever";
    public static final String STATUS="status";

    public String sender;
    public String reciever;
    public int status;

    public Friendship() {}

    public Friendship(String sender, String reciever, int status) {
        this.sender = sender;
        this.reciever = reciever;
        this.status = status;
    }

    public String getFriend(){
        if(AllUsers.deviceUser.phone.equals(sender))
            return reciever;
        else
            return sender;
    }
}
