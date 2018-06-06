package com.github.ebraminio.droidpersiancalendar.adapters;

import android.content.res.TypedArray;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.ebraminio.droidpersiancalendar.utils.Utils;

import com.hamdam.hamdam.R;
import com.hamdam.hamdam.view.activity.MainActivity;

import java.lang.ref.WeakReference;

/**
 * Part of DroidPersianCalendar project
 * {https://github.com/ebraminio/DroidPersianCalendar/}.
 * @author ebraminio
 */
public class DrawerAdapter extends RecyclerView.Adapter<DrawerAdapter.ViewHolder> {
    private final WeakReference<MainActivity> mainActivity;
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private int selectedItem;
    private String[] drawerTitles;
    private String[] drawerSubtitles;
    private TypedArray drawerIcon;

    public DrawerAdapter(MainActivity mainActivity) {
        this.mainActivity = new WeakReference<>(mainActivity);
        drawerTitles = mainActivity.getResources().getStringArray(com.hamdam.hamdam.R.array.drawerTitles);
        drawerSubtitles = mainActivity.getResources().getStringArray(com.hamdam.hamdam.R.array.drawerSubtitles);
        drawerIcon = mainActivity.getResources().obtainTypedArray(com.hamdam.hamdam.R.array.drawerIcons);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView itemTitle;
        private TextView itemSubtitle;
        private AppCompatImageView imageView;
        private View background, padView;

        public ViewHolder(View itemView, int ViewType) {
            super(itemView);

            if (ViewType == TYPE_ITEM) {
                itemView.setOnClickListener(this);
                itemView.setFilterTouchesWhenObscured(true);
                itemTitle = (TextView) itemView.findViewById(com.hamdam.hamdam.R.id.itemTitle);
                itemSubtitle = (TextView) itemView.findViewById(com.hamdam.hamdam.R.id.itemSubtitle);
                imageView = (AppCompatImageView) itemView.findViewById(R.id.item_drawer_icon);
                background = itemView.findViewById(com.hamdam.hamdam.R.id.background);
                padView = itemView.findViewById(R.id.no_icon_item);
            } else {
                imageView = (AppCompatImageView) itemView.findViewById(com.hamdam.hamdam.R.id.image);
            }
        }

        @Override
        public void onClick(View view) {
            if (mainActivity != null)
            mainActivity.get().onClickItem(getAdapterPosition());
        }
    }

    @Override
    public DrawerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(com.hamdam.hamdam.R.layout.item_drawer, parent, false);

            return new ViewHolder(v, viewType);

        } else if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(com.hamdam.hamdam.R.layout.header_drawer, parent, false);

            return new ViewHolder(v, viewType);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(DrawerAdapter.ViewHolder holder, int position) {
        if (!isPositionHeader(position) && mainActivity != null) {
            Utils utils = Utils.getInstance(mainActivity.get());
            utils.setFont(holder.itemTitle);
            holder.itemTitle.setText(drawerTitles[position - 1]);
            if (drawerSubtitles[position - 1].length() != 0) {
                holder.itemSubtitle.setVisibility(View.VISIBLE);
                utils.setTypeface(holder.itemSubtitle);
                holder.itemSubtitle.setText(drawerSubtitles[position - 1]);
            } else {
                holder.itemSubtitle.setVisibility(View.GONE);
            }

            if (position == drawerTitles.length
                    || position  == drawerTitles.length - 1
                    || position == drawerTitles.length - 2) { // About, FAQ, License; no icon
                holder.imageView.setVisibility(View.GONE);
                holder.padView.setVisibility(View.VISIBLE);
            } else {
                holder.padView.setVisibility(View.GONE);
                holder.imageView.setVisibility(View.VISIBLE);
                holder.imageView.setImageResource(drawerIcon.getResourceId(position - 1, 0));
            }
            if (selectedItem == position) {
                holder.background.setVisibility(View.VISIBLE);
            } else {
                holder.background.setVisibility(View.GONE);
            }
        }
    }

    public void setSelectedItem(int item) {
        selectedItem = item;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return drawerTitles.length + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position)) {
            return TYPE_HEADER;
        } else {
            return TYPE_ITEM;
        }
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }
}
