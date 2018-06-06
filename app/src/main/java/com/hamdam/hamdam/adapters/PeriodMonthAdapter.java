package com.hamdam.hamdam.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.ebraminio.droidpersiancalendar.utils.Utils;
import com.hamdam.hamdam.view.fragment.PeriodMonthFragment;

import com.hamdam.hamdam.model.MenstruationDayModel;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * RecyclerView Adapter for calendar months. This code is based on the DroidPersianCalendar
 * project, but has been modified to allow it to hold custom Fragment objects, as well
 * as display additional graphics on each calendar day depending on menstruation stats.
 */
public class PeriodMonthAdapter extends RecyclerView.Adapter<PeriodMonthAdapter.ViewHolder> {

    private WeakReference<Context> mWeakContext;
    private PeriodMonthFragment monthFragment;
    private final int TYPE_HEADER = 0;
    private final int TYPE_DAY = 1;
    private List<MenstruationDayModel> days;
    private int selectedDay = -1;
    private TypedValue colorDayName = new TypedValue();
    private final int firstDayOfWeek;
    private final int totalDays;
    private final String[] FIRST_CHAR_OF_DAYS_OF_WEEK_NAME = {"ش", "ی", "د", "س",
            "چ", "پ", "ج"};

    // RecyclerViewHolder offset: Ids for calendar days start after offset
    private final int RECYCLEVIEW_OFFSET = 6;

    public PeriodMonthAdapter(Context context, PeriodMonthFragment monthFragment, List<MenstruationDayModel> days) {
        firstDayOfWeek = days.get(0).getDayOfWeek();
        totalDays = days.size();
        this.monthFragment = monthFragment;
        this.mWeakContext = new WeakReference<>(context);
        this.days = days;
        context.getTheme().resolveAttribute(com.hamdam.hamdam.R.attr.colorTextDayName, colorDayName, true);
    }

    public void clearSelectedDay() {
        selectedDay = -1;
        notifyDataSetChanged();
    }

    public void selectDay(int dayOfMonth) {
        selectedDay = dayOfMonth + RECYCLEVIEW_OFFSET + firstDayOfWeek;
        notifyDataSetChanged();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView num;
        View selectDay;
        View periodWindow;

        public ViewHolder(View itemView) {
            super(itemView);

            num = (TextView) itemView.findViewById(com.hamdam.hamdam.R.id.num);
            selectDay = itemView.findViewById(com.hamdam.hamdam.R.id.select_day);
            periodWindow = itemView.findViewById(com.hamdam.hamdam.R.id.periodWindowBackground);
            itemView.setFilterTouchesWhenObscured(true);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            position += RECYCLEVIEW_OFFSET - (position % 7) * 2;
            if (totalDays < position - RECYCLEVIEW_OFFSET - firstDayOfWeek) {
                return;
            }

            if (position - 7 - firstDayOfWeek >= 0) {
                monthFragment.onClickItem(days
                        .get(position - (RECYCLEVIEW_OFFSET + 1) - firstDayOfWeek)
                        .getPersianDate());

                selectedDay = position; // to move the circle
                notifyDataSetChanged();
            }
        }
    }

    @Override
    public PeriodMonthAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = mWeakContext.get();
        if (context != null) {
            View v = LayoutInflater.from(context).inflate(com.hamdam.hamdam.R.layout.item_day, parent, false);

            return new ViewHolder(v);
        }
        Log.e("PeriodMonthAdapter", "Context was null; viewholder not created");
        return null;
    }

    @Override
    public void onBindViewHolder(PeriodMonthAdapter.ViewHolder holder, int position) {

        holder.periodWindow.setAlpha(1.0f);
        position += RECYCLEVIEW_OFFSET - (position % 7) * 2;
        if (totalDays < position - RECYCLEVIEW_OFFSET - firstDayOfWeek) { // out of range
            return;
        }

        if (!isPositionHeader(position)) {
            if (position - (RECYCLEVIEW_OFFSET + 1) - firstDayOfWeek >= 0) {

                // Font, text and digit for calendar number
                setCalendarNumber(holder, position);

                if (position == selectedDay) {
                    holder.selectDay.setVisibility(View.VISIBLE);

                } else {
                    holder.selectDay.setVisibility(View.GONE);
                }

                if (days.get(position - (RECYCLEVIEW_OFFSET + 1) - firstDayOfWeek).isToday()) {
                    holder.selectDay.setVisibility(selectedDay == -1 || selectedDay == position
                            ? View.VISIBLE
                            : View.GONE);
                }

                // Set period and ovulation background windows.
                if (days.get(position - (RECYCLEVIEW_OFFSET + 1) - firstDayOfWeek).isOvulation()) {

                    // dimness gradient: edges are dimmer than middle: levels high -> low, middle 0
                    int dimLevel = days.get(position - (RECYCLEVIEW_OFFSET + 1) - firstDayOfWeek)
                            .getOvDimness(); // lower means dimmer
                    float alpha = 0.25f + ((float) dimLevel * 0.25f); // outer edges more dimmed
                    holder.periodWindow.setAlpha(alpha < 0.9f ? alpha : 0.9f);
                    holder.periodWindow.setBackgroundResource(com.hamdam.hamdam.R.drawable.ovulation_window);
                    holder.periodWindow.setVisibility(View.VISIBLE);
                }

                if (days.get(position - (RECYCLEVIEW_OFFSET + 1) - firstDayOfWeek).isPeriodProjection()) {
                    holder.periodWindow.setAlpha(0.5f);
                    holder.periodWindow.setBackgroundResource(com.hamdam.hamdam.R.drawable.period_window);
                    holder.periodWindow.setVisibility(View.VISIBLE);
                }

                if (days.get(position - (RECYCLEVIEW_OFFSET + 1) - firstDayOfWeek).isPeriod()) {
                    holder.periodWindow.setAlpha(1.0f);
                    holder.periodWindow.setBackgroundResource(com.hamdam.hamdam.R.drawable.period_window);
                    holder.periodWindow.setVisibility(View.VISIBLE);
                }

            } else {
                holder.selectDay.setVisibility(View.GONE);
                holder.num.setVisibility(View.GONE);
                holder.periodWindow.setVisibility(View.GONE);
            }
        } else {
            holder.num.setText(FIRST_CHAR_OF_DAYS_OF_WEEK_NAME[position]);
            holder.num.setTextSize(20);
            holder.selectDay.setVisibility(View.GONE);
            holder.num.setVisibility(View.VISIBLE);
            Context context = mWeakContext.get();
            if (context != null) {
                holder.num.setTextColor(ContextCompat.getColor(context, colorDayName.resourceId));
                Utils.getInstance(context).setFont(holder.num);
            }
        }
    }

    @Override
    public int getItemCount() {
        return 7 * 7; // days of week * month view rows
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position)) {
            return TYPE_HEADER;
        } else {
            return TYPE_DAY;
        }
    }

    private boolean isPositionHeader(int position) {
        return position < (RECYCLEVIEW_OFFSET + 1);
    }

    private void setCalendarNumber(ViewHolder holder, int position) {
        holder.num.setText(days.get(position - 7 - days.get(0).getDayOfWeek()).getNum());
        holder.num.setVisibility(View.VISIBLE);

        holder.num.setTextSize(22);
        Context context = mWeakContext.get();
        if (context != null) {
            holder.num.setTextColor(ContextCompat.getColor(context, com.hamdam.hamdam.R.color.light_text_day));
        }
    }

    public List<MenstruationDayModel> getDays() {
        return days;
    }



}

