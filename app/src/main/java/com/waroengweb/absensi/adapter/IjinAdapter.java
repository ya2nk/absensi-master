package com.waroengweb.absensi.adapter;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.bumptech.glide.Glide;
import com.waroengweb.absensi.R;
import com.waroengweb.absensi.database.AppDatabase;
import com.waroengweb.absensi.database.entity.Ijin;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class IjinAdapter extends RecyclerView.Adapter<IjinAdapter.MyViewHolder> {

    public static List<Ijin> ijinList;
    private Context context;
    private AppDatabase db;

    public IjinAdapter(Context context,List<Ijin> ijinList){
        this.context = context;
        this.ijinList = ijinList;
        db = Room.databaseBuilder(context,
                AppDatabase.class, "MyDB").allowMainThreadQueries().fallbackToDestructiveMigration().build();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView nip,tanggal,status,approved,ket;
        ImageView userImg,delBtn;
        public MyViewHolder(View view){
            super(view);
            nip = (TextView)view.findViewById(R.id.nipText);
            tanggal = (TextView)view.findViewById(R.id.tanggalText);
            ket = (TextView)view.findViewById(R.id.keteranganText);
            userImg = (ImageView)view.findViewById(R.id.userImg);
            status = (TextView)view.findViewById(R.id.statusText);
            approved = (TextView)view.findViewById(R.id.approveText);
            delBtn = (ImageView)view.findViewById(R.id.delBtn);

        }
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_list_item2, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        //holder.mImage.setImageResource(listdata.get(position).getThubnail());
        Date tanggalDb = ijinList.get(position).getTanggal();
        String tanggal = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(tanggalDb);
        Date tanggalDb2 = ijinList.get(position).getTanggal2();
        String tanggal2 = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(tanggalDb2);


        holder.nip.setText(ijinList.get(position).getNip());
        holder.tanggal.setText("("+tanggal+") - ("+tanggal2+") ("+ijinList.get(position).getJenisIjin()+")");
        holder.ket.setText(ijinList.get(position).getTypeIjin());

        final RecyclerView.ViewHolder x=holder;
        Glide.with(context)
                .load(ijinList.get(position).getFoto())
                .into(holder.userImg);

        String statusText = ijinList.get(position).getUploaded() == 1 ? "Sudah Dikirim" : "Belum Dikirim";
        String approveText = ijinList.get(position).getApproved() == 1 ? "Disetujui" : "Belum Disetujui";
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
                Ijin ijin = ijinList.get(position);
                if (ijin.getUploaded() == 0){
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                    builder1.setMessage("Apakah Yakin menghapus data ini?.");
                    builder1.setCancelable(true);

                    builder1.setPositiveButton(
                            "Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    db.IjinDao().deleteIjin(ijin.getId());
                                    ijinList.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position,ijinList.size());
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

    }

    @Override
    public int getItemCount() {
        return ijinList.size();
    }


    
}
