package com.unimelb.marinig.bletracker.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.unimelb.marinig.bletracker.Adapters.Data.ServiceContent;
import com.unimelb.marinig.bletracker.Fragments.ServicesListFragment.OnListFragmentInteractionListener;
import com.unimelb.marinig.bletracker.Logger.TrackerLog;
import com.unimelb.marinig.bletracker.R;

import java.util.List;

/**
 * This class represents the adapter for the ListView (or better, RecyclerView) which shows the list of active services in the services fragments
 * It has a list of ServiceContent (which are simply Services represented by a name, a bootstrapper and maybe a context and a Service class reference)
 *
 * It works by using a ViewHolder which basically represents the View of a single item in the list, in our case that view is the service_item.xml file.
 * So once the view is created it can be recycled but we don't need to take care of it!
 *
 */
public class ServicesListRecyclerViewAdapter extends RecyclerView.Adapter<ServicesListRecyclerViewAdapter.ViewHolder> {

    private final List<ServiceContent> mValues;
    private final OnListFragmentInteractionListener mListener;

    public ServicesListRecyclerViewAdapter(List<ServiceContent> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ServicesListRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.service_item, parent, false);
        return new ServicesListRecyclerViewAdapter.ViewHolder(view);
        //return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.service_item, null));
    }

    /**
     *  We need to take care of when an item is binded to a View! When that happens we need to be sure that the View shows the correct data
     * So since we got the position of the requested Item, we get the item from the list (which will be a ServiceContent), we get its name and show it in the view
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(final ServicesListRecyclerViewAdapter.ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        final ServiceContent service= mValues.get(position);
        final String serviceName = service.getServiceName();
        holder.mServiceName.setText(serviceName);
        holder.mStatus.setVisibility( service.isRunning() ? View.VISIBLE : View.INVISIBLE);

        //Now this was tricky, we can't use a context to start a service, so nothing like MaintActivity.this.startService, nor context.startService
        //We needed something which is different for every service, or that at least can start a service
        //So first idea was having a ServiceContent with the context, so when it's created in the MainActivity it would pass MaintAcitivy.this
        //And then this would simply call service.StartServiceCOntent().
        //The final solution is similar but instead, when startServiceContent is called a custom method of the ServiceBootstrapper is called! YAY!
        holder.mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                service.startServiceContent();
                TrackerLog.d("ServicesListFragment", serviceName + " startButton clicked, starting service...");
                //holder.mStatus.setVisibility( service.isRunning() ? View.VISIBLE : View.INVISIBLE);
            }
        });

        holder.mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                service.stopServiceContent();
                TrackerLog.d("ServicesListFragment", serviceName + " stopButton clicked, stopping service...");
                //holder.mStatus.setVisibility( service.isRunning() ? View.VISIBLE : View.INVISIBLE);
            }
        });

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mServiceName;
        public final Button mStartButton;
        public final Button mStopButton;
        public final ProgressBar mStatus;

        public ServiceContent mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mServiceName = (TextView) view.findViewById(R.id.serviceName);
            mStartButton = (Button) view.findViewById(R.id.startButton);
            mStopButton = (Button) view.findViewById(R.id.stopButton);
            mStatus = (ProgressBar) view.findViewById(R.id.service_status);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mServiceName.getText() + "'";
        }
    }
}
