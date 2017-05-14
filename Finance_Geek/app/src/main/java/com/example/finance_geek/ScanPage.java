package com.example.finance_geek;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.googlecode.tesseract.android.TessBaseAPI;
import java.io.*;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.*;

public class ScanPage extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public final String APP_TAG = "Finance_Geek";
    public String photoFileName = "photo1.jpg";

    private FloatingActionButton selectPictureButton;
    private FloatingActionButton uploadButton;
    private ImageView imageView;
    private StorageReference myStorage;
    private static final int GALLERY_INTENT = 2;
    private static final int REQUEST_TAKE_PHOTO = 1;
    String CurrentPhotoPath;
    Bitmap image;
    private TessBaseAPI mTess;
    private String restaurant = "";
    String datapath = "";

    Uri mImageUri;
    File photo = null;

    //date
    final DateFormat df = new SimpleDateFormat("MMM dd, yyyy");
    final Date dateobj = new Date();

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan__page);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        myStorage = FirebaseStorage.getInstance().getReference();

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        selectPictureButton = (FloatingActionButton) findViewById(R.id.select_image);

        imageView = (ImageView)findViewById(R.id.imageView);

        //select image from gallery
        selectPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, GALLERY_INTENT);
            }
        });

        uploadButton = (FloatingActionButton)findViewById(R.id.upload);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                onLaunchCamera(view);
            }
        });


        //init image
        image = BitmapFactory.decodeResource(getResources(), R.drawable.anne);

        datapath = getFilesDir()+ "/tesseract/";

        //make sure training data has been copied
        checkFile(new File(datapath + "tessdata/"));

        //init Tesseract API
        String language = "eng";

        mTess = new TessBaseAPI();
        mTess.init(datapath, language);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkCameraPermission();
        }
    }

    public void onLaunchCamera(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try
        {
            // place where to store camera taken picture
            photo = this.createTemporaryFile("picture", ".jpg");
            photo.delete();
        }
        catch(Exception e)
        {
            Toast.makeText(this, "Please check SD card! Image shot is impossible!", Toast.LENGTH_SHORT).show();
        }
        mImageUri = Uri.fromFile(photo);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);

        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            if (resultCode == RESULT_OK)
            {
                this.getContentResolver().notifyChange(mImageUri, null);
                ContentResolver cr = this.getContentResolver();

                try
                {
                    image = android.provider.MediaStore.Images.Media.getBitmap(cr, mImageUri);
                    imageView = (ImageView) findViewById(R.id.imageView);
                    imageView.setImageBitmap(image);
                }
                catch (Exception e)
                {
                    Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT).show();
                }

                try {
                    //Pop up text input for restaurant name
                    AlertDialog.Builder alert = new AlertDialog.Builder(this);
                    alert.setMessage("Enter the restaurant name");
                    final EditText input = new EditText(this);
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    alert.setView(input);

                    // Set up the buttons
                    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            restaurant = input.getText().toString();
                            processImage();
                        }
                    });

                    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    alert.show();
                } catch (Exception e)
                {
                    Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
                }
            }
            else
            {
                Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }

        else if(requestCode == GALLERY_INTENT && resultCode == RESULT_OK)
        {
            Uri uri = data.getData();
            StorageReference filepath = myStorage.child("Photos").child(uri.getLastPathSegment());
            filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                }
            });
            imageView.setImageURI(uri);
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                image = bitmap;

                //Pop up text input for restaurant name
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                //alert.setTitle("Title");
                alert.setMessage("Enter the restaurant name");
                final EditText input = new EditText(this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                alert.setView(input);

                // Set up the buttons
                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        restaurant = input.getText().toString();
                        processImage();
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                alert.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        else if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK)
        {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);
        }
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
        getMenuInflater().inflate(R.menu.scan__page, menu);
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Intent i = new Intent(ScanPage.this, HomePage.class);
            startActivity(i);
        } else if (id == R.id.nav_report) {
            Intent i = new Intent(ScanPage.this, ReportPage.class);
            startActivity(i);
        } else if (id == R.id.nav_camera) {

        } else if (id == R.id.nav_search) {
            Intent i = new Intent(ScanPage.this, SearchPage.class);
            startActivity(i);
        } else if (id == R.id.nav_settings) {
            Intent i = new Intent(ScanPage.this, SettingsPage.class);
            startActivity(i);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void copyFiles() {
        try {
            //location we want the file to be at
            String filepath = datapath + "/tessdata/eng.traineddata";

            //get access to AssetManager
            AssetManager assetManager = getAssets();

            //open byte streams for reading/writing
            InputStream instream = assetManager.open("tessdata/eng.traineddata");
            OutputStream outstream = new FileOutputStream(filepath);

            //copy the file to the location specified by filepath
            byte[] buffer = new byte[1024];
            int read;
            while ((read = instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, read);
            }
            outstream.flush();
            outstream.close();
            instream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void checkFile(File dir) {
        //directory does not exist, but we can successfully create it
        if (!dir.exists()&& dir.mkdirs()){
            copyFiles();
        }
        //The directory exists, but there is no data file in it
        if(dir.exists()) {
            String datafilepath = datapath+ "/tessdata/eng.traineddata";
            File datafile = new File(datafilepath);
            if (!datafile.exists()) {
                copyFiles();
            }
        }
    }

    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 99;
    public boolean checkCameraPermission(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.CAMERA)) {

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);
            }
            return false;
        }
        else {
            return true;
        }
    }

    public void processImage(){
        String OCRresult = null;
        mTess.setImage(image);
        OCRresult = mTess.getUTF8Text();
        Log.v("OCR Message", OCRresult);

        String pattern1 = "(\\n)(\\d)(.*?)(\\$)((.)*)";
        String pattern2 = "(\\d)(.*)(\\d)(\\.)(\\d*)";
        Pattern r = Pattern.compile(pattern1);
        Matcher m = r.matcher(OCRresult);
        Matcher test = r.matcher(OCRresult);

        ArrayList itemName = new ArrayList();
        ArrayList itemPrice = new ArrayList();

        // Switches patterns if it doesn't match the receipt.
        if(!test.find()) {
            r = Pattern.compile(pattern2);
            m = r.matcher(OCRresult);
            while(m.find()) {
                String itemString = m.group(3) + m.group(4) + m.group(5);
                Log.v("Item String", itemString);
                itemName.add(m.group(2));
                itemPrice.add(itemString);
            }
        }
        if (test.find()) {
            while (m.find()) {
                itemName.add(m.group(3));
                itemPrice.add(m.group(5));
            }
        }

        Iterator<String> itName = itemName.iterator();
        Iterator<String> itPrice = itemPrice.iterator();

        double price;
        String item;
        boolean successMessage = false;
        while (itName.hasNext()){
            item = itName.next();
            try {
                price = Double.parseDouble(itPrice.next());
                writeToDB(restaurant, item, price);
                successMessage = true;
            }
            catch (Exception e) {
                Toast.makeText(this, "Problem reading receipt! Try another.", Toast.LENGTH_SHORT).show();
            }
        }
        if (successMessage) {
            Toast.makeText(ScanPage.this, "Successfully added items from receipt!", Toast.LENGTH_LONG).show();
        }
        else
        {
            Toast.makeText(this, "Problem reading receipt! Try another.", Toast.LENGTH_SHORT).show();
        }
    }

    public void writeToDB(String r, String i, double p)
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference myRef = database.getReference(user.getUid());
        final DatabaseReference itemListChild = myRef.child("Item List");
        DatabaseReference itemChild = itemListChild.push();
        itemChild.setValue(new Item(r, i, p, df.format(dateobj)));
    }

    private File createTemporaryFile(String part, String ext) throws Exception
    {
        File tempDir= Environment.getExternalStorageDirectory();
        tempDir=new File(tempDir.getAbsolutePath()+"/.temp/");
        if(!tempDir.exists())
        {
            tempDir.mkdirs();
        }
        return File.createTempFile(part, ext, tempDir);
    }
}
