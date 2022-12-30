package com.waroengweb.absensi.adapter;


import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.bumptech.glide.Glide;
import com.waroengweb.absensi.R;
import com.waroengweb.absensi.database.AppDatabase;
import com.waroengweb.absensi.database.entity.Absen;
import com.waroengweb.absensi.helpers.UploadDataAbsen;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AbsenAdapter extends RecyclerView.Adapter<AbsenAdapter.MyViewHolder> {

    public static List<Absen> absenList;
    private Context context;
    private AppDatabase db;
    private AppCompatActivity activity;

    public AbsenAdapter(Context context,List<Absen> absenList,AppCompatActivity activity){
        this.context = context;
        this.absenList = absenList;
        db = Room.databaseBuilder(context,
                AppDatabase.class, "MyDB").allowMainThreadQueries().fallbackToDestructiveMigration().build();
        this.activity = activity;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView nip,tanggal,status,approved;
        ImageView userImg,delBtn;
        Button upload;
        public MyViewHolder(View view){
            super(view);
            nip = (TextView)view.findViewById(R.id.nipText);
            tanggal = (TextView)view.findViewById(R.id.tanggalText);
            userImg = (ImageView)view.findViewById(R.id.userImg);
            status = (TextView)view.findViewById(R.id.statusText);
            approved = (TextView)view.findViewById(R.id.approveText);
            delBtn = (ImageView)view.findViewById(R.id.delBtn);
            upload = (Button)view.findViewById(R.id.btnUpload);
        }
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_list_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        //holder.mImage.setImageResource(listdata.get(position).getThubnail());
        Date tanggalDb = absenList.get(position).getTanggal();
        String tanggal = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(tanggalDb);
        String[] dayName = new String[]{"Minggu","Senin","Selasa","Rabu","Kamis","Jumat","Sabtu"};

        //String dayWeek = new SimpleDateFormat("E", Locale.getDefault()).format(tanggalDb);
        Calendar calendar = Calendar.getInstance();
		calendar.setTime(tanggalDb);
        int dayWeek = calendar.get(Calendar.DAY_OF_WEEK);
        String inOut = "Pulang";
        String waktu = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(tanggalDb);
        String endWaktu = "12:00";
        if (dayWeek== 6){
            endWaktu = "09:00";
        }

        if (checktimings(waktu,endWaktu)){
            inOut = "Datang";
        }

        if (checktimings("13:00",waktu) && checktimings(waktu,"13:30")){
            inOut = "Siang";
        }



        holder.nip.setText(absenList.get(position).getNip());
        holder.tanggal.setText(dayName[dayWeek-1]+" "+tanggal+" ( "+inOut+" )");

        final RecyclerView.ViewHolder x=holder;
        Glide.with(context)
                .load(absenList.get(position).getFoto())
                .into(holder.userImg);

        String statusText = absenList.get(position).getUploaded() == 1 ? "Sudah Dikirim" : "Belum Dikirim";
        String approveText = absenList.get(position).getApproved() == 1 ? "Disetujui" : "Belum Disetujui";
        holder.status.setText(statusText);
        holder.approved.setText(approveText);
        if(statusText.equals("Belum Dikirim")){
            holder.status.setTextColor(Color.parseColor("red"));
        } else {
            holder.status.setTextColor(Color.parseColor("green"));
        }

        if(approveText.equals("Belum Disetujui")){
            holder.approved.setTextColor(Color.parseColor("red"));
        } else {
            holder.approved.setTextColor(Color.parseColor("green"));
        }

        holder.delBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(context,"click "+ijinList.get(position).getId(),Toast.LENGTH_SHORT).show();
                Absen absen = absenList.get(position);
                if (absen.getUploaded() == 0){
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                    builder1.setMessage("Apakah Yakin menghapus data ini?.");
                    builder1.setCancelable(true);

                    builder1.setPositiveButton(
                            "Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    db.AbsenDao().deleteAbsen(absen.getId());
                                    absenList.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position,absenList.size());
                                    dialog.cancel();
                                }

                                //
                            });
                    builder1.setNegativeButton(
                            "No",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                }
            }
        });

        if (absenList.get(position).getUploaded() == 1){
            holder.upload.setVisibility(View.GONE);
        }

        if (absenList.get(position).getUploaded() == 0) {

            holder.upload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new UploadDataAbsen(context, activity).setUploadData(absenList.get(position));
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return absenList.size();
    }

    private boolean checktimings(String time, String endtime) {

        String pattern = "HH:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);

        try {
            Date date1 = sdf.parse(time);
            Date date2 = sdf.parse(endtime);

            if(date1.before(date2)) {
                return true;
            } else {
                return false;
            }
        } catch (ParseException e){
            e.printStackTrace();
        }
        return false;
    }
}
