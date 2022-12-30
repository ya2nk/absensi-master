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
import com.waroengweb.absensi.CutiActivity;
import com.waroengweb.absensi.IjinActivity;
import com.waroengweb.absensi.app.AppController;
import com.waroengweb.absensi.app.VolleyMultipartRequest;
import com.waroengweb.absensi.database.AppDatabase;
import com.waroengweb.absensi.database.entity.Ijin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UploadData {
    private Context context;
    private AppCompatActivity activity;
    private ProgressDialog pd;
    public static AppDatabase db;
    private String url = "https://siap.kerincikab.go.id/";

    public UploadData(Context context,AppCompatActivity activity)
    {
        this.context = context;
        this.activity = activity;

        pd = new ProgressDialog(context);
        pd.setMessage("Upload Data...");
        db = DBHelper.builder(context);
    }

    public void setUploadData(String jenis)
    {
        pd.show();
        final List<Ijin> listIjin;
        if (jenis == "Ijin") {
            listIjin = db.IjinDao().getAllIjinNotUploaded();
        } else {
            listIjin = db.IjinDao().getAllCutiNotUploaded();
        }

        VolleyMultipartRequest mReq = new VolleyMultipartRequest(Request.Method.POST, url+"api/save_ijin",
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
                                            db.IjinDao().updateIjin(id);


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
                                                db.IjinDao().updateIjinApproved(id);

                                            }
                                        }
                                    } catch (JSONException e){
                                        e.printStackTrace();
                                    }
                                    if (jenis =="Ijin") {
                                        if (((IjinActivity) activity).getFragmentRefreshListener() != null) {
                                            ((IjinActivity) activity).getFragmentRefreshListener().onRefresh();
                                        }
                                    } else {
                                        ((CutiActivity) activity).prepareCutiData(-30);
                                    }

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
                params.put("data",gson.toJson(listIjin));
                List<Ijin> listApproved = db.IjinDao().getAllIjinUploadedAndNotApprove();
                params.put("data_approved",gson.toJson(listApproved));
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() throws AuthFailureError {
                Map<String, DataPart> params = new HashMap<>();

                for (Ijin ijin : listIjin){
                    long imagename = System.currentTimeMillis();


                    if (ijin.getFoto() != null){
                        params.put("photo["+ijin.getId()+"]", new DataPart("PIC_1_"+String.valueOf(imagename)+".jpg",convertImageToBytes(ijin.getFoto())));
                    }

                    if (ijin.getKeterangan() != null){
                        params.put("keterangan["+ijin.getId()+"]", new DataPart("PIC_2_"+String.valueOf(imagename)+".jpg",convertImageToBytes(ijin.getKeterangan())));
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
