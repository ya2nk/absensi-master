package com.waroengweb.absensi;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
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
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import android.widget.TextView;
import android.widget.Toast;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.room.Room;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;
import com.google.android.gms.common.ConnectionResult;
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
import com.google.android.material.textfield.TextInputLayout;

import com.tapadoo.alerter.Alerter;
import com.waroengweb.absensi.database.AppDatabase;
import com.waroengweb.absensi.database.entity.Dinas;
import com.waroengweb.absensi.helpers.ExifHelper;
import com.waroengweb.absensi.helpers.UriUtils;

import java.io.File;
import java.io.IOException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.basgeekball.awesomevalidation.ValidationStyle.TEXT_INPUT_LAYOUT;

import id.zelory.compressor.Compressor;

public class InputDinasActivity extends BaseActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, OnMapReadyCallback {

    Calendar myCalendar;
    int editTextSelect = 0;
    Button saveData,takePicture2;
    Uri filePhoto2;
    String fileString2,typeText="Sore",jenisText="dalam_dinas";
    ImageView imagePhoto2;
    AutoCompleteTextView nip;
    AppDatabase db;
    private AwesomeValidation validation;
    RadioGroup typeDinas;
    RadioGroup jenisDinas;
    TextInputLayout txtTgl,txtTgl2;
    TextView txtFile;
    LinearLayout mapContainer;
    private Location location;
    private GoogleApiClient googleApiClient;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 3000;
    private LocationRequest locationRequest;
    private static final long UPDATE_INTERVAL = 2000, FASTEST_INTERVAL = 2000;
    private Marker marker;
    private GoogleMap googleMap;
    Double latitude,longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_dinas);

        myCalendar = Calendar.getInstance();

        txtFile = (TextView) findViewById(R.id.txt_file);
        txtTgl = (TextInputLayout) findViewById(R.id.tgl_lbl);
        txtTgl.getEditText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (jenisText == "dalam_dinas") {
                    setDatePicker(0);
                } else {
                    setDatePicker(3);
                }
            }
        });

        txtTgl2 = (TextInputLayout) findViewById(R.id.tgl_lbl2);
        txtTgl2.getEditText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDatePicker2();
            }
        });
        txtTgl2.setVisibility(View.GONE);

        takePicture2 = (Button)findViewById(R.id.take_picture2);
        takePicture2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                i.setType("application/pdf");
                i.setAction(Intent.ACTION_GET_CONTENT);

                launchSomeActivity.launch(i);
            }
        });

        saveData = (Button)findViewById(R.id.save_data);
        saveData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
            }
        });


        //imagePhoto2 = (ImageView)findViewById(R.id.preview2);

        db = Room.databaseBuilder(this,
                AppDatabase.class, "MyDB").allowMainThreadQueries().fallbackToDestructiveMigration().build();

        nip = (AutoCompleteTextView)findViewById(R.id.nip);
        List<String> NIP = db.AbsenDao().getNip();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, NIP);
        nip.setAdapter(adapter);

        validation = new AwesomeValidation(TEXT_INPUT_LAYOUT);
        AwesomeValidation.disableAutoFocusOnFirstFailure();
        validation.addValidation(this,R.id.nip_lbl, RegexTemplate.NOT_EMPTY,R.string.required);
        validation.addValidation(this,R.id.tgl_lbl, RegexTemplate.NOT_EMPTY,R.string.required);

        jenisDinas = (RadioGroup)findViewById(R.id.jenis);
        jenisDinas.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                switch(checkedId) {
                    case R.id.dalam_dinas:
                        jenisText = "dalam_dinas";
                        txtTgl2.setVisibility(View.GONE);
                        mapContainer.setVisibility(View.VISIBLE);
                        break;
                    case R.id.luar_dinas:
                        jenisText = "luar_dinas";
                        txtTgl2.setVisibility(View.VISIBLE);
                        mapContainer.setVisibility(View.GONE);
                        break;

                }
                txtTgl.getEditText().getText().clear();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        googleApiClient = new GoogleApiClient.Builder(this).
                addApi(LocationServices.API).
                addConnectionCallbacks(this).
                addOnConnectionFailedListener(this).build();

        mapContainer = findViewById(R.id.map_container);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

    }
    public void takePicture2()
    {
        final CharSequence[] options = {"Pilih PDF Dari Galeri", "Batal"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Silakan Pilih File PDF");

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {

               if (options[item].equals("Pilih PDF Dari Galeri")) {
                    /* Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto, 204);//one can be replaced with any action code */
                   Intent i = new Intent();
                   i.setType("application/pdf");
                   i.setAction(Intent.ACTION_GET_CONTENT);

                   launchSomeActivity.launch(i);

                } else if (options[item].equals("Batal")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    public  File getOutputMediaFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String mFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File mFile = File.createTempFile(mFileName, ".jpg", storageDir);
        return mFile;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if(resultCode == RESULT_OK){
            if (requestCode == 201){

            }  else if (requestCode == 202) {

            } else if (requestCode == 203){

            }  else if (requestCode == 204) {

            }

        }

    }

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

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    public void saveData()
    {
        if(validation.validate()) {
            if (nip.getText().length() != 18) {
                Alerter.create(this).setTitle("ERROR").setText("FORMAT NIP SALAH").setBackgroundColorInt(Color.RED).show();
                return;
            }

            if (fileString2 == null) {
                Alerter.create(this).setTitle("ERROR").setText("BELUM AMBIL PHOTO BERKAS").setBackgroundColorInt(Color.RED).show();
                return;
            }
            if (Objects.equals(jenisText, "luar_dinas")) {
                if (txtTgl2.getEditText().getText().toString().isEmpty()) {
                    Alerter.create(this).setTitle("ERROR").setText("TANGGAL AKHIR BELUM DIPILIH").setBackgroundColorInt(Color.RED).show();
                    return;
                }
                latitude = 0.0;
                longitude = 0.0;
            }

            if(Objects.equals(jenisText, "dalam_dinas")) {
                if (latitude == null || longitude == null ){
                    Alerter.create(this).setTitle("ERROR").setText("LOKASI GPS MASIH KOSONG").setBackgroundColorInt(Color.RED).show();
                    return;
                }
            }

            Dinas dinas = new Dinas();
            dinas.setNip(nip.getText().toString());
            dinas.setApproved(0);
            dinas.setUploaded(0);
            dinas.setFotoBerkas(fileString2);
            dinas.setTypeDinas(typeText);
            dinas.setJenisDinas(jenisText);
            dinas.setLatitude(latitude);
            dinas.setLongitude(latitude);

            DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
            Date tanggalNew,tanggalNew2;

            try {
                tanggalNew = formatter.parse(txtTgl.getEditText().getText().toString());
                tanggalNew2 = tanggalNew;
                if (jenisText == "luar_dinas") {
                    tanggalNew2 = formatter.parse(txtTgl2.getEditText().getText().toString());
                }
                dinas.setTanggal(tanggalNew);
                dinas.setTanggal2(tanggalNew2);
            } catch (ParseException pe) {
                pe.printStackTrace();
            }

            db.DinasDao().insertDinas(dinas);
            Toast.makeText(this, "DATA BERHASIL DISIMPAN", Toast.LENGTH_SHORT).show();
            clearDinas();

        }
    }

    private void clearDinas()
    {
        nip.setText("");
        fileString2 = null;
        takePicture2.setText("Photo/Gambar");
        //imagePhoto2.setImageDrawable(getResources().getDrawable(R.drawable.doc));
        txtFile.setText("BELUM PILIH PDF");
        txtTgl.getEditText().getText().clear();
        txtTgl2.getEditText().getText().clear();
    }

    private void setDatePicker(int number)
    {
        DatePickerDialog datePickerDialog = new DatePickerDialog(InputDinasActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                String formatTanggal = "dd-MM-yyyy";
                SimpleDateFormat sdf = new SimpleDateFormat(formatTanggal);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                if (month != calendar.get(Calendar.MONTH)) {
                    Toast.makeText(InputDinasActivity.this,"Hanya bisa input dibulan yang sama",Toast.LENGTH_SHORT).show();
                    return;
                }
                txtTgl.getEditText().setText(sdf.format(myCalendar.getTime()));


            }
        }, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH)-number);
        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        datePickerDialog.getDatePicker().setMaxDate(myCalendar.getTimeInMillis());
        datePickerDialog.updateDate(myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void setDatePicker2()
    {
        DatePickerDialog datePickerDialog = new DatePickerDialog(InputDinasActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                String formatTanggal = "dd-MM-yyyy";
                SimpleDateFormat sdf = new SimpleDateFormat(formatTanggal);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                try {
                    Date date1 = sdf.parse(txtTgl.getEditText().getText().toString());
                    calendar.setTime(date1);
                    if(calendar.after(myCalendar)) {
                        Toast.makeText(InputDinasActivity.this,"Tanggal Akhir harus lebih besar dari tanggal awal",Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                txtTgl2.getEditText().setText(sdf.format(myCalendar.getTime()));
            }
        }, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    ActivityResultLauncher<Intent> launchSomeActivity
            = registerForActivityResult(
            new ActivityResultContracts
                    .StartActivityForResult(),
            result -> {
                if (result.getResultCode()
                        == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    // do your operation from here....
                    if (data != null
                            && data.getData() != null) {
                        Uri selectedImageUri = data.getData();
                        File pdfFile = null;
                        try {
                            pdfFile = UriUtils.getFileFromUri(getContentResolver(), selectedImageUri, getCacheDir());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        fileString2 =  pdfFile.toString();
                        txtFile.setText(fileString2);
                    }
                }
            });

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
                    Alerter.create(InputDinasActivity.this)
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
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (isLocationEnabled()) {
            // Permissions ok, we get last location
            location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);


            if (location != null) {
                if (location.isFromMockProvider()) {
                    Alerter.create(InputDinasActivity.this)
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
    protected void onResume() {
        super.onResume();
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
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

}
