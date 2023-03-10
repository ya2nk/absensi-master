package com.waroengweb.absensi;

import android.app.AlertDialog;

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

import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.room.Room;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;
import com.google.android.material.textfield.TextInputLayout;

import com.tapadoo.alerter.Alerter;
import com.waroengweb.absensi.database.AppDatabase;
import com.waroengweb.absensi.database.entity.Dinas;
import com.waroengweb.absensi.helpers.ExifHelper;

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
    Button takePicture,saveData,takePicture2;
    Uri filePhoto,filePhoto2;
    String fileString,fileString2,typeText="Pagi",jenisText="dalam_kota";
    ImageView imagePhoto,imagePhoto2;
    AutoCompleteTextView nip;
    AppDatabase db;
    private AwesomeValidation validation;
    RadioGroup typeDinas;
    RadioGroup jenisDinas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_dinas);

        myCalendar = Calendar.getInstance();

        String timeStamp = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
        TextView txtTgl = (TextView) findViewById(R.id.tgl_txt);
        txtTgl.setText(timeStamp);

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

        saveData = (Button)findViewById(R.id.save_data);
        saveData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
            }
        });

        imagePhoto = (ImageView)findViewById(R.id.preview);
        imagePhoto2 = (ImageView)findViewById(R.id.preview2);

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
        validation.addValidation(this,R.id.tanggal_lbl, RegexTemplate.NOT_EMPTY,R.string.required);
        validation.addValidation(this,R.id.tanggal_lbl2, RegexTemplate.NOT_EMPTY,R.string.required);

        typeDinas = (RadioGroup)findViewById(R.id.type);
        typeDinas.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                switch(checkedId) {
                    case R.id.pagi:
                        typeText = "Pagi";
                        break;
                    case R.id.sore:
                        typeText = "Sore";
                        break;

                }
            }
        });

        jenisDinas = (RadioGroup)findViewById(R.id.jenis);
        jenisDinas.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                switch(checkedId) {
                    case R.id.dalam_dinas:
                        jenisText = "dalam_dinas";
                        typeDinas.setVisibility(View.VISIBLE);
                        break;
                    case R.id.luar_dinas:
                        jenisText = "luar_dinas";
                        typeDinas.setVisibility(View.GONE);
                        break;

                }
            }
        });
    }

    public void takePicture()
    {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        Intent i;
        i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File pictureFile = null;
        try {
            pictureFile = getOutputMediaFile();
        } catch (IOException ex) {
            Toast.makeText(InputDinasActivity.this,
                    "Photo file can't be created, please try again",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        filePhoto = Uri.fromFile(pictureFile);
        Uri photoUri = FileProvider.getUriForFile(InputDinasActivity.this,
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
                        Toast.makeText(InputDinasActivity.this,
                                "Photo file can't be created, please try again",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    filePhoto2 = Uri.fromFile(pictureFile);
                    Uri photoUri = FileProvider.getUriForFile(InputDinasActivity.this,
                            "com.waroengweb.absensi.provider",pictureFile
                    );
                    i.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                        i.putExtra("android.intent.extras.LENS_FACING_FRONT", 1);
                    } else {
                        i.putExtra("android.intent.extras.CAMERA_FACING", 1);
                    }
                    i.putExtra("android.intent.extra.USE_FRONT_CAMERA", true);
                    startActivityForResult(i,203);

                } else if (options[item].equals("Pilih Dari Galeri")) {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto, 204);//one can be replaced with any action code

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
                fileString = compressImage(filePhoto).toString();
                imagePhoto.setImageURI(Uri.parse(fileString));
                imagePhoto.requestFocus();
                takePicture.setText("Ganti Photo");
                try {
                    ExifHelper.copyExif(filePhoto.getPath(), fileString);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }  else if (requestCode == 202) {
                File imageFile = new File(getRealPathFromURI(data.getData()));
                fileString = compressImage(Uri.fromFile(imageFile)).toString();
                imagePhoto.setImageURI(Uri.parse(fileString));
                takePicture.setText("Ganti Photo");
            } else if (requestCode == 203){
                fileString2 = compressImage(filePhoto2).toString();
                imagePhoto2.setImageURI(Uri.parse(fileString2));
                imagePhoto2.requestFocus();
                takePicture2.setText("Ganti Photo");
                try {
                    ExifHelper.copyExif(filePhoto2.getPath(), fileString2);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }  else if (requestCode == 204) {
                File imageFile = new File(getRealPathFromURI(data.getData()));
                fileString2 = compressImage(Uri.fromFile(imageFile)).toString();
                imagePhoto2.setImageURI(Uri.parse(fileString2));
                takePicture2.setText("Ganti Photo");
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

            if (fileString == null) {
                Alerter.create(this).setTitle("ERROR").setText("BELUM AMBIL PHOTO..").setBackgroundColorInt(Color.RED).show();
                return;
            }

            Dinas dinas = new Dinas();
            dinas.setNip(nip.getText().toString());
            dinas.setApproved(0);
            dinas.setUploaded(0);
            dinas.setFoto(fileString);
            dinas.setFotoBerkas(fileString2);
            dinas.setTypeDinas(typeText);
            dinas.setTanggal(new Date());
            dinas.setTanggal2(new Date());
            dinas.setJenisDinas(jenisText);


            db.DinasDao().insertDinas(dinas);
            Toast.makeText(this, "DATA BERHASIL DISIMPAN", Toast.LENGTH_SHORT).show();
            clearDinas();

        }
    }

    private void clearDinas()
    {
        nip.setText("");

        fileString = null;
        fileString2 = null;
        takePicture.setText("Photo/Gambar");
        takePicture2.setText("Photo/Gambar");
        imagePhoto.setImageDrawable(getResources().getDrawable(R.drawable.index));
        imagePhoto2.setImageDrawable(getResources().getDrawable(R.drawable.doc));
    }
}
