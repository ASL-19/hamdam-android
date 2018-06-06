package com.hamdam.hamdam.adapters;

import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hamdam.hamdam.model.DataStat;

import java.util.ArrayList;

/**
 * Adapter for infographic (data summary) display.
 */
public class InfographicAdapter extends RecyclerView.Adapter<InfographicAdapter.ViewHolder> {
    private ArrayList<DataStat> mDataset;

    public InfographicAdapter(ArrayList<DataStat> dataset) {
        this.mDataset = dataset;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title, description, value;
        public AppCompatImageView icon;

        public ViewHolder(View itemView, TextView title, TextView description, TextView value,
                          AppCompatImageView icon) {
            super(itemView);
            this.title = title;
            this.description = description;
            this.value = value;
            this.icon = icon;
        }
    }

    @Override
    public InfographicAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(com.hamdam.hamdam.R.layout.item_infographic_element, parent, false);

        TextView title = (TextView) view.findViewById(com.hamdam.hamdam.R.id.infographic_title);
        TextView value = (TextView) view.findViewById(com.hamdam.hamdam.R.id.infographic_text_value);
        TextView description = (TextView) view.findViewById(com.hamdam.hamdam.R.id.infographic_text_description);
        AppCompatImageView icon = (AppCompatImageView) view.findViewById(com.hamdam.hamdam.R.id.infographic_icon);

        return new ViewHolder(view, title, description, value, icon);
    }

    @Override
    public void onBindViewHolder(InfographicAdapter.ViewHolder holder, int position) {
        holder.title.setText(mDataset.get(position).getTitle());
        holder.description.setText(mDataset.get(position).getDescription());
        holder.value.setText(mDataset.get(position).getFormattedValue());


    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
