package com.waroengweb.absensi;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.room.Room;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;

import com.tapadoo.alerter.Alerter;
import com.waroengweb.absensi.database.AppDatabase;
import com.waroengweb.absensi.database.entity.Ijin;
import com.waroengweb.absensi.helpers.DBHelper;

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

public class InputCutiActivity extends AppCompatActivity {

    EditText tglText,tglText2;
    Calendar myCalendar;
    Button takePicture,saveData,takePicture2;
    ImageView imagePhoto,imagePhoto2;
    Uri filePhoto,filePhoto2;
    String fileString,fileString2;
    AppDatabase db;
    AutoCompleteTextView nip,editTextFilledExposedDropdown;
    private AwesomeValidation validation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_cuti);

        myCalendar = Calendar.getInstance();

        tglText = (EditText)findViewById(R.id.tanggal);
        tglText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDateDialog(0);
            }
        });

        tglText2 = (EditText)findViewById(R.id.tanggal2);
        tglText2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDateDialog(1);
            }
        });



        takePicture = (Button)findViewById(R.id.take_picture);
        takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture();
            }
        });

        takePicture2 = (Button)findViewById(R.id.take_picture2);
        takePicture2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture2();
            }
        });

        imagePhoto = (ImageView)findViewById(R.id.preview);
        imagePhoto2 = (ImageView)findViewById(R.id.preview2);

        saveData = (Button)findViewById(R.id.save_data);
        saveData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
            }
        });

        db = DBHelper.builder(this);

        nip = (AutoCompleteTextView)findViewById(R.id.nip);
        List<String> NIP = db.AbsenDao().getNip();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, NIP);
        nip.setAdapter(adapter);

        validation = new AwesomeValidation(TEXT_INPUT_LAYOUT);
        AwesomeValidation.disableAutoFocusOnFirstFailure();
        validation.addValidation(this,R.id.nip_lbl, RegexTemplate.NOT_EMPTY,R.string.required);
        validation.addValidation(this,R.id.tanggal_lbl, RegexTemplate.NOT_EMPTY,R.string.required);
        validation.addValidation(this,R.id.tanggal_lbl2, RegexTemplate.NOT_EMPTY,R.string.required);

        String[] type = new String[] {"CUTI TAHUNAN", "CUTI SAKIT", "CUTI BESAR", "CUTI ALASAN PENTING","CUTI DILUAR TANGGUNGAN NEGARA","CUTI MELAHIRKAN"};

        ArrayAdapter<String> adapter2 =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        type);

        editTextFilledExposedDropdown =
                findViewById(R.id.filled_exposed_dropdown);
        editTextFilledExposedDropdown.setAdapter(adapter2);

    }

    private void takePicture()
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED ) {
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

    public void takePicture2()
    {
        final CharSequence[] options = {"Ambil Photo", "Pilih Dari Galeri", "Batal"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Silakan Pilih ambil photo atau dari galeri");

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("Ambil Photo")) {
                    StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                    StrictMode.setVmPolicy(builder.build());
                    Intent i;
                    i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File pictureFile = null;
                    try {
                        pictureFile = getOutputMediaFile();
                    } catch (IOException ex) {
                        Toast.makeText(InputCutiActivity.this,
                                "Photo file can't be created, please try again",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    filePhoto2 = Uri.fromFile(pictureFile);
                    Uri photoUri = FileProvider.getUriForFile(InputCutiActivity.this,
                            "com.waroengweb.absensi.provider",pictureFile
                    );
                    i.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                        i.putExtra("android.intent.extras.LENS_FACING_FRONT", 1);
                    } else {
                        i.putExtra("android.intent.extras.CAMERA_FACING", 1);
                    }
                    i.putExtra("android.intent.extra.USE_FRONT_CAMERA", true);
                    startActivityForResult(i,201);

                } else if (options[item].equals("Pilih Dari Galeri")) {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto, 202);//one can be replaced with any action code

                } else if (options[item].equals("Batal")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void openDateDialog(int tglNumber)
    {
        new DatePickerDialog(InputCutiActivity.this, new DatePickerDialog.OnDateSetListener() {
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
                    Toast.makeText(InputCutiActivity.this,"Hanya bisa input dibulan yang sama",Toast.LENGTH_SHORT).show();
                    return;
                }

                if (tglNumber == 0){
                    tglText.setText(sdf.format(myCalendar.getTime()));
                } else {
                    try {
                        Date date1 = sdf.parse(tglText.getText().toString());
                        calendar.setTime(date1);
                        if(calendar.after(myCalendar)) {
                            Toast.makeText(InputCutiActivity.this,"Tanggal Akhir harus lebih besar dari tanggal awal",Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    tglText2.setText(sdf.format(myCalendar.getTime()));
                }

            }
        }, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
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
            if (requestCode == 200){
                fileString = compressImage(filePhoto).toString();
                imagePhoto.setImageURI(Uri.parse(fileString));
                imagePhoto.requestFocus();
                takePicture.setText("Ganti Photo");
            } else if (requestCode == 201) {
                fileString2 = compressImage(filePhoto2).toString();
                imagePhoto2.setImageURI(Uri.parse(fileString2));
                imagePhoto2.requestFocus();
                takePicture2.setText("Ganti Photo");
            } else {
                File imageFile = new File(getRealPathFromURI(data.getData()));
                fileString2 = compressImage(Uri.fromFile(imageFile)).toString();
                imagePhoto2.setImageURI(Uri.parse(fileString2));
                takePicture2.setText("Ganti Photo");
            }

        }

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

    public void saveData()
    {
        if(validation.validate()) {
            if (nip.getText().length() != 18) {
                Alerter.create(this).setTitle("ERROR").setText("FORMAT NIP SALAH").setBackgroundColorInt(Color.RED).show();
                return;
            }

            if (fileString == null) {
                Alerter.create(this).setTitle("ERROR").setText("BELUM AMBIL PHOTO..").setBackgroundColorInt(Color.RED).show();
                return;
            }

            if (fileString2 == null) {
                Alerter.create(this).setTitle("ERROR").setText("BELUM AMBIL PHOTO KETERANGAN").setBackgroundColorInt(Color.RED).show();
                return;
            }

            Ijin cuti = new Ijin();
            cuti.setNip(nip.getText().toString());
            cuti.setApproved(0);
            cuti.setUploaded(0);
            cuti.setFoto(fileString);
            cuti.setKeterangan(fileString2);
            cuti.setLamaHari(1);
            cuti.setJenisIjin("Cuti");
            cuti.setTypeIjin(editTextFilledExposedDropdown.getText().toString());


            DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
            Date tanggalNew,tanggalNew2;

            try {
                tanggalNew = formatter.parse(tglText.getText().toString());
                tanggalNew2 = formatter.parse(tglText2.getText().toString());
                cuti.setTanggal(tanggalNew);
                cuti.setTanggal2(tanggalNew2);
            } catch (ParseException pe) {
                pe.printStackTrace();
            }

            db.IjinDao().insertIjin(cuti);
            Toast.makeText(this, "DATA BERHASIL DISIMPAN", Toast.LENGTH_SHORT).show();
            clearIjin();

        }
    }

    private void clearIjin()
    {
        nip.setText("");
        tglText2.setText("");
        tglText.setText("");
        fileString = null;
        fileString2 = null;
        takePicture.setText("Ambil Photo");
        takePicture2.setText("Photo/Gambar");
        imagePhoto.setImageDrawable(getResources().getDrawable(R.drawable.index));
        imagePhoto2.setImageDrawable(getResources().getDrawable(R.drawable.doc));

    }

}
