package com.jk.stepcounter.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jk.stepcounter.info.StepCountInfo;

import java.util.ArrayList;

/**
 * Created by 17652 on 2018/3/7.
 */

public class MainRecyclerViewAdapter extends RecyclerView.Adapter<MainRecyclerViewAdapter.ViewHolder> {

    public void setDataSet(ArrayList<StepCountInfo> dataSet) {
        mDataSet = dataSet;
        notifyDataSetChanged();
    }

    private ArrayList<StepCountInfo> mDataSet;
    private String todayData = "";

    public void setServiceStatus(String serviceStatus) {
        ServiceStatus = serviceStatus;
    }

    private String ServiceStatus = "0";

    public void setTodayData(String todayData) {
        this.todayData = todayData;
        notifyItemChanged(0);
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView;

        public ViewHolder(TextView v) {
            super(v);
            mTextView = v;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MainRecyclerViewAdapter(ArrayList<StepCountInfo> myDataset) {
        mDataSet = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MainRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                 int viewType) {
        // create a new view
        TextView v = (TextView) LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        // set the view's size, margins, paddings and layout parameters
        // ...

        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        StepCountInfo stepCountInfo = mDataSet.get(position);

        if (position == 0 ){

                String s = "今天走了" + todayData + "步" + ServiceStatus;
                holder.mTextView.setText( s );
                return;


        }
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        String s = "日期: " + stepCountInfo.date + "\n"
                + "步数： " + stepCountInfo.stepCount;

        holder.mTextView.setText(s);

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (mDataSet != null && mDataSet.size()!=0) {

            return mDataSet.size();
        }
        return 1;
    }

}
