package com.example.finance_geek;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HomePage extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static class Item {
        public String restaurant;
        public String item;
        public double price;

        public Item() {
            super();
        }

        public Item(String restaurantName, String itemName, double price) {
            super();
            this.restaurant = restaurantName;
            this.item = itemName;
            this.price = price;
        }

        @Override
        public String toString() {
            NumberFormat priceFormatter = NumberFormat.getCurrencyInstance();

            return "Restaurant: " + this.restaurant + "\n"
                    + "Item: " + this.item + "\n"
                    + "Price: " + priceFormatter.format(price);
        }
    }

    int counter = 0; //counter for the + widget

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home__page);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            EditText resturantText = (EditText) findViewById(R.id.restaurantText);
            EditText itemText = (EditText) findViewById(R.id.itemText);
            EditText priceText = (EditText) findViewById(R.id.priceText);
            Button button = (Button) findViewById(R.id.addButton);
            @Override
            public void onClick(View view) {

                if(counter % 2 == 0) {
                    resturantText.setVisibility(View.VISIBLE);
                    itemText.setVisibility(View.VISIBLE);
                    priceText.setVisibility(View.VISIBLE);
                    button.setVisibility(View.VISIBLE);
                    counter++;
                }
                else {
                    resturantText.setVisibility(View.GONE);
                    resturantText.setText(null);
                    itemText.setVisibility(View.GONE);
                    itemText.setText(null);
                    priceText.setVisibility(View.GONE);
                    priceText.setText(null);
                    button.setVisibility(View.GONE);
                    counter = 0;
                    //hide keyboard
                    InputMethodManager inputManager = (InputMethodManager)
                            getSystemService(Context.INPUT_METHOD_SERVICE);

                    inputManager.hideSoftInputFromWindow((null == getCurrentFocus()) ?
                            null : getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //date
        DateFormat df = new SimpleDateFormat("MMM dd, yyyy");
        Date dateobj = new Date();
        TextView date = (TextView) findViewById(R.id.date);
        date.setText(df.format(dateobj));

        // Add items via the Button and EditText at the bottom of the window.
        final EditText restaurantText = (EditText) findViewById(R.id.restaurantText);
        final EditText itemText = (EditText) findViewById(R.id.itemText);
        final EditText priceText = (EditText) findViewById(R.id.priceText);
        final Button button = (Button) findViewById(R.id.addButton);

        // Get ListView object from xml
        final ListView listView = (ListView) findViewById(R.id.listView);

        // Create a new Adapter
        final ArrayAdapter<Item> adapter = new ArrayAdapter<Item>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1);

        // Assign adapter to ListView
        listView.setAdapter(adapter);

        // Connect to the Firebase database
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        // Get a reference to the todoItems child items in the database
        //final DatabaseReference myRef = database.getReference("todoItems");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference myRef = database.getReference(user.getUid());

        // Assign a listener to detect changes to the child items
        // of the database reference.
        myRef.addChildEventListener(new ChildEventListener(){
            // This function is called once for each child that exists
            // when the listener is added. Then it is called
            // each time a new child is added.
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Item value = child.getValue(Item.class);
                    adapter.add(value);
                }
            }

            // This function is called each time a child item is removed.
            public void onChildRemoved(DataSnapshot dataSnapshot){
                Item value = dataSnapshot.getValue(Item.class);
                adapter.remove(value);
            }

            // The following functions are also required in ChildEventListener implementations.
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName){}
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName){}

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("TAG:", "Failed to read value.", error.toException());
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String resturant = restaurantText.getText().toString();

                String item = itemText.getText().toString();

                String priceString = priceText.getText().toString();
                double price = Double.parseDouble(priceString);

                DatabaseReference itemListRef = myRef.child("Item List");
                DatabaseReference itemRef = itemListRef.push();

                itemRef.setValue(new Item(resturant, item, price));
            }
        });

        // Delete items when clicked
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                Query myQuery = myRef.orderByValue().equalTo(
                        listView.getItemAtPosition(position).toString());

                myQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChildren()) {
                            DataSnapshot firstChild = dataSnapshot.getChildren().iterator().next();
                            firstChild.getRef().removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                })
                ;}
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
        getMenuInflater().inflate(R.menu.home__page, menu);
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {

        } else if (id == R.id.nav_report) {
            Intent i = new Intent(HomePage.this, ReportPage.class);
            startActivity(i);
        } else if (id == R.id.nav_camera) {
            Intent i = new Intent(HomePage.this, ScanPage.class);
            startActivity(i);
        } else if (id == R.id.nav_search) {
            Intent i = new Intent(HomePage.this, SearchPage.class);
            startActivity(i);
        } else if (id == R.id.nav_settings) {
            Intent i = new Intent(HomePage.this, SettingsPage.class);
            startActivity(i);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
