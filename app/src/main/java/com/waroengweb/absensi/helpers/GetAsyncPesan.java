package com.waroengweb.absensi.helpers;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.room.Room;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.waroengweb.absensi.DetailPesanActivity;
import com.waroengweb.absensi.LoginActivity;
import com.waroengweb.absensi.MainActivity;
import com.waroengweb.absensi.PesanActivity;
import com.waroengweb.absensi.R;
import com.waroengweb.absensi.app.AppController;
import com.waroengweb.absensi.database.AppDatabase;
import com.waroengweb.absensi.database.entity.Pesan;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetAsyncPesan {

    Context context;
    AppCompatActivity activity;
    public static AppDatabase db;
    ProgressDialog pd;
    private String activityName;

    public GetAsyncPesan(Context context, AppCompatActivity activity)
    {
        this.context = context;
        this.activity = activity;
        db = Room.databaseBuilder(context,
                AppDatabase.class, "MyDB").allowMainThreadQueries().fallbackToDestructiveMigration().build();
        pd = new ProgressDialog(activity);
        pd.setMessage("Loading...");
        activityName = context.getClass().getSimpleName();
    }

    public void doTask()
    {
        if (NetworkHelper.isNetworkAvailable(context)) {
            MyAsyncTasks myAsyncTasks = new MyAsyncTasks();
            List<Integer> idPesans = db.PesanDao().getAllIdPesan();
            myAsyncTasks.execute(idPesans);
        }
    }

    public class MyAsyncTasks extends AsyncTask<List<Integer>, Pesan, String>
    {
        //@Override
        protected void onPreExecute()
        {
            //super.onPreExecute();
            if(activityName.equals("PesanActivity")){
                pd.show();
                //Toast.makeText(context,activityName,Toast.LENGTH_SHORT).show();
            }
        }

        //@Override
        protected String doInBackground(List<Integer>... params)
        {

            //super.doInBackground()
            String url = "https://siap.kerincikab.go.id/";
            StringRequest postRequest = new StringRequest(Request.Method.POST, url+"api/get-pengumuman",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // response
                            Log.d("Response", response);
                            try {
                                JSONObject json = new JSONObject(response);
                                if (json.getString("result").equals("success")) {
                                    JSONArray rows = json.getJSONArray("rows");
                                    if (rows.length() > 0){
                                        for(int id=0;id<rows.length();id++){
                                            JSONObject row = rows.getJSONObject(id);
                                            Pesan pesan = new Pesan();
                                            pesan.setDari(row.getString("dari"));
                                            pesan.setJudul(row.getString("judul"));
                                            pesan.setPesan(row.getString("pesan"));
                                            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                            try{
                                                pesan.setTanggal(formatter.parse(row.getString("tanggal")));
                                            } catch (ParseException pe){
                                                pe.printStackTrace();
                                            }

                                            pesan.setIdPesan(row.getInt("id"));

                                            Pesan pesanDb = db.PesanDao().getPesanRow(row.getInt("id"));
                                            if (pesanDb == null){
                                                db.PesanDao().insertPesan(pesan);
                                                publishProgress(pesan);
                                            }


                                        }
                                    }
                                }
                            } catch(JSONException e){
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // error
                            Log.d("Error.Response", error.toString());
                        }
                    }
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> paramsPost = new HashMap<String, String>();
                    //List<Integer> idPesans = db.PesanDao().getAllIdPesan();
                    Gson gson = new Gson();
                    paramsPost.put("idPesan",gson.toJson(params[0]));
                    return paramsPost;
                }
            };
            AppController.getInstance().addToRequestQueue(postRequest);
            //}
            return "complete";
        }

        protected void onProgressUpdate(Pesan... pesan)
        {

            String NOTIFICATION_CHANNEL_ID = "my_channel_id_01"; // We'll add Notification Channel Id using NotificationManager.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_HIGH);
                // Configure the notification channel.
                notificationChannel.setDescription("Channel description");
                notificationChannel.enableLights(true);
                notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
                notificationChannel.enableVibration(true);
                notificationManager.createNotificationChannel(notificationChannel);
            }

            //Bundle bundle = new Bundle();
            //bundle.putInt("idPesan", pesan[0].getId());

            Intent intent;
            if (Session.getLoginStatus(context)) {
                intent = new Intent(context, DetailPesanActivity.class);
                intent.putExtra("id",pesan[0].getIdPesan());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            } else {
                intent = new Intent(context, LoginActivity.class);
            }



            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                    PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
            notificationBuilder.setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.alerter_ic_notifications)
                    .setAutoCancel(true) // menghapus notif ketika user melakukan tap pada notif
                    .setLights(200,200,200) // light button
                    .setContentTitle(pesan[0].getDari())
                    .setContentText(pesan[0].getJudul())
                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI) // set sound
                    .setOnlyAlertOnce(true) // set alert sound notif
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(pesan[0].getJudul()))
                    .setContentIntent(pendingIntent);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationManager.notify(/*notification id*/pesan[0].getId(), notificationBuilder.build());
            }



        }

        @Override
        protected void onPostExecute(String s) {
            //counter.setText(String.valueOf(db.PesanDao().getUnreadPesan()));
            if (activityName.equals("MainActivity")){
                ((MainActivity) activity).setCounter();
            }

            if (activityName.equals("PesanActivity")){
                ((PesanActivity) activity).getInbox("");
                pd.dismiss();
            }
        }
    }
}
