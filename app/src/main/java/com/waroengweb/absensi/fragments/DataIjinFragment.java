package com.waroengweb.absensi.fragments;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.waroengweb.absensi.IjinActivity;
import com.waroengweb.absensi.R;
import com.waroengweb.absensi.adapter.IjinAdapter;
import com.waroengweb.absensi.database.AppDatabase;
import com.waroengweb.absensi.database.entity.Ijin;
import com.waroengweb.absensi.radioButton.PresetRadioGroup;
import com.waroengweb.absensi.radioButton.PresetValueButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class DataIjinFragment extends Fragment {

    public static AppDatabase db;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    IjinAdapter ijinAdapter;
    PresetRadioGroup mSetDurationPresetRadioGroup;
    List<Ijin> ijinList = new ArrayList<>();

    public DataIjinFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_data_ijin, container, false);

        recyclerView = (RecyclerView) v.findViewById(R.id.recycleViewContainer2);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        db = Room.databaseBuilder(getActivity(),
                AppDatabase.class, "MyDB").allowMainThreadQueries().fallbackToDestructiveMigration().build();

        mSetDurationPresetRadioGroup = (PresetRadioGroup) v.findViewById(R.id.preset_time_radio_group);

        mSetDurationPresetRadioGroup.setOnCheckedChangeListener(new PresetRadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View radioGroup, View radioButton, boolean isChecked, int checkedId) {
                //Toast.makeText(DataPresensi.this,"Value : "+((PresetValueButton) radioButton).getValue(),Toast.LENGTH_SHORT).show();
                //mSetDurationEditText.setSelection(mSetDurationEditText.getText().length());
                String value = ((PresetValueButton) radioButton).getValue();
                int days;
                switch (value) {
                    case "30 Hari" :
                        days = -30;
                        break;
                    default :
                        days = -30;
                        break;
                }

                prepareIjinData(days);
            }
        });

        prepareIjinData(-30);

        ((IjinActivity)getActivity()).setFragmentRefreshListener(new IjinActivity.FragmentRefreshListener() {
            @Override
            public void onRefresh() {
                prepareIjinData(-30);
            }
        });

        return v;
    }

    public void prepareIjinData(int days) {

        if (days == 0){
            ijinList = db.IjinDao().getAllIjin();
            //absenList.clear();
        } else {
            String dateFormat = "yyyy-MM-dd HH:mm:ss";
            SimpleDateFormat format = new SimpleDateFormat(dateFormat);
            Date dateStart;
            try {
                dateStart = format.parse(getCalculatedDate(dateFormat, days));
                ijinList = db.IjinDao().getAllIjinBetween(dateStart.getTime(), new Date().getTime());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }



        ijinAdapter = new IjinAdapter(getActivity(),ijinList);
        recyclerView.setAdapter(ijinAdapter);
        //mAdapter.notifyDataSetChanged();
        runLayoutAnimation(recyclerView);

    }

    public static String getCalculatedDate(String dateFormat, int days) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat s = new SimpleDateFormat(dateFormat);
        cal.add(Calendar.DAY_OF_YEAR, days);
        return s.format(new Date(cal.getTimeInMillis()));
    }

    private void runLayoutAnimation(final RecyclerView recyclerView) {
        final Context context = recyclerView.getContext();
        final LayoutAnimationController controller =
                AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_from_bottom);

        recyclerView.setLayoutAnimation(controller);
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }

}
