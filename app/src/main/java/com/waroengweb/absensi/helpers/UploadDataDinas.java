package com.waroengweb.absensi.helpers;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.tapadoo.alerter.Alerter;
import com.waroengweb.absensi.DinasActivity;
import com.waroengweb.absensi.app.AppController;
import com.waroengweb.absensi.app.VolleyMultipartRequest;
import com.waroengweb.absensi.database.AppDatabase;
import com.waroengweb.absensi.database.entity.Dinas;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UploadDataDinas {
    private Context context;
    private AppCompatActivity activity;
    private ProgressDialog pd;
    public static AppDatabase db;
    private String url = "https://siap.kerincikab.go.id/";

    public UploadDataDinas(Context context, AppCompatActivity activity)
    {
        this.context = context;
        this.activity = activity;

        pd = new ProgressDialog(context);
        pd.setMessage("Upload Data...");
        db = Room.databaseBuilder(context,
                AppDatabase.class, "MyDB").allowMainThreadQueries().fallbackToDestructiveMigration().build();
    }

    public void setUploadData(Dinas dinas)
    {
        pd.show();
        final List<Dinas> listDinas;
        if (dinas.getNip() == null){
            listDinas = db.DinasDao().getAllDinasNotUploaded();
        } else {
            listDinas = new ArrayList<>();
            listDinas.add(dinas);
        }



        VolleyMultipartRequest mReq = new VolleyMultipartRequest(Request.Method.POST, url+"api/save_dinas",
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        pd.dismiss();
                        AppController.getInstance().getRequestQueue().getCache().clear();

                        if (response.statusCode == 200){
                            String result = new String(response.data);
                            //Toast.makeText(context,result,Toast.LENGTH_SHORT).show();

                            try {
                                JSONObject json = new JSONObject(result);
                                if (json.getString("result").equals("success")){


                                    JSONArray arrayId = json.getJSONArray("ids");
                                    if (arrayId.length() != 0){
                                        for (int i=0;i<arrayId.length();i++){
                                            int id = arrayId.getInt(i);
                                            db.DinasDao().updateDinas(id);


                                        }

                                        Alerter.create(activity).setTitle("INFO").setText("Data Berhasil Diupload")
                                                .setBackgroundColorInt(Color.GREEN).show();
                                    } else {
                                        JSONArray arrayError = json.getJSONArray("error");
                                        if (arrayError.length() == 0){
                                            Alerter.create(activity).setTitle("ERROR").setText("Data Gagal Diupload")
                                                    .setBackgroundColorInt(Color.RED).show();
                                        } else {
                                            Alerter.create(activity).setTitle("ERROR").setText(arrayError.getString(0))
                                                    .setBackgroundColorInt(Color.RED).show();
                                        }
                                    }


                                    try {
                                        JSONArray jArray = json.getJSONArray("approved");
                                        if (jArray.length() != 0){
                                            for (int i=0;i<jArray.length();i++){
                                                int id = jArray.getInt(i);
                                                db.DinasDao().updateDinasApproved(id);

                                            }
                                        }
                                    } catch (JSONException e){
                                        e.printStackTrace();
                                    }

                                    ((DinasActivity) activity).prepareDinasData(-30);


                                } else {
                                    Toast.makeText(context,result,Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e){
                                e.printStackTrace();
                            }


                            Log.d("response :",new String(response.data));
                        } else {
                            Toast.makeText(context,"ERROR",Toast.LENGTH_SHORT).show();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pd.dismiss();

                if (error instanceof NoConnectionError){
                    Toast.makeText(context,"Tidak Ada Jaringan Internet",Toast.LENGTH_SHORT).show();
                } else if (error instanceof TimeoutError) {
                    Toast.makeText(context,"Jaringan Internet Bermasalah Silakan coba beberapa saat lagi",Toast.LENGTH_SHORT).show();
                } else {
                    //try(error.networkResponse.data!=null) {
                    String body;
                    try {
                        body = new String(error.networkResponse.data,"UTF-8");
                        Toast.makeText(context,body,Toast.LENGTH_SHORT).show();
                        Log.d("Error :",body);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }

            }
        }){
            @Override
            protected Map<String,String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                Gson gson = new Gson();
                params.put("data",gson.toJson(listDinas));
                List<Dinas> listApproved = db.DinasDao().getAllDinasUploadedAndNotApprove();
                params.put("data_approved",gson.toJson(listApproved));
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() throws AuthFailureError {
                Map<String, DataPart> params = new HashMap<>();

                for (Dinas dinas : listDinas){
                    long imagename = System.currentTimeMillis();


                    if (dinas.getFoto() != null){
                        params.put("photo["+dinas.getId()+"]", new DataPart("PIC_1_"+String.valueOf(imagename)+".jpg",convertImageToBytes(dinas.getFoto())));
                    }


                }

                return params;
            }
        };

        mReq.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


        AppController.getInstance().addToRequestQueue(mReq);
    }

    public  byte[] convertImageToBytes(String path) {
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        return stream.toByteArray();
    }
}
