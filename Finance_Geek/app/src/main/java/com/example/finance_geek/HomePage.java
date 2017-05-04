package com.example.finance_geek;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.google.firebase.auth.*;
import com.google.firebase.database.*;
import java.text.*;
import java.util.*;

public class HomePage extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static class Item {
        String restaurant;
        String item;
        double price;
        String date;

        public Item() {
            super();
        }

        public Item(String restaurantName, String itemName, double price, String date) {
            super();
            this.restaurant = restaurantName;
            this.item = itemName;
            this.price = price;
            this.date = date;
        }

        @Override
        public String toString() {
            NumberFormat priceFormatter = NumberFormat.getCurrencyInstance();

            return "Restaurant: " + this.restaurant + "\n"
                    + "Item: " + this.item + "\n"
                    + "Price: " + priceFormatter.format(price);
        }

        public boolean equals(String value1, String value2, double value3) {
            return this.restaurant == value1 && this.item == value2 && this.price == value3;
        }
    }

    int counter = 0; //counter for the + widget
    double sum = 0.0; //total price

    //data to send to Report Page
    final static HashMap<String, Double> data_price_date = new HashMap<>();
    String data_date;
    double data_price_total;

    //datepicker
    FloatingActionButton datepicker;
    int year_x, month_x, day_x;
    static final int DIALOG_ID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home__page);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            EditText restaurantText = (EditText) findViewById(R.id.restaurantText);
            EditText itemText = (EditText) findViewById(R.id.itemText);
            EditText priceText = (EditText) findViewById(R.id.priceText);
            Button button = (Button) findViewById(R.id.addButton);
            @Override
            public void onClick(View view) {

                if(counter % 2 == 0) {
                    restaurantText.setVisibility(View.VISIBLE);
                    itemText.setVisibility(View.VISIBLE);
                    priceText.setVisibility(View.VISIBLE);
                    button.setVisibility(View.VISIBLE);
                    counter++;
                }
                else {
                    restaurantText.setVisibility(View.GONE);
                    restaurantText.setText(null);
                    itemText.setVisibility(View.GONE);
                    itemText.setText(null);
                    priceText.setVisibility(View.GONE);
                    priceText.setText(null);
                    button.setVisibility(View.GONE);
                    counter = 0;
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

        //datepicker
        final Calendar calendar = Calendar.getInstance();
        year_x = calendar.get(Calendar.YEAR);
        month_x = calendar.get(Calendar.MONTH);
        day_x = calendar.get(Calendar.DAY_OF_MONTH);
        showDialogOnButtonClick();

        //date today
        final DateFormat df = new SimpleDateFormat("MMM dd, yyyy");
        final Date dateobj = new Date();
        final TextView date = (TextView) findViewById(R.id.date);
        date.setText(df.format(dateobj));

        // Add items via the Button and EditText at the bottom of the window.
        final EditText restaurantText = (EditText) findViewById(R.id.restaurantText);
        final EditText itemText = (EditText) findViewById(R.id.itemText);
        final EditText priceText = (EditText) findViewById(R.id.priceText);
        final Button button = (Button) findViewById(R.id.addButton);

        final TextView totalPrice = (TextView) findViewById(R.id.totalPrice);
        totalPrice.setText("Total: $0.00");

        // Get ListView object from xml
        final ListView listView = (ListView) findViewById(R.id.listView);

        // Create a new Adapter
        final ArrayAdapter<Item> adapter = new ArrayAdapter<Item>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1);

        // Assign adapter to ListView
        listView.setAdapter(adapter);

        // Connect to the Firebase database
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        // Get a reference to the child items in the database
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference myRef = database.getReference(user.getUid());
        final DatabaseReference itemListChild = myRef.child("Item List");

        //update listview based on datepiker
        date.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                //Item test = new Item("test", "test", 5.00, "May 02, 2017");
                //adapter.add(test);
                adapter.clear();
                itemListChild.orderByChild("date").addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                        Item value = dataSnapshot.getValue(Item.class);
                        Log.v("DATE: ", value.date);
                        Log.v("DATE AT TOP: ", date.getText().toString());
                        if(value.date.equals(date.getText().toString())) {
                            adapter.add(value);
                            Log.v("test", "test");
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {}

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {}

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {}

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
            }
        });

        // Assign a listener to detect changes to the child items
        // of the database reference.
        itemListChild.addChildEventListener(new ChildEventListener(){

            // This function is called once for each child that exists
            // when the listener is added. Then it is called
            // each time a new child is added.
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Item value = dataSnapshot.getValue(Item.class);
                if(value.date.equals(df.format(dateobj))) {
                    adapter.add(value);

                    data_date = value.date;
                }

                //updating total price
                if(adapter.isEmpty()) {
                    totalPrice.setText("Total: $0.00");
                }
                else {
                    //get total price
                    Double doublePrice = value.price;
                    sum = sum + doublePrice;
                    String stringPrice = String.valueOf(String.format("%.2f", sum)); //2 decimal places
                    totalPrice.setText("Total: $" + stringPrice);
                }

                Log.v("Item: ", value.toString());
                Log.v("Date: ", value.date);

                //pass data to report page
                if(data_price_date.containsKey(value.date))
                {
                    if(sum != 0) {
                        Log.v("in map: ", "existing key");
                        //data_price_total = data_price_date.get(value.date) + value.price;
                        //Log.v("oldPrice: ", data_price_date.get(value.date).toString());
                        data_price_date.put(value.date, sum);
                        Log.v("SUM: ", Double.toString(data_price_total));
                    }
                }
                else
                {
                    data_price_date.put(value.date, value.price);
                    data_price_total = 0;
                    Log.v("in map: ", "new key");
                }

                for (Map.Entry<String, Double> entry : data_price_date.entrySet()) {
                    Log.v("Map: ", entry.getKey() + ", " + entry.getValue());
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
                String regex = "[0-9.]*";
                String restaurant = restaurantText.getText().toString();
                String item = itemText.getText().toString();
                String priceString = priceText.getText().toString();
                double price = 0.00;

                //check if input is null
                if (restaurant.equals("")) {
                    Toast.makeText(getApplicationContext(), "You did not enter a restaurant", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(item.equals("")) {
                    Toast.makeText(getApplicationContext(), "You did not enter an item", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(priceString.equals("")) {
                    Toast.makeText(getApplicationContext(), "You did not enter a price", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(!priceString.equals("")) {
                    if(priceString.matches(regex)) {
                        price = Double.parseDouble(priceString);
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Enter a numerical price", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                DatabaseReference itemChild = itemListChild.push();
                Log.d("itemChild", itemChild.getKey());
                itemChild.setValue(new Item(restaurant, item, price, df.format(dateobj)));

                //UI changes
                restaurantText.setVisibility(View.GONE);
                restaurantText.setText(null);
                itemText.setVisibility(View.GONE);
                itemText.setText(null);
                priceText.setVisibility(View.GONE);
                priceText.setText(null);
                button.setVisibility(View.GONE);
                counter = 0;
            }
        });

        // Delete items when clicked
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                Item item = (Item) listView.getItemAtPosition(position);
                String restaurantValue = item.restaurant;
                String itemValue = item.item;
                double priceValue = item.price;

                Query myQuery = itemListChild.orderByChild("item").equalTo(restaurantValue);
                Log.v("QUERY", myQuery.toString());
                Log.v("Restaurant", restaurantValue);
                Log.v("Item", itemValue);
                Log.v("Price", Double.toString(priceValue));

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
                ;
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

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View v = getCurrentFocus();

        if (v != null &&
                (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_MOVE) &&
                v instanceof EditText &&
                !v.getClass().getName().startsWith("android.webkit.")) {
            int scrcoords[] = new int[2];
            v.getLocationOnScreen(scrcoords);
            float x = ev.getRawX() + v.getLeft() - scrcoords[0];
            float y = ev.getRawY() + v.getTop() - scrcoords[1];

            if (x < v.getLeft() || x > v.getRight() || y < v.getTop() || y > v.getBottom())
                hideKeyboard(this);
        }
        return super.dispatchTouchEvent(ev);
    }

    public static void hideKeyboard(Activity activity) {
        if (activity != null && activity.getWindow() != null && activity.getWindow().getDecorView() != null) {
            InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
        }
    }

    public static HashMap getPriceDateData() {
        return data_price_date;
    }

    public void showDialogOnButtonClick() {
        datepicker = (FloatingActionButton) findViewById(R.id.datepicker);

        datepicker.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                showDialog(DIALOG_ID);
            }
        });
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if(id == DIALOG_ID) {
            return new DatePickerDialog(this, dpickerListner, year_x, month_x, day_x);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener dpickerListner = new DatePickerDialog.OnDateSetListener() {
       @Override
       public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfYear) {
           year_x = year;
           month_x = monthOfYear;
           day_x = dayOfYear;
           String[] MONTHS = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

           DecimalFormat nf2 = new DecimalFormat("#00");

           TextView date = (TextView) findViewById(R.id.date);
           date.setText(MONTHS[month_x] + " " + nf2.format(day_x) + ", " + year_x);
       }
    };
}