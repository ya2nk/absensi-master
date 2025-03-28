package com.waroengweb.absensi;

import android.app.Activity;
import android.app.AlertDialog;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RadioGroup;

import android.widget.TextView;
import android.widget.Toast;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.FileProvider;
import androidx.room.Room;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;
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

import static com.basgeekball.awesomevalidation.ValidationStyle.TEXT_INPUT_LAYOUT;

import id.zelory.compressor.Compressor;

public class InputDinasActivity extends BaseActivity {

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
                        break;
                    case R.id.luar_dinas:
                        jenisText = "luar_dinas";
                        txtTgl2.setVisibility(View.VISIBLE);
                        break;

                }
                txtTgl.getEditText().getText().clear();
            }
        });
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
            if (jenisText == "luar_dinas") {
                if (txtTgl2.getEditText().getText().toString().isEmpty()) {
                    Alerter.create(this).setTitle("ERROR").setText("TANGGAL AKHIR BELUM DIPILIH").setBackgroundColorInt(Color.RED).show();
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
}
