package com.example.friendly.objects;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("FriendRemoval")
public class FriendRemoval extends ParseObject {

    public static final String KEY_FROM_USER = "fromUser";
    public static final String KEY_TO_USER = "toUser";

    public ParseUser getFromUser(){
        return getParseUser(KEY_FROM_USER);
    }

    public void setFromUser(ParseUser user){
        put(KEY_FROM_USER,user);
    }

    public ParseUser getToUser(){
        return getParseUser(KEY_TO_USER);
    }

    public void setToUser(ParseUser user){
        put(KEY_TO_USER,user);
    }

}