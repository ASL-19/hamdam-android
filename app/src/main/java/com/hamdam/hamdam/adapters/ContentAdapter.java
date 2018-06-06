package com.hamdam.hamdam.adapters;

import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hamdam.hamdam.model.StaticFact;
import com.hamdam.hamdam.presenter.PresenterContracts;
import com.hamdam.hamdam.util.UtilWrapper;
import com.hamdam.hamdam.view.fragment.StaticContentFragment;

import java.util.ArrayList;

/**
 * Adapter class for StaticFact objects to populate list of static content.
 */
public class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.ViewHolder> {
    private ArrayList<StaticFact> mDataset;
    private PresenterContracts.StaticContentPresenter presenter;

    public ContentAdapter(PresenterContracts.StaticContentPresenter presenter,
                          ArrayList<StaticFact> dataset) {
        this.presenter = presenter;
        this.mDataset = dataset;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView sectionHeading, itemHeading;
        public View layoutContainer;
        public LinearLayout topBorderLayout;

        public ViewHolder(View itemView, TextView sectionHeading,
                          TextView subHeading, View container, LinearLayout topBorderLayout) {
            super(itemView);
            this.sectionHeading = sectionHeading;
            this.itemHeading = subHeading;
            this.layoutContainer = container;
            this.topBorderLayout = topBorderLayout;
        }
    }

    @Override
    public ContentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(com.hamdam.hamdam.R.layout.item_fact_element, parent, false);

        CardView containerView = (CardView) view.findViewById(com.hamdam.hamdam.R.id.fact_container);
        TextView subHeading = (TextView) view.findViewById(com.hamdam.hamdam.R.id.fact_title);
        TextView sectionHeading = (TextView) view.findViewById(com.hamdam.hamdam.R.id.fact_section_header);
        LinearLayout cardBorderTop = (LinearLayout) view.findViewById(com.hamdam.hamdam.R.id.fact_cardview_top_border);

        return new ViewHolder(view, sectionHeading, subHeading, containerView, cardBorderTop);

    }

    @Override
    public void onBindViewHolder(final ContentAdapter.ViewHolder holder, final int position) {
        if (holder.getAdapterPosition() != position) {
            Log.e("ContentAdapter", "ViewHolder position is not the same as adapter position");
        }
        boolean isNewTopic = mDataset.get(position).isNewTopic();

        final String text = mDataset.get(position).getSubheading();
        holder.itemHeading.setText(text);

        if (isNewTopic) {
            holder.sectionHeading.setVisibility(View.VISIBLE);
            holder.sectionHeading.setText(mDataset.get(position).getHeading());
        } else {
            holder.sectionHeading.setVisibility(View.GONE);
        }
        int color = UtilWrapper.getBorderColor(mDataset.get(position).getTopicType());
        holder.topBorderLayout.setBackgroundResource(color);

        holder.layoutContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = StaticContentFragment.newInstance
                        (text, mDataset.get(holder.getAdapterPosition()).getBody(),
                                mDataset.get(position).getTopicType());
                presenter.showContentFragment(fragment, StaticContentFragment.class.getName());

            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

}
