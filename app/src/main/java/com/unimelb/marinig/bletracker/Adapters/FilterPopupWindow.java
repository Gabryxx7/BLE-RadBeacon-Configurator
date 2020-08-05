package com.unimelb.marinig.bletracker.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.unimelb.marinig.bletracker.Model.TrackerPreferences;
import com.unimelb.marinig.bletracker.R;

import java.util.ArrayList;
import java.util.List;

public class FilterPopupWindow extends RelativePopupWindow {
    private Context context;
    private RecyclerView mRecyclerView;
    private FilterDropdownAdapter dropdownAdapter;

    public FilterPopupWindow(Context context){
        super(context);
        this.context = context;
        setupView(new TrackerPreferences(context));
    }

    public void setFilterClickedListener(FilterClickedListener filterClickedListener) {
        dropdownAdapter.setFilterClickedListener(filterClickedListener);
    }

    private void setupView(TrackerPreferences preferences) {
        View view = LayoutInflater.from(context).inflate(R.layout.popup_menu, null);

        mRecyclerView = view.findViewById(R.id.popup_rv);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        //mRecyclerView.addItemDecoration(new DividerItemDecoration(context, LinearLayoutManager.VERTICAL));

        dropdownAdapter = new FilterDropdownAdapter(preferences);
        mRecyclerView.setAdapter(dropdownAdapter);

        setContentView(view);
    }


    public interface FilterClickedListener {
        void onFilterSelected(int position, FilterDropdownAdapter.FilterMenuItem filter, boolean isChecked);
    }


    public class FilterDropdownAdapter extends RecyclerView.Adapter<FilterDropdownAdapter.FilterViewHolder> {

        public class FilterMenuItem{
            public String id;
            public String filterTitle;

            public FilterMenuItem(String id, String filterTitle){
                super();
                this.id = id;
                this.filterTitle = filterTitle;
            }
        }

        private List<FilterMenuItem> mFilters;
        private FilterClickedListener filterClickedListener;
        private TrackerPreferences mPreferences;
        public FilterDropdownAdapter(TrackerPreferences preferences){
            super();
            mPreferences = preferences;
            mFilters = new ArrayList<>();
            mFilters.add(new FilterMenuItem(TrackerPreferences.Settings.FILTER_IBEACONS, "iBeacons"));
            mFilters.add(new FilterMenuItem(TrackerPreferences.Settings.FILTER_CONFIGURABLE_RADBEACONS, "Config. Radbeacons"));
            mFilters.add(new FilterMenuItem(TrackerPreferences.Settings.FILTER_EDDYSTONE, "Eddystone"));
            mFilters.add(new FilterMenuItem(TrackerPreferences.Settings.FILTER_PROJECT_BEACONS, "Proj. Radbeacons"));
        }


        public void setFilterClickedListener(FilterClickedListener filterClickedListener) {
            this.filterClickedListener = filterClickedListener;
        }

        @NonNull
        @Override
        public FilterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new FilterViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.filter_dropdown_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull FilterViewHolder holder, final int position) {
            final FilterMenuItem filterItem = mFilters.get(position);
            holder.mSortOptionCheckBox.setText(filterItem.filterTitle);
            holder.mSortOptionCheckBox.setChecked(mPreferences.getBool(filterItem.id));
            holder.mSortOptionCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(filterClickedListener != null){
                        filterClickedListener.onFilterSelected(position, filterItem, ((AppCompatCheckBox) view).isChecked());
                    }
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return mFilters.size();
        }

        public class FilterViewHolder extends RecyclerView.ViewHolder{
            AppCompatCheckBox mSortOptionCheckBox;

            public FilterViewHolder(View itemView) {
                super(itemView);
                mSortOptionCheckBox = itemView.findViewById(R.id.filter_item_checkbox);
            }
        }

    }
}
