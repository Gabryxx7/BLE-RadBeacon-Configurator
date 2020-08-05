package com.unimelb.marinig.bletracker.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.unimelb.marinig.bletracker.Adapters.Data.ServiceContent;
import com.unimelb.marinig.bletracker.Adapters.ServicesListRecyclerViewAdapter;
import com.unimelb.marinig.bletracker.Interfaces.ToolbarFragment;
import com.unimelb.marinig.bletracker.Logger.TrackerLog;
import com.unimelb.marinig.bletracker.R;

import java.util.List;

/**
 * A simple {@link androidx.fragment.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ServicesListFragment.OnListFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ServicesListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ServicesListFragment extends ToolbarFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnListFragmentInteractionListener mListener;
    private View mView = null;
    private RecyclerView mRecyclerView = null;
    private ServicesListRecyclerViewAdapter servicesListAdapter;
    private List<ServiceContent> mServicesList;

    public ServicesListFragment() {
        // Required empty public constructor
    }

    final Handler refreshHandler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            updateServicesList();
            refreshHandler.postDelayed(this, 2 * 1000);
        }
    };

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ServicesListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ServicesListFragment newInstance(String param1, String param2) {
        ServicesListFragment fragment = new ServicesListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TrackerLog.d("Fragments", "Creating Fragment");
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        refreshHandler.postDelayed(runnable, 2 * 1000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        refreshHandler.removeCallbacks(runnable);
        //BLETrackerApplication.getRefWatcher(this.getContext()).watch(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(mView == null) {
            mView = inflater.inflate(R.layout.fragment_services_list, container, false);
            Context context = mView.getContext();
            mRecyclerView = mView.findViewById(R.id.services_list);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            mRecyclerView.setAdapter(servicesListAdapter);

            Button startAllButton = mView.findViewById(R.id.start_all_button);
            Button stopAllButton = mView.findViewById(R.id.stop_all_button);

            startAllButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    for(ServiceContent service : mServicesList){
                        service.startServiceContent();
                    }
                }
            });

            stopAllButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    for(ServiceContent service : mServicesList){
                        service.stopServiceContent();
                    }
                }
            });

            //Optional:  Decoration and divider
            //DividerItemDecoration itemDecorator = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
            //itemDecorator.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.divider));
            //mRecyclerView.addItemDecoration(itemDecorator);

            // Set the adapter
            /*
            if (mView instanceof RecyclerView) {
                Context context = mView.getContext();
                mRecyclerView = (RecyclerView) mView;
                if (mColumnCount <= 1) {
                    mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                } else {
                    mRecyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
                }
                mRecyclerView.setAdapter(btDeviceListAdapter);
            }*/
        }
        return mView;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setServicesListAdapter(ServicesListRecyclerViewAdapter adapter){
        servicesListAdapter = adapter;
    }

    public void updateServicesList(){
        servicesListAdapter.notifyDataSetChanged();
    }

    public void setServicesList(List<ServiceContent> list){
        mServicesList = list;
    }

    @Override
    public void updateToolbar(Toolbar pToolbar) {
        super.updateToolbar(pToolbar);
        pToolbar.setTitle("Active Services");
        pToolbar.setSubtitle("");
        MenuItem refreshItem = pToolbar.getMenu().findItem(R.id.action_refresh);
        if(refreshItem != null)
            refreshItem.setVisible(false);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(ServiceContent item);
    }
}
