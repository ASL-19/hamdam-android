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
import android.support.v7.widget.AppCompatImageView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.github.ebraminio.droidpersiancalendar.utils.Utils;

import com.hamdam.hamdam.R;
import com.hamdam.hamdam.enums.StatusEnum;
import com.hamdam.hamdam.presenter.DatabaseHelperImpl;
import com.hamdam.hamdam.presenter.PresenterContracts;
import com.hamdam.hamdam.util.UtilWrapper;

import java.lang.ref.WeakReference;
import java.util.HashMap;

/**
 * Display graphics for single-status item (where bar graph would not make sense).
 */
public class SingleStatusDialogFragment extends AppCompatDialogFragment {
    private TextView mTextSummary, mNoDataMessage;
    private AppCompatImageView mIcon;
    private static SingleStatusGraphTask mTask;
    private StatusEnum.StatusType mStatusType;
    private final String TYPE = "StatusType";

    public static SingleStatusDialogFragment newInstance(StatusEnum.StatusType statusType) {
        SingleStatusDialogFragment mFragment = new SingleStatusDialogFragment();
        mFragment.mStatusType = statusType;
        return mFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mStatusType = StatusEnum.StatusType.valueOf(savedInstanceState.getString(TYPE));
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.dialog_stats_single_item, null, false);

        TextView titleView = (TextView) view.findViewById(R.id.status_chart_title);
        titleView.setText(UtilWrapper.getStatusLabel(getContext(), mStatusType));

        mNoDataMessage = (TextView) view.findViewById(R.id.no_data_message);
        mTextSummary = (TextView) view.findViewById(R.id.text_data_summary);
        mIcon = (AppCompatImageView) view.findViewById(R.id.graph_icon);

        mTask = new SingleStatusGraphTask
                (new WeakReference<>(getContext()),
                        mStatusType, mIcon, mTextSummary, mNoDataMessage);
        mTask.execute();

        builder.setView(view);
        builder.setNeutralButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
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

    private static class SingleStatusGraphTask extends AsyncTask<Void, Void, String> {
        private WeakReference<Context> mWeakContext;
        private StatusEnum.StatusType mType;
        private ProgressDialog mDialog;
        private PresenterContracts.DatabasePresenter mPresenter;
        private TextView mTextSummary, mNoDataMessage;
        private AppCompatImageView mIcon;

        public SingleStatusGraphTask(WeakReference<Context> contextWeakReference,
                            StatusEnum.StatusType type,
                            AppCompatImageView icon,
                                     TextView summary,
                            TextView noDataMessage) {
            this.mWeakContext = contextWeakReference;
            this.mType = type;
            this.mIcon = icon;
            this.mTextSummary = summary;
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

        @Override @Nullable
        protected String doInBackground(Void... voids) {
            String result = null;
            Context context = mWeakContext.get();
            if (context != null) {
                mPresenter = DatabaseHelperImpl.getInstance(context);
                HashMap<StatusEnum.StatusValue, Integer> map
                        = mPresenter.getStatusValueSummary(mType);
                if (!map.keySet().isEmpty()) { // 1 element max
                    for (StatusEnum.StatusValue value : map.keySet()) {
                        if (map.get(value) > 0) {
                            result = Utils.formatNumber(map.get(value));
                        }
                    }
                    return result; // Only element in map
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(@Nullable String
                                             result) {
            if (mDialog.isShowing()) {
                mDialog.dismiss();
            }
            if (mType.equals(StatusEnum.StatusType.EXERCISE)) {
                mIcon.setBackgroundResource(R.drawable.icon_exercise_s);
            } else { // will never happen-- graphic is only created if StatusType is EXERCISE
                mIcon.setBackgroundResource(R.drawable.baricon_hamdam);
            }

            if (result == null) {
                mNoDataMessage.setVisibility(View.VISIBLE);
                mTextSummary.setVisibility(View.GONE);
            } else {
                mTextSummary.setText(result);
            }
        }
    }

    @Override
    public void onStop() {
        if (mTask != null) {
            mTask.cancel(false);
            mTask = null;
        }
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(TYPE, mStatusType.name());
        super.onSaveInstanceState(outState);
    }
}
