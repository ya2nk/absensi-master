package com.waroengweb.absensi;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.room.Room;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.tapadoo.alerter.Alerter;
import com.waroengweb.absensi.app.AppController;
import com.waroengweb.absensi.app.NetworkHelper;
import com.waroengweb.absensi.database.AppDatabase;
import com.waroengweb.absensi.database.entity.Absen;
import com.waroengweb.absensi.helpers.Session;
import com.waroengweb.absensi.helpers.TimeSetting;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import static com.basgeekball.awesomevalidation.ValidationStyle.TEXT_INPUT_LAYOUT;

import id.zelory.compressor.Compressor;

public class Presensi extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, OnMapReadyCallback {

    Button takePicture,saveData;
    ImageView imagePhoto;
    AutoCompleteTextView nip;
    private TextView showMap;
    Timer timer = null;
    private String url = "https://siap.kerincikab.go.id/";
    private Uri filePhoto;
    private String fileString;
    private Location location;
    private GoogleApiClient googleApiClient;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 3000;
    private LocationRequest locationRequest;
    private static final long UPDATE_INTERVAL = 2000, FASTEST_INTERVAL = 2000; // = 5 seconds


    private AwesomeValidation validation;
    private AppDatabase db;
    private TimeSetting timeSetting;
    private Marker marker;
    private GoogleMap googleMap;

    Double latitude,longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_presensi);


        db = Room.databaseBuilder(this,
                AppDatabase.class, "MyDB").allowMainThreadQueries().fallbackToDestructiveMigration().build();

        takePicture = (Button)findViewById(R.id.take_picture);
        takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture();
            }
        });

        timeSetting = new TimeSetting(this);
        timeSetting.checkDateAndTimezoneSetting(this);

        saveData = (Button)findViewById(R.id.save_data);
        saveData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
            }
        });

        imagePhoto = (ImageView)findViewById(R.id.preview);
        nip = (AutoCompleteTextView) findViewById(R.id.nip);

        List<String> NIP = db.AbsenDao().getNip();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, NIP);
        nip.setAdapter(adapter);

        googleApiClient = new GoogleApiClient.Builder(this).
                addApi(LocationServices.API).
                addConnectionCallbacks(this).
                addOnConnectionFailedListener(this).build();

        validation = new AwesomeValidation(TEXT_INPUT_LAYOUT);
        AwesomeValidation.disableAutoFocusOnFirstFailure();
        validation.addValidation(this,R.id.nip_lbl, RegexTemplate.NOT_EMPTY,R.string.required);

        myTimer();

				SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        showMap = (TextView)findViewById(R.id.showMap);
        showMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String urlAddress = "http://maps.google.com/maps?q="+ latitude  +"," + longitude +"( Location )&iwloc=A&hl=es";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlAddress));
                startActivity(intent);

            }
        });


    }
		
		@Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }




    public void takePicture()
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            Intent i;
            i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File pictureFile = null;
            try {
                pictureFile = getOutputMediaFile();
            } catch (IOException ex) {
                Toast.makeText(this,
                        "Photo file can't be created, please try again",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            filePhoto = Uri.fromFile(pictureFile);
            Uri photoUri = FileProvider.getUriForFile(this,
                    "com.waroengweb.absensi.provider",pictureFile
                    );
            i.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                i.putExtra("android.intent.extras.LENS_FACING_FRONT", 1);
            } else {
                i.putExtra("android.intent.extras.CAMERA_FACING", 1);
            }
            i.putExtra("android.intent.extra.USE_FRONT_CAMERA", true);
            startActivityForResult(i,200);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if(resultCode == RESULT_OK){
            fileString = compressImage(filePhoto).toString();
            imagePhoto.setImageURI(Uri.parse(fileString));
            imagePhoto.requestFocus();
            takePicture.setText("Ganti Photo");
        }
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    /* public String compressImage(Uri fileData){
        File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + getPackageName() + "/media/image");
        if (f.mkdirs() || f.isDirectory()){
            return SiliCompressor.with(this).compress(fileData.getPath(),f,true);

        }
        return null;
    } */

    public File compressImage(Uri fileData){

        File compressFile;
        try {
            compressFile = new Compressor(this).compressToFile(new File(fileData.getPath()));
            return compressFile;
        } catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    public  File getOutputMediaFile() throws IOException {


        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String mFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File mFile = File.createTempFile(mFileName, ".jpg", storageDir);
        return mFile;
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if (isLocationEnabled()) {
            // Permissions ok, we get last location
            location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);


            if (location != null) {
                if (location.isFromMockProvider()){
                    Alerter.create(Presensi.this)
                            .setTitle("ERROR")
                            .setText("Terdeteksi menggunakan Fake Gps")
                            .setBackgroundColorInt(Color.RED).show();

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 1500);
                }
                //Toast.makeText(this,"Latitude : " + location.getLatitude() + "\nLongitude : " + location.getLongitude(),Toast.LENGTH_SHORT).show();
                latitude = location.getLatitude();
                longitude = location.getLongitude();

                LatLng latLng = new LatLng(latitude, longitude);
                MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("I am here!");
                googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                marker = googleMap.addMarker(markerOptions);


            }

            startLocationUpdates();
        } else {
            Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
    }

    private void startLocationUpdates() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this, "You need to enable permissions to display location !", Toast.LENGTH_SHORT).show();
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if (isLocationEnabled()) {

            if (location != null) {
                if (location.isFromMockProvider()) {
                    Alerter.create(Presensi.this)
                            .setTitle("ERROR")
                            .setText("Terdeteksi menggunakan Fake Gps")
                            .setBackgroundColorInt(Color.RED).show();

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 1500);
                }
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                //loadMapScene();
                //Toast.makeText(this,"Latitude : " + location.getLatitude() + "\nLongitude : " + location.getLongitude(),Toast.LENGTH_SHORT).show();
                marker.setPosition(new LatLng(latitude,longitude));
                //locationTv.setText("Latitude : " + location.getLatitude() + "\nLongitude : " + location.getLongitude());
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        timeSetting.checkDateAndTimezoneSetting(this);

        if (!checkPlayServices()) {
            Toast.makeText(this,"You need to install Google Play Services to use the App properly",Toast.LENGTH_SHORT).show();
        }

        if (googleApiClient != null) {
            googleApiClient.connect();
        }


    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST);
            } else {
                finish();
            }

            return false;
        }

        return true;
    }


    @Override
    protected void onPause() {
        super.onPause();
        

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                 // stop location updates
                if (googleApiClient != null  &&  googleApiClient.isConnected()) {
                    LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
                    googleApiClient.disconnect();


                }
        }

    }

    private void saveData()
    {
        if(validation.validate()) {

            if (latitude == null || longitude == null ){
                Alerter.create(this).setTitle("ERROR").setText("LOKASI GPS MASIH KOSONG").setBackgroundColorInt(Color.RED).show();
                return;
            }

            if (fileString == null){
                Alerter.create(this).setTitle("ERROR").setText("BELUM AMBIL PHOTO..").setBackgroundColorInt(Color.RED).show();
                return;
            }

            if(nip.getText().length() != 18){
                Alerter.create(this).setTitle("ERROR").setText("FORMAT NIP SALAH").setBackgroundColorInt(Color.RED).show();
                return;
            }

            Absen absen = new Absen();
            absen.setNip(nip.getText().toString());
            absen.setUploaded(0);
            absen.setLongitude(longitude);
            absen.setLatitude(latitude);
            absen.setFoto(fileString);
            absen.setHpModel(getDeviceName());
            absen.setApproved(0);
            absen.setTimezone(TimeZone.getDefault().getID());

            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date tanggalNew;
            String tanggal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            try {
                tanggalNew = formatter.parse(tanggal);
                absen.setTanggal(tanggalNew);
                absen.setTanggalReal(encrypt(tanggal));
            } catch (ParseException pe){
                pe.printStackTrace();
            }
            clearForm();
            if (NetworkHelper.isNetworkAvailable(this)) {
                StringRequest postRequest = new StringRequest(Request.Method.GET, url+"api/get-time",
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.d("Response", response);
                                absen.setTanggalServer(response);
                                db.AbsenDao().insertAbsen(absen);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // error
                                Log.d("Error.Response", error.toString());
                                db.AbsenDao().insertAbsen(absen);
                            }
                        }
                );
                AppController.getInstance().addToRequestQueue(postRequest);
            } else {
                db.AbsenDao().insertAbsen(absen);
            }


            Toast.makeText(this,"DATA BERHASIL DISIMPAN",Toast.LENGTH_SHORT).show();

            myTimer();
        }
    }

    private void clearForm()
    {
        nip.setText("");
        fileString = null;
        takePicture.setText("Ambil Photo");
        imagePhoto.setImageDrawable(getResources().getDrawable(R.drawable.index));
    }

    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }


    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    private void myTimer(){


        Log.d("Presensi", "Restarting timer");
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.d("Presensi", "Run the task");
                clearForm();
            }
        }, 300000);
    }

    public static String encrypt(String s){

        String ascString = "";
        for (int i = 0; i < s.length(); i++){
            int ascii = (int)s.charAt(i);
            ascString += String.valueOf(Character.toChars(ascii+1));
        }
        return ascString;
    }

    
}
