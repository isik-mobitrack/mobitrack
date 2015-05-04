package com.tytogroup.mobitrack.user;

import java.util.ArrayList;

public class AllUsers {
    private static AllUsers instance;
    private ArrayList<User> users;
    public static User deviceUser=new User();

    private AllUsers(){
        users=new ArrayList<>();
    }

    public static AllUsers getInstance(){
        if(instance==null)
            instance=new AllUsers();
        return instance;
    }

    public static User getUserForPhone(String phone){
        for(int i=0;i<instance.users.size();i++){
            if(instance.users.get(i).phone.equals(phone))
                return instance.users.get(i);
        }
        return null;
    }

    public User getUser(int index){
        return users.get(index);
    }

    public String getUsername(String phone){
        for(int i=0;i<users.size();i++){
            if(users.get(i).phone.equals(phone))
                return users.get(i).username;
        }
        return null;
    }

    public String getUsername(int index){
        return users.get(index).username;
    }

    public int getCount(){
        return users.size();
    }

    public void addUser(User u){
        for(int i=0;i<users.size();i++){
            if(users.get(i).phone.equals(u.phone))
                return;
        }
        users.add(u);
    }

    public void clear(){
        users.clear();
    }

}
