package com.hamdam.hamdam.view.dialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.github.ebraminio.droidpersiancalendar.utils.Utils;
import com.hamdam.hamdam.enums.StatusEnum;
import com.hamdam.hamdam.presenter.PresenterContracts;
import com.hamdam.hamdam.util.UtilWrapper;

import com.hamdam.hamdam.presenter.DatabaseHelperImpl;

import org.eazegraph.lib.charts.BarChart;
import org.eazegraph.lib.models.BarModel;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Display individual graph results.
 */
public class GraphDialogFragment extends AppCompatDialogFragment {
    private static BarGraphTask mTask;
    private StatusEnum.StatusType mStatusType;
    private final String TYPE = "StatusType";

    public static GraphDialogFragment newInstance(StatusEnum.StatusType statusType) {
        GraphDialogFragment mFragment = new GraphDialogFragment();
        mFragment.mStatusType = statusType;
        return mFragment;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mStatusType = StatusEnum.StatusType
                    .valueOf(savedInstanceState.getString(TYPE));
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(com.hamdam.hamdam.R.layout.dialog_graph_display, null, false);

        TextView titleView = (TextView) view.findViewById(com.hamdam.hamdam.R.id.status_chart_title);
        titleView.setText(UtilWrapper.getStatusLabel(getActivity(), mStatusType));

        TextView mNoDataMessage = (TextView) view.findViewById(com.hamdam.hamdam.R.id.no_data_message);

        BarChart mChart = (BarChart) view.findViewById(com.hamdam.hamdam.R.id.bar_chart);
        mTask = new BarGraphTask
                (new WeakReference<>(getContext()), mStatusType, mChart, mNoDataMessage);
        mTask.execute();

        builder.setView(view);
        builder.setNeutralButton(getString(com.hamdam.hamdam.R.string.dismiss), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        });

        Dialog dialog = builder.create();
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        return dialog;
    }

    private static class BarGraphTask extends AsyncTask<Void, Void, ArrayList<BarModel>> {
        private WeakReference<Context> mWeakContext;
        private StatusEnum.StatusType mType;
        private ProgressDialog mDialog;
        private PresenterContracts.DatabasePresenter mPresenter;
        private TextView mNoDataMessage;
        private BarChart mChart;

        public BarGraphTask(WeakReference<Context> contextWeakReference,
                            StatusEnum.StatusType type,
                            BarChart chart,
                            TextView noDataMessage) {
            this.mWeakContext = contextWeakReference;
            this.mType = type;
            this.mChart = chart;
            this.mNoDataMessage = noDataMessage;
        }

        @Override
        protected void onPreExecute() {
            Context context = mWeakContext.get();
            if (context != null) {
                mDialog = new ProgressDialog(context);
                mDialog.show();
            }
        }

        @Override
        @Nullable
        protected ArrayList<BarModel> doInBackground(Void... voids) {
            boolean isEmptyGraph = true;

            Context context = mWeakContext.get();
            if (context != null) {
                mPresenter = DatabaseHelperImpl.getInstance(context);
                HashMap<StatusEnum.StatusValue, Integer> map
                        = mPresenter.getStatusValueSummary(mType);

                if (!map.keySet().isEmpty()) {
                    ArrayList<BarModel> barModels = new ArrayList<>();

                    // Array resource containing all colors for questions
                    int[] colors = context.getResources().getIntArray(com.hamdam.hamdam.R.array.barGraphColors);

                    for (StatusEnum.StatusValue value : map.keySet()) {

                        // If all values in a given category are zero,
                        // replace graph with 'no data' message.
                        // If at least one nonzero value, display the graph.
                        if (map.get(value) > 0 && isEmptyGraph) {
                            isEmptyGraph = false;
                        }
                        String label = UtilWrapper.getValueLabel(context, value);
                        int index = value.getOrdinal().getValue(); // max 3; never out of bounds
                        BarModel model = new BarModel(label, Utils.formatNumber(map.get(value)),
                                map.get(value), colors[index]);
                        model.setShowLabel(true);
                        model.setShowFormattedValue(true);
                        barModels.add(model);
                    }
                    if (!isEmptyGraph) {
                        return barModels;
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(@Nullable ArrayList<BarModel>
                                             result) {
            if (mDialog.isShowing()) {
                mDialog.dismiss();
            }
            if (result == null) {
                mNoDataMessage.setVisibility(View.VISIBLE);
                mChart.setVisibility(View.GONE);
            } else { // format chart, start animation
                mChart.setFixedBarWidth(true); // Set to true or add entries and then set false.
                mChart.setShowFormattedValues(true);
                mChart.setAnimationTime(200);
                mChart.setBarWidth(90);
                mChart.addBarList(result);
                mChart.startAnimation();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(TYPE, mStatusType.name());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStop() {
        if (mTask != null) {
            mTask.cancel(false);
            mTask = null;
        }
        super.onStop();
    }

}
