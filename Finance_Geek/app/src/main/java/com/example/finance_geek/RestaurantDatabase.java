package com.example.finance_geek;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class RestaurantDatabase extends AppCompatActivity {

    public class MenuItem {
        String name;
        double price;

        public MenuItem() {
            super();
        }

        public MenuItem(String name, double price) {
            super();
            this.name = name;
            this.price = price;
        }
    }

    public static void main (String[] args) {
        new RestaurantDatabase().createDatabase();
        System.out.println("In Restaurant.java");
    }

    public void createDatabase() {
        // Connect to the Firebase database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        Log.d("test", "in createDatabase");
        DatabaseReference myRef = database.getReference("Restaurant");

        ArrayList<MenuItem> VegetarianHouse = new ArrayList<>();

        myRef.child("Vegetarian House").push().setValue(new MenuItem("Crispy Spring Rolls", 6.50));
    }
}
