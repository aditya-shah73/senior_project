package com.example.myfirstapp;

import com.firebase.client.Firebase;

/**
 * Created by aditya on 9/21/16.
 */
public class FinanceGeek extends android.app.Application {

    @Override
    public void onCreate(){
        super.onCreate();
        Firebase.setAndroidContext(this);
    }
}
