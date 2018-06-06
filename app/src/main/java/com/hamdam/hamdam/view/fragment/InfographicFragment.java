package com.hamdam.hamdam.view.fragment;


import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.ebraminio.droidpersiancalendar.calendar.PersianDate;
import com.github.ebraminio.droidpersiancalendar.utils.Utils;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.hamdam.hamdam.enums.StatusEnum;
import com.hamdam.hamdam.model.DataStat;
import com.hamdam.hamdam.presenter.PresenterContracts;
import com.hamdam.hamdam.util.DateUtil;
import com.hamdam.hamdam.util.UtilWrapper;
import com.hamdam.hamdam.view.activity.BaseActivity;
import com.hamdam.hamdam.view.dialog.SingleStatusDialogFragment;

import com.hamdam.hamdam.adapters.InfographicAdapter;
import com.hamdam.hamdam.presenter.DatabaseHelperImpl;
import com.hamdam.hamdam.view.dialog.GraphDialogFragment;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.charts.ValueLineChart;
import org.eazegraph.lib.communication.IOnItemFocusChangedListener;
import org.eazegraph.lib.models.PieModel;
import org.eazegraph.lib.models.ValueLinePoint;
import org.eazegraph.lib.models.ValueLineSeries;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import uk.co.chrisjenx.calligraphy.TypefaceUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class InfographicFragment extends Fragment implements View.OnClickListener,
        IOnItemFocusChangedListener {
    private static final String TAG = "InfographicFragment";

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private static InfographicDataTask mDataTask;
    private static LineChartTask mChartTask, mPeriodChartTask;
    private static PieChartTask mStatusChartTask;
    private ValueLineChart mPeriodLineChart, mCycleLineChart;
    private PieChart mStatusPieChart;
    private TextView noPeriodDataMessage, noCycleDataMessage;
    private TextView[] textViews; // the legend beside pie chart
    private Map<Integer, TextView> mLegendMap; // Pie chart legend to pie element
    private Map<TextView, Integer> mIndexMap;
    private Map<String, TextView> mViewLabels; // Legend label corresponding to view
    private static final int VIEW_IMAGE_REQUEST = 1;
    private Tracker mTracker;

    private enum Task {
        PERIOD,
        CYCLE
    }

    public InfographicFragment() {
        // Required empty public constructor
    }

    public static InfographicFragment newInstance() {
        return new InfographicFragment();
    }

    @Override
    public void onStart() {
        super.onStart();

        mLegendMap = new HashMap<>();
        mIndexMap = new HashMap<>();

        loadInfoData();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mDataTask != null) {
            mDataTask.cancel(true);
            mDataTask = null;
        }
        if (mChartTask != null) {
            mChartTask.cancel(true);
            mChartTask = null;
        }
        if (mPeriodChartTask != null) {
            mPeriodChartTask.cancel(true);
            mPeriodChartTask = null;
        }
        if (mStatusChartTask != null) {
            mStatusChartTask.cancel(true);
            mStatusChartTask = null;
        }

        mLegendMap = null;
        mIndexMap = null;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Analytics
        BaseActivity parent = (BaseActivity) getActivity();
        if (parent != null) {
            mTracker = parent.getTracker();
            mTracker.setScreenName(getClass().getSimpleName());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mTracker != null) {
            mTracker.send(new HitBuilders.ScreenViewBuilder()
                    .build());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().getWindow().setStatusBarColor
                    (ContextCompat.getColor(getContext(), com.hamdam.hamdam.R.color.primary_dark));
        }

        View view = inflater.inflate(com.hamdam.hamdam.R.layout.fragment_infographic, container, false);
        UtilWrapper.setActionBar(getActivity(), getString(com.hamdam.hamdam.R.string.your_data), true);

        mRecyclerView = (RecyclerView) view.findViewById(com.hamdam.hamdam.R.id.infographic_recycler_view);
        mPeriodLineChart = (ValueLineChart) view.findViewById(com.hamdam.hamdam.R.id.line_chart_periods);
        mCycleLineChart = (ValueLineChart) view.findViewById(com.hamdam.hamdam.R.id.line_chart_cycles);
        mStatusPieChart = (PieChart) view.findViewById(com.hamdam.hamdam.R.id.status_pie_chart);
        mStatusPieChart.setOnItemFocusChangedListener(this);

        AppCompatTextView mCycleLengthTitle = (AppCompatTextView) view.findViewById(com.hamdam.hamdam.R.id.cycle_chart_title);
        mCycleLengthTitle.setText(com.hamdam.hamdam.R.string.chart_title_cycle_lengths);

        AppCompatTextView mPeriodLengthTitle = (AppCompatTextView) view.findViewById(com.hamdam.hamdam.R.id.period_chart_title);
        mPeriodLengthTitle.setText(com.hamdam.hamdam.R.string.chart_title_period_lengths);

        AppCompatTextView mStatusTitle = (AppCompatTextView) view.findViewById(com.hamdam.hamdam.R.id.status_chart_title);
        mStatusTitle.setText(com.hamdam.hamdam.R.string.chart_title_status_breakdown);

        noCycleDataMessage = (TextView) view.findViewById(com.hamdam.hamdam.R.id.no_data_cycle);
        noPeriodDataMessage = (TextView) view.findViewById(com.hamdam.hamdam.R.id.no_data_period);

        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mViewLabels = new HashMap<>();
        setLegendListener(view);

        return view;

    }

    // Load infographic data
    private void loadInfoData() {
        mDataTask = new InfographicDataTask(new WeakReference<>(getContext()), mRecyclerView);
        mDataTask.execute();

        if (noCycleDataMessage != null) {
            mChartTask = new LineChartTask(new WeakReference<Context>(getContext()), mCycleLineChart,
                    noCycleDataMessage, Task.CYCLE);
            mChartTask.execute();
        }

        if (noPeriodDataMessage != null) {
            mPeriodChartTask = new LineChartTask(new WeakReference<Context>(getContext()),
                    mPeriodLineChart, noPeriodDataMessage, Task.PERIOD);
            mPeriodChartTask.execute();
        }

        mStatusChartTask = new PieChartTask(new WeakReference<Context>(getContext()),
                mStatusPieChart, mLegendMap, mIndexMap,mViewLabels);
        mStatusPieChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rotateWheel();
            }
        });
        mStatusChartTask.execute();
    }

    private void rotateWheel() {
        if (!mStatusPieChart.getData().isEmpty()) {
            mStatusPieChart.setCurrentItem((mStatusPieChart.getCurrentItem() + 1)
                    % mStatusPieChart.getData().size());
        }
    }

    // IOItemFocusChangeListener for pie chart slices
    @Override
    public void onItemFocusChanged(int position) {
        TextView targetView = mLegendMap.get(position);
        for (TextView t : textViews) {
            t.setTextSize(20);
            if (mIndexMap.containsKey(t)) {
                t.setTextColor(UtilWrapper.getStatusColor(getContext(), getStatusFromView(t)));
            } else {
                t.setTextColor(Color.LTGRAY);
            }
            t.setTypeface((TypefaceUtils.load(getActivity().getAssets(), "fonts/iransans.ttf")));
            t.setBackgroundResource(0); // no button
        }
        if (targetView != null) {
            targetView.setBackgroundResource(com.hamdam.hamdam.R.drawable.button_bg_round_corners);
            GradientDrawable background = (GradientDrawable) targetView.getBackground();
            int targetColor = targetView.getCurrentTextColor();
            background.setColor(targetColor);
            targetView.setTextColor(Color.WHITE);
            targetView.setTextSize(24);
            targetView.setTypeface
                    ((TypefaceUtils.load(getActivity().getAssets(), "fonts/iransans_bold.ttf")));
        }
    }

    /*
     * Set onClickListener for legend items
     */
    private void setLegendListener(View view) {
        textViews = new TextView[]{(TextView) view.findViewById(com.hamdam.hamdam.R.id.legend_sex),
                (TextView) view.findViewById(com.hamdam.hamdam.R.id.legend_bleeding),
                (TextView) view.findViewById(com.hamdam.hamdam.R.id.legend_exercise),
                (TextView) view.findViewById(com.hamdam.hamdam.R.id.legend_fluids),
                (TextView) view.findViewById(com.hamdam.hamdam.R.id.legend_sleep),
                (TextView) view.findViewById(com.hamdam.hamdam.R.id.legend_mood),
                (TextView) view.findViewById(com.hamdam.hamdam.R.id.legend_pain)};

        // Add textviews to map and set on click listener
        for (TextView t : textViews) {
            t.setOnClickListener(this);
            mViewLabels.put(t.getText().toString(), t);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case com.hamdam.hamdam.R.id.legend_exercise:
                int ex = getLegendIndex((TextView) view);
                onItemFocusChanged(ex);
                SingleStatusDialogFragment singleStatusFragment =
                        SingleStatusDialogFragment.newInstance(getStatusFromView(view));
                singleStatusFragment.show(getChildFragmentManager(),
                        SingleStatusDialogFragment.class.getName());
                break;
            case com.hamdam.hamdam.R.id.legend_sex:
            case com.hamdam.hamdam.R.id.legend_bleeding:
            case com.hamdam.hamdam.R.id.legend_mood:
            case com.hamdam.hamdam.R.id.legend_sleep:
            case com.hamdam.hamdam.R.id.legend_fluids:
            case com.hamdam.hamdam.R.id.legend_pain:
                int which = getLegendIndex((TextView) view);
                onItemFocusChanged(which);
                GraphDialogFragment graphDialogFragment =
                        GraphDialogFragment.newInstance(getStatusFromView(view));
                graphDialogFragment.show(getChildFragmentManager(),
                        GraphDialogFragment.class.getName());
                break;
            default:
                break;
        }
    }

    private int getLegendIndex(TextView view) {
        if (mIndexMap != null && mIndexMap.containsKey(view)) {
            return mIndexMap.get(view);
        } else {
            return 0;
        }
    }

    @Nullable
    private StatusEnum.StatusType getStatusFromView(View view) {
        switch (view.getId()) {
            case com.hamdam.hamdam.R.id.legend_bleeding:
                return StatusEnum.StatusType.BLEEDING;
            case com.hamdam.hamdam.R.id.legend_exercise:
                return StatusEnum.StatusType.EXERCISE;
            case com.hamdam.hamdam.R.id.legend_sex:
                return StatusEnum.StatusType.SEX;
            case com.hamdam.hamdam.R.id.legend_mood:
                return StatusEnum.StatusType.MOOD;
            case com.hamdam.hamdam.R.id.legend_sleep:
                return StatusEnum.StatusType.SLEEP;
            case com.hamdam.hamdam.R.id.legend_fluids:
                return StatusEnum.StatusType.FLUIDS;
            case com.hamdam.hamdam.R.id.legend_pain:
                return StatusEnum.StatusType.PAIN;
            default:
                return null;
        }
    }

    /**
     * Async tasks to populate graphs/charts with user summary data.
     */
    private static class PieChartTask extends AsyncTask<Void, Void, ArrayList<PieModel>> {

        WeakReference<Context> contextWeakReference;
        private ProgressDialog mProgressDialog;
        private PieChart mChart;
        Map<Integer, TextView> mLegendMap; // Pie chart legend to pie element
        Map<TextView, Integer> mIndexMap;
        Map<String, TextView> mViewLabels;

        public PieChartTask(WeakReference<Context> contextWeakReference, PieChart chart,
                            Map<Integer, TextView> legendMap, // Pie chart legend to pie element
                            Map<TextView, Integer> indexMap,
                            Map<String, TextView> viewLabels) {
            this.contextWeakReference = contextWeakReference;
            this.mChart = chart;
            this.mLegendMap =legendMap;
            this.mViewLabels = viewLabels;
            this.mIndexMap = indexMap;
        }

        @Override
        protected void onPreExecute() {
            if (contextWeakReference != null) {
                Context context = contextWeakReference.get();
                mProgressDialog = new ProgressDialog(context);
                mProgressDialog.show();
            }
        }

        @Override
        protected ArrayList<PieModel> doInBackground(Void... voids) {
            if (contextWeakReference != null) {
                Context context = contextWeakReference.get();
                PresenterContracts.DatabasePresenter dbHelper = DatabaseHelperImpl.getInstance(context);

                ArrayList<PieModel> slices = new ArrayList<>();
                HashMap<StatusEnum.StatusType, Integer> mData = dbHelper.getStatusHistory();
                String persianLabel;
                String statusColor;

                // Set chart data
                if (isValidDataset(mData)) {
                    for (StatusEnum.StatusType type : mData.keySet()) {
                        persianLabel = UtilWrapper.getStatusLabel(context, type);
                        statusColor = String.format("#%06X",
                                UtilWrapper.getStatusColor(context, type));
                        slices.add(new PieModel(persianLabel, mData.get(type),
                                Color.parseColor(statusColor)));
                    }
                    return slices;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<PieModel> result) {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            if (result == null) {
                mChart.clearChart();

                // No focused textviews
                for (TextView t : mViewLabels.values()) {
                    t.setTextColor(Color.LTGRAY);
                }

            } else {
                for (PieModel slice : result) {
                    slice.setShowLabel(true);
                    slice.setShowFormattedValue(true);
                    slice.setFormattedValue(Utils
                            .formatNumber(Math.round(slice.getValue())));
                    if (slice.getValue() > 0) {
                        mChart.addPieSlice(slice);
                        TextView targetTextView = mViewLabels.get(slice.getLegendLabel());
                        if (targetTextView != null) {
                            mLegendMap.put(mChart.getData().indexOf(slice), targetTextView);
                            mIndexMap.put(targetTextView, mChart.getData().indexOf(slice));
                        }
                    }
                }
                mChart.setUseCustomInnerValue(true);
                if (!mChart.getData().isEmpty()) {
                    mChart.setCurrentItem(0);
                }
                mChart.startAnimation();
            }
        }

        // Check if at least one category has reported data.
        private boolean isValidDataset(HashMap<StatusEnum.StatusType, Integer> dataSet) {
            boolean hasData = false;
            for (StatusEnum.StatusType type : dataSet.keySet()) {
                if (dataSet.get(type) > 0) {
                    hasData = true;
                    break;
                }
            }
            return hasData;
        }
    }


    private static final class LineChartTask extends AsyncTask<Void, Void, ValueLineSeries> {
        WeakReference<Context> contextWeakReference;
        private ProgressDialog mProgressDialog;
        private ValueLineChart mChart;
        private TextView noDataMessage;
        private Task calculationType;

        public LineChartTask(WeakReference<Context> contextWeakReference, ValueLineChart chart,
                             TextView noDataMessage, Task calculationType) {
            this.contextWeakReference = contextWeakReference;
            this.mChart = chart;
            this.calculationType = calculationType;
            this.noDataMessage = noDataMessage;
        }

        @Override
        protected void onPreExecute() {
            if (contextWeakReference != null) {
                Context context = contextWeakReference.get();
                mProgressDialog = new ProgressDialog(context);
                mProgressDialog.show();
            }
        }

        @Override
        protected ValueLineSeries doInBackground(Void... voids) {
            if (contextWeakReference != null) {
                Context context = contextWeakReference.get();
                PresenterContracts.DatabasePresenter dbHelper = DatabaseHelperImpl.getInstance(context);

                ValueLineSeries series = new ValueLineSeries();
                series.setColor(com.hamdam.hamdam.R.color.medium_background_purple);
                HashMap<Date, Integer> mData = null;

                // Decide which data to add
                switch (this.calculationType) {
                    case PERIOD:
                        mData = dbHelper.getPeriodLengths();
                        break;
                    case CYCLE:
                        mData = dbHelper.getCycleLengths();
                        break;
                    default:
                        break;
                }

                // Set chart data
                if (mData != null
                        && mData.size() > 1) { // Min two points to plot the graph
                    for (Date d : mData.keySet()) {
                        PersianDate persianDate = DateUtil.gregorianDateToPersian(d);
                        String legendLabel = Utils.getMonthName(persianDate) + " "
                                + Utils.formatNumber(persianDate.getDayOfMonth());
                        series.addPoint(new ValueLinePoint(legendLabel,
                                Utils.formatNumber(mData.get(d)), mData.get(d)));
                    }
                    return series;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(@Nullable ValueLineSeries result) {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            if (result == null) {
                mChart.setVisibility(View.GONE);
                noDataMessage.setVisibility(View.VISIBLE);
            } else {
                mChart.setVisibility(View.VISIBLE);
                mChart.setShowFormattedValues(true);
                noDataMessage.setVisibility(View.GONE);
                mChart.addSeries(result);
                mChart.startAnimation();
            }
        }
    }

    private static class InfographicDataTask extends AsyncTask<Void, Void, ArrayList<DataStat>> {

        WeakReference<Context> contextWeakReference;
        private ProgressDialog mProgressDialog;
        private RecyclerView mRecyclerview;

        public InfographicDataTask(WeakReference<Context> weakContext,
                                   RecyclerView recyclerView) {
            this.contextWeakReference = weakContext;
            this.mRecyclerview = recyclerView;
        }

        @Override
        protected void onPreExecute() {
            if (contextWeakReference != null) {
                Context context = contextWeakReference.get();
                mProgressDialog = new ProgressDialog(context);
                mProgressDialog.show();
            }
        }

        @Override
        protected ArrayList<DataStat> doInBackground(Void... voids) {
            if (contextWeakReference != null) {
                Context context = contextWeakReference.get();
                ArrayList<DataStat> result = new ArrayList<>();
                PresenterContracts.DatabasePresenter dbHelper = DatabaseHelperImpl.getInstance(context);

                // get averages
                DataStat mPeriodLength, mCycleLength;
                Integer averagePeriod = dbHelper.getAveragePeriodLength(),
                        averageCycle = dbHelper.getAverageCycleLength();

                mPeriodLength = new DataStat.DataStatsBuilder()
                        .title(context.getString(com.hamdam.hamdam.R.string.period_length))
                        .value(averagePeriod)
                        .icon(com.hamdam.hamdam.R.drawable.icon_startperiod)
                        .formattedValue(Utils.formatNumber(averagePeriod))
                        .build();

                mCycleLength = new DataStat.DataStatsBuilder()
                        .title(context.getString(com.hamdam.hamdam.R.string.cycle_length))
                        .value(averageCycle)
                        .icon(com.hamdam.hamdam.R.drawable.baricon_calendar)
                        .formattedValue(Utils.formatNumber(averageCycle))
                        .build();

                result.add(mPeriodLength);
                result.add(mCycleLength);

                return result;
            }

            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<DataStat> result) {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }

            InfographicAdapter mAdapter = new InfographicAdapter(result);
            mRecyclerview.setAdapter(mAdapter);
            contextWeakReference = null;

            super.onPostExecute(result);
        }
    }

    @Override
    public void onDestroy() {
        this.mViewLabels = null;
        super.onDestroy();
    }
}
