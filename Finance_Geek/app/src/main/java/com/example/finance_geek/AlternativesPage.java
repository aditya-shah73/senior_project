package com.example.finance_geek;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class AlternativesPage extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    HashMap<String, Double> data = new HashMap<>();
    String address;
    String number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alternatives_page);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Get ListView object from xml
        final ListView listView = (ListView) findViewById(R.id.alternatves);

        // Create a new Adapter
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_2, android.R.id.text2);

        // Assign adapter to ListView
        listView.setAdapter(adapter);

        // Connect to the Firebase database
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        // Get a reference to the child items in the database
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference myRef = database.getReference(user.getUid());
        final DatabaseReference itemListChild = myRef.child("Item List");
        final DatabaseReference restaurant = database.getReference("Restaurant");

        //Query to get user items in database
        final Query item = itemListChild.orderByChild("item");
        item.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    HomePage.Item value = singleSnapshot.getValue(HomePage.Item.class);
                    String name = value.item;
                    double price = value.price;
                    data.put(name, price);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //query to get restaurants
        Query restaurants = restaurant.orderByKey();
        restaurants.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    final String restaurantName = singleSnapshot.getKey();
                    Log.v("RESTAURANT_NAME: ", restaurantName);

                    //query to get restaurant address
                    final Query restaurantAddress = restaurant.child(restaurantName).orderByKey().equalTo("Address");
                    restaurantAddress.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                                address = singleSnapshot.getValue().toString();
                                Log.v("RESTAURANT_ADDRESS: ", address);
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    //query to get restaurant phone number
                    final Query restaurantNumber = restaurant.child(restaurantName).orderByKey().equalTo("Phone Number");
                    restaurantNumber.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                                number = singleSnapshot.getValue().toString();
                                Log.v("RESTAURANT_NUMBER: ", number);
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    //query to get restaurant items
                    Query restaurantItem = restaurant.child(restaurantName).child("Item");
                    restaurantItem.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                                String key = singleSnapshot.getKey();
                                double value = Double.parseDouble(singleSnapshot.getValue().toString());
                                String price = String.valueOf(String.format("%.2f", value));
                                Log.v("RESTAURANT ITEMS: ", key + ", " + value);

                                //add to listview
                                for (Map.Entry entry : data.entrySet()) {
                                    if(entry.getKey().equals(key)) {
                                        Log.v("FIRST", "IF");

                                        if((double)entry.getValue() > value) {
                                            adapter.add(key + "\n"
                                                    + "Restaurant: " + restaurantName + "\n"
                                                    + "Price: " + price + "\n"
                                                    + "Address: " + address + "\n"
                                                    + "Number: " + number

                                            );
                                            Log.v("SECOND", "IF");
                                        }
                                    }
                                }
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.alternatives_page, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Intent i = new Intent(AlternativesPage.this, HomePage.class);
            startActivity(i);
        } else if (id == R.id.nav_report) {
            Intent i = new Intent(AlternativesPage.this, ReportPage.class);
            startActivity(i);
        } else if (id == R.id.nav_camera) {
            Intent i = new Intent(AlternativesPage.this, ScanPage.class);
            startActivity(i);
        } else if (id == R.id.nav_alternatives) {

        } else if (id == R.id.nav_search) {
            Intent i = new Intent(AlternativesPage.this, SearchPage.class);
            startActivity(i);
        } else if (id == R.id.nav_settings) {
            Intent i = new Intent(AlternativesPage.this, SettingsPage.class);
            startActivity(i);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
