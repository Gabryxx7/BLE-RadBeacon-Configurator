package com.unimelb.marinig.bletracker.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.unimelb.marinig.bletracker.Model.TrackerPreferences;
import com.unimelb.marinig.bletracker.R;

import java.util.ArrayList;
import java.util.List;

public class SortPopupWindow extends RelativePopupWindow {
    private Context context;
    private RecyclerView mRecyclerView;
    private SortDropdownAdapter dropdownAdapter;

    public SortPopupWindow(Context context){
        super(context);
        this.context = context;
        setupView();
    }

    public void setSortSelectedListener(SortSelectedListener sortSelectedListener) {
        dropdownAdapter.setSortSelectedListener(sortSelectedListener);
    }

    private void setupView() {
        View view = LayoutInflater.from(context).inflate(R.layout.popup_menu, null);

        mRecyclerView = view.findViewById(R.id.popup_rv);
        //mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        //mRecyclerView.addItemDecoration(new DividerItemDecoration(context, LinearLayoutManager.VERTICAL));

        dropdownAdapter = new SortDropdownAdapter();
        mRecyclerView.setAdapter(dropdownAdapter);

        setContentView(view);
    }


    public interface SortSelectedListener {
        void onSortSelected(int position, SortItem sortOption, TextView view);
    }

    public class SortDropdownAdapter extends RecyclerView.Adapter<SortDropdownAdapter.SortViewHolder> {

        private List<SortItem> sortOptions;
        private SortSelectedListener sortSelectedListener;

        public SortDropdownAdapter(){
            super();

            this.sortOptions = new ArrayList<>();
            sortOptions.add(new SortItem(TrackerPreferences.Ordering.ORDER_NAME, "Name"));
            sortOptions.add(new SortItem(TrackerPreferences.Ordering.ORDER_RSSI, "RSSI"));
            sortOptions.add(new SortItem(TrackerPreferences.Ordering.ORDER_MAC, "Mac addr."));
            sortOptions.add(new SortItem(TrackerPreferences.Ordering.ORDER_MAJOR, "Major"));
            sortOptions.add(new SortItem(TrackerPreferences.Ordering.ORDER_MINOR, "Minor"));
            sortOptions.add(new SortItem(TrackerPreferences.Ordering.ORDER_TYPE, "Type"));
            sortOptions.add(new SortItem(TrackerPreferences.Ordering.ORDER_TIMESCAN, "Time Scan"));
        }


        public void setSortSelectedListener(SortSelectedListener sortSelectedListener) {
            this.sortSelectedListener = sortSelectedListener;
        }

        @NonNull
        @Override
        public SortViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new SortViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.sort_dropdown_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull SortViewHolder holder, final int position) {
            final SortItem sortOption = sortOptions.get(position);
            int col = Color.parseColor("#696969");
            Drawable draw = null;
            if(sortOption.isSelected) {
                col = Color.parseColor("#FFA500");

                draw = context.getDrawable(R.drawable.ic_arrow_downward_black_24dp);
                if(sortOption.sortOrder != 0){
                    draw = context.getDrawable(R.drawable.ic_arrow_upward_black_24dp);
                }

                draw = DrawableCompat.wrap(draw);
                DrawableCompat.setTint(draw, col);
            }

            holder.mSortOptionTextView.setCompoundDrawablesWithIntrinsicBounds(draw, null, null, null);
            holder.mSortOptionTextView.setTextColor(col);
            holder.mSortOptionTextView.setText(sortOption.sortItemTitle);
            holder.mSortOptionTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    for(int i = 0; i < sortOptions.size(); i++){
                        if(i == position)
                            continue;
                        sortOptions.get(i).isSelected = false;
                    }
                    sortOption.isSelected = true;
                    if(sortSelectedListener != null){
                        sortSelectedListener.onSortSelected(position, sortOption, (TextView) view);
                    }
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return sortOptions.size();
        }

        public class SortViewHolder extends RecyclerView.ViewHolder{
            AppCompatTextView mSortOptionTextView;

            public SortViewHolder(View itemView) {
                super(itemView);
                mSortOptionTextView = itemView.findViewById(R.id.sort_option_text);
            }
        }

    }

    public class SortItem {
        public int sortId;
        public String sortItemTitle;
        public byte sortOrder = 0;
        public boolean isSelected = false;

        public SortItem(int id , String title){
            super();
            this.sortId = id;
            this.sortItemTitle = title;
        }
    }
}
