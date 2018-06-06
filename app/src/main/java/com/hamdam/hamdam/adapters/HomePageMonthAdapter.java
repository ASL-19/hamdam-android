package com.hamdam.hamdam.adapters;

import android.content.Context;
import android.os.Build;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hamdam.hamdam.R;
import com.hamdam.hamdam.model.MenstruationDayModel;
import com.hamdam.hamdam.util.AnimateUtils;

import java.util.List;

/**
 * Simple adapter class to display the current month's menstrual calendar information.
 */
public class HomePageMonthAdapter extends RecyclerView.Adapter<HomePageMonthAdapter.ViewHolder> {
	private static final String TAG = "HomePageMonthAdapter";

    private Context context;
    private List<MenstruationDayModel> days;

    // Store period and ovulation ranges
    private int layoutPadding, parentLayoutWidth;
    private final int PAD_IN_DP = 2, MARGIN_IN_DP = 10;


    public HomePageMonthAdapter(Context context,
                                List<MenstruationDayModel> days) {
        this.context = context;
        this.days = days;
        float scale = context.getResources().getDisplayMetrics().density;
        layoutPadding = (int) (PAD_IN_DP * scale + 0.5f); // convert padding (dp) to px

        // screen width - (margin + border padding) * 2 = recyclerview width.
        parentLayoutWidth = context.getResources().getDisplayMetrics().widthPixels
                - 2 * (layoutPadding + (int) (MARGIN_IN_DP * scale + 0.5f));

    }

    /*
     * Replace current days with new list of days.
     * The caller is responsible for calling notifyDatasetChanged() and updating the view.
     */
    public void addAllItems(List<MenstruationDayModel> days) {
        this.days = days;
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        View dot;
        AppCompatImageView today;
        LinearLayoutCompat itemLayoutCompat;

        protected ViewHolder(View itemView) {
            super(itemView);
            today = (AppCompatImageView) itemView.findViewById(com.hamdam.hamdam.R.id.today);
            itemLayoutCompat = (LinearLayoutCompat)
                    itemView.findViewById(com.hamdam.hamdam.R.id.item_layout_container);
            dot = itemView.findViewById(com.hamdam.hamdam.R.id.dot);
        }
    }

    @Override
    public HomePageMonthAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(com.hamdam.hamdam.R.layout.item_homepage_day, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        LinearLayoutCompat.LayoutParams params = (LinearLayoutCompat.LayoutParams)
                holder.today.getLayoutParams();

        // Reset item width and padding to default
        holder.itemLayoutCompat.setMinimumWidth(parentLayoutWidth / days.size());
        holder.itemLayoutCompat.setPadding(holder.itemLayoutCompat.getPaddingLeft(),
                holder.itemLayoutCompat.getPaddingTop(),
                holder.itemLayoutCompat.getPaddingRight(),
                holder.itemLayoutCompat.getPaddingBottom());
        holder.itemLayoutCompat.setBackgroundResource(R.color.white);
        holder.today.setBackground(ResourcesCompat.getDrawable(context.getResources(),
                com.hamdam.hamdam.R.drawable.day_homepage, null));
        holder.today.setAlpha(1.0f);

        // Reset layout margins and hide day/dot views
        params.setMargins(1, 1, 1, 1);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            // fix display...
            params.setMargins(0,0,0,0);
        }

        holder.today.setLayoutParams(params);

        holder.today.setVisibility(View.GONE);
        holder.dot.setBackgroundResource(com.hamdam.hamdam.R.drawable.circle_day_inactive);
        holder.dot.setMinimumWidth(6);
        holder.dot.setMinimumHeight(6);
        holder.dot.setVisibility(View.GONE);

        if (position >= 0 && position < days.size()) {
            holder.dot.setVisibility(View.VISIBLE);
            holder.today.setBackground(ResourcesCompat.getDrawable(context.getResources(),com.hamdam.hamdam.R.drawable.day_homepage, null));
            holder.today.setVisibility(View.VISIBLE);

            // Set current day indicator and fill other days with default gray
            if (days.get(position).isToday()) {
                holder.dot.setBackgroundResource(com.hamdam.hamdam.R.drawable.circle_select_light);
                holder.dot.setMinimumWidth(20);
                holder.dot.setMinimumHeight(20);
            }

            // Round corners of listview; replace day views with rounded corners if on ends
            if (position == 0) {
                holder.itemLayoutCompat.setBackgroundResource
                        (com.hamdam.hamdam.R.drawable.bg_rounded_white_start);
                holder.itemLayoutCompat.setPadding(holder.itemLayoutCompat.getPaddingLeft()
                                + layoutPadding,
                        holder.itemLayoutCompat.getPaddingTop(),
                        holder.itemLayoutCompat.getPaddingRight(),
                        holder.itemLayoutCompat.getPaddingBottom());
                holder.today.setBackground(ResourcesCompat.getDrawable(context.getResources(),
                        com.hamdam.hamdam.R.drawable.bg_rounded_gray_start, null));
            }

            if (position == days.size() - 1) {
                holder.itemLayoutCompat.setBackgroundResource
                        (com.hamdam.hamdam.R.drawable.bg_rounded_white_end);
                holder.itemLayoutCompat.setPadding(holder.itemLayoutCompat.getPaddingLeft(),
                        holder.itemLayoutCompat.getPaddingTop(),
                        holder.itemLayoutCompat.getPaddingRight() + layoutPadding,
                        holder.itemLayoutCompat.getPaddingBottom());
                holder.today.setBackground(ResourcesCompat.getDrawable(context.getResources(),
                        com.hamdam.hamdam.R.drawable.bg_rounded_gray_end, null));
            }

            // Set period and ovulation background windows; remove day margins (fill solid colour)
            // Set ovulation dimness on gradient
            if (days.get(position).isOvulation()) {
                int dimLevel = days.get(position).getOvDimness();
                float alpha = 0.6f + ((float) dimLevel * 0.15f); // outer edges more dimmed
                holder.today.setAlpha(alpha < 1.0f ? alpha : 1.0f);
                holder.dot.setBackground(ResourcesCompat.getDrawable(context.getResources(),
                        com.hamdam.hamdam.R.drawable.ovulation_window, null));
                params.setMargins(0, 0, 0, 0);
                holder.today.setLayoutParams(params);

                if (position == days.size() - 1) {
                    holder.today.setBackgroundResource
                            (com.hamdam.hamdam.R.drawable.bg_rounded_ov_end);

                } else if (position == 0) {
                    holder.today.setBackgroundResource
                            (com.hamdam.hamdam.R.drawable.bg_rounded_ov_start);
                } else {
                    holder.today.setBackground(ResourcesCompat.getDrawable(context.getResources(),
                            com.hamdam.hamdam.R.drawable.ovulation_window_homepage, null));
                }
                AnimateUtils.fadeIn(holder.today, alpha);
            }

            // Not 'else' -- date could be both period and ovulation--more important to display period
            if (days.get(position).isPeriod() || days.get(position).isPeriodProjection()) {
                float alpha = 1.0f;
                params.setMargins(0, 0, 0, 0);
                holder.today.setLayoutParams(params);
                holder.dot.setBackgroundResource(com.hamdam.hamdam.R.drawable.period_window);

                if (position == days.size() - 1) {
                    holder.today.setImageResource(R.drawable.bg_rounded_red_end);
//                            (ResourcesCompat.getDrawable(context.getResources(),com.hamdam.hamdam.R.drawable.bg_rounded_red_end,null));

                } else if (position == 0) {
                    holder.today.setBackgroundResource
                            (com.hamdam.hamdam.R.drawable.bg_rounded_red_start);
                } else {
                    holder.today.setImageResource(R.drawable.period_window_homepage);
//                    holder.today.setBackground(ResourcesCompat.getDrawable(context.getResources(),com.hamdam.hamdam.R.drawable.period_window_homepage, null));
                }

                // Dim projections only
                if (days.get(position).isPeriod()) {
                    holder.today.setAlpha(1.0f);
                } else {
                    alpha = 0.8f;
                    holder.today.setAlpha(0.8f);
                }
                AnimateUtils.fadeIn(holder.today, alpha);
            }

        } else {
            Log.e("HomePageMonthAdapter", "position " + position + "out of display range");
        }
    }


    @Override
    public int getItemCount() {
        return days.size();
    }

    public List<MenstruationDayModel> getDays() {
        return days;
    }
}
