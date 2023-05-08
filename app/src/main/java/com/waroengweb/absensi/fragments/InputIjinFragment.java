package com.waroengweb.absensi.fragments;


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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;
import com.google.android.material.textfield.TextInputLayout;
import com.tapadoo.alerter.Alerter;
import com.waroengweb.absensi.InputCutiActivity;
import com.waroengweb.absensi.R;
import com.waroengweb.absensi.database.AppDatabase;
import com.waroengweb.absensi.database.entity.Ijin;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;
import static com.basgeekball.awesomevalidation.ValidationStyle.TEXT_INPUT_LAYOUT;

import id.zelory.compressor.Compressor;

/**
 * A simple {@link Fragment} subclass.
 */
public class InputIjinFragment extends Fragment {

    TextInputLayout txtTgl;
    RadioGroup jenisGroup;
    Button takePicture,saveData,takePicture2;
    ImageView imagePhoto,imagePhoto2;
    Uri filePhoto,filePhoto2;
    String fileString,fileString2,typeText="Penuh",jenisText="Izin";
    AppDatabase db;
    AutoCompleteTextView nip;
    private AwesomeValidation validation;
    Calendar myCalendar;
    DatePickerDialog datePicker;

    public InputIjinFragment() {
        // Required empty public constructor
    }

   public static InputIjinFragment newInstance() {
        return new InputIjinFragment();
   }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_input_ijin, container, false);

        myCalendar = Calendar.getInstance();

        imagePhoto = (ImageView)v.findViewById(R.id.preview);
        imagePhoto2 = (ImageView)v.findViewById(R.id.preview2);

        txtTgl = (TextInputLayout) v.findViewById(R.id.tanggal_lbl);

        txtTgl.getEditText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (jenisText == "Izin") {
                    setDatePicker(0);
                } else {
                    setDatePicker(3);
                }
            }
        });

        jenisGroup = (RadioGroup)v.findViewById(R.id.jenis_group);
        jenisGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                switch(checkedId) {
                    case R.id.ijin:
                        jenisText = "Izin";

                        break;
                    case R.id.sakit:
                        jenisText = "Sakit";

                        break;
                }
            }
        });

        db = Room.databaseBuilder(getActivity(),
                AppDatabase.class, "MyDB").allowMainThreadQueries().fallbackToDestructiveMigration().build();

        nip = (AutoCompleteTextView)v.findViewById(R.id.nip);
        List<String> NIP = db.AbsenDao().getNip();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_dropdown_item_1line, NIP);
        nip.setAdapter(adapter);

        takePicture = (Button)v.findViewById(R.id.take_picture);
        takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture();
            }
        });

        takePicture2 = (Button)v.findViewById(R.id.take_picture2);
        takePicture2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture2();
            }
        });

        saveData = (Button)v.findViewById(R.id.save_data);
        saveData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
            }
        });



        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        validation = new AwesomeValidation(TEXT_INPUT_LAYOUT);
        AwesomeValidation.disableAutoFocusOnFirstFailure();
        validation.addValidation(getActivity(),R.id.nip_lbl, RegexTemplate.NOT_EMPTY,R.string.required);
    }

    public void takePicture()
    {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            Intent i;
            i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File pictureFile = null;
            try {
                pictureFile = getOutputMediaFile();
            } catch (IOException ex) {
                Toast.makeText(getActivity(),
                        "Photo file can't be created, please try again",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            filePhoto = Uri.fromFile(pictureFile);
            Uri photoUri = FileProvider.getUriForFile(getActivity(),
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

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
                        Toast.makeText(getActivity(),
                                "Photo file can't be created, please try again",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    filePhoto2 = Uri.fromFile(pictureFile);
                    Uri photoUri = FileProvider.getUriForFile(getActivity(),
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

    public  File getOutputMediaFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String mFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
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
        Cursor cursor = getActivity().getContentResolver().query(contentURI, null, null, null, null);
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
            compressFile = new Compressor(getContext()).compressToFile(new File(fileData.getPath()));
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
                Alerter.create(getActivity()).setTitle("ERROR").setText("FORMAT NIP SALAH").setBackgroundColorInt(Color.RED).show();
                return;
            }

            if (fileString == null) {
                Alerter.create(getActivity()).setTitle("ERROR").setText("BELUM AMBIL PHOTO..").setBackgroundColorInt(Color.RED).show();
                return;
            }

            if (fileString2 == null) {
                Alerter.create(getActivity()).setTitle("ERROR").setText("BELUM AMBIL PHOTO KETERANGAN").setBackgroundColorInt(Color.RED).show();
                return;
            }

            Ijin ijin = new Ijin();
            ijin.setNip(nip.getText().toString());
            ijin.setApproved(0);
            ijin.setUploaded(0);
            ijin.setFoto(fileString);
            ijin.setKeterangan(fileString2);
            ijin.setJenisIjin(jenisText);
            ijin.setTypeIjin(typeText);
            ijin.setLamaHari(1);

            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date tanggalNew;
            String tanggal = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            try {
                tanggalNew = formatter.parse(tanggal);
                ijin.setTanggal(tanggalNew);
                ijin.setTanggal2(tanggalNew);
                ijin.setTanggalInput(new Date());
            } catch (ParseException pe) {
                pe.printStackTrace();
            }

            db.IjinDao().insertIjin(ijin);
            Toast.makeText(getActivity(), "DATA BERHASIL DISIMPAN", Toast.LENGTH_SHORT).show();
            //new DataIjinFragment().prepareIjinData(-30);
            clearIjin();
        }
    }

    private void clearIjin()
    {
        nip.setText("");
        fileString = null;
        fileString2 = null;
        takePicture.setText("Ambil Photo");
        takePicture2.setText("Photo/Gambar");
        imagePhoto.setImageDrawable(getResources().getDrawable(R.drawable.index));
        imagePhoto2.setImageDrawable(getResources().getDrawable(R.drawable.doc));

    }

    private void setDatePicker(int number)
    {
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
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
                        Toast.makeText(getActivity(),"Hanya bisa input dibulan yang sama",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    txtTgl.getEditText().setText(sdf.format(myCalendar.getTime()));


            }
        }, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH)-number);
        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        datePickerDialog.getDatePicker().setMaxDate(myCalendar.getTimeInMillis());
        datePickerDialog.show();
    }

}
