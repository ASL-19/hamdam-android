package com.hamdam.hamdam.view.fragment.statusfragments;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ArrayRes;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatImageButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hamdam.hamdam.enums.StatusEnum;
import com.hamdam.hamdam.presenter.PresenterContracts;
import com.hamdam.hamdam.service.eventbus.PagerEvent;
import com.hamdam.hamdam.util.AnimateUtils;
import com.hamdam.hamdam.util.UtilWrapper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;


/**
 * A {@link Fragment} subclass displaying selectable quiz icons.
 * This fragment is recycled for all quiz screens in daily quiz.
 */
public class QuizIconFragment extends Fragment implements PresenterContracts.IconView {
	private static final String TAG = "QuizIconFragment";
    private int mPosition;
    private static final String POSITION = "Position";
    private AppCompatImageButton icon;
    private TextView topicTitle;
    private PresenterContracts.QuestionView mContext;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            Bundle b = getArguments();
            mPosition = b.getInt(POSITION);
        } else {
            mPosition = savedInstanceState.getInt(POSITION);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof PresenterContracts.QuestionView) {
            mContext = (PresenterContracts.QuestionView) context;
        } else {
            Log.e("StatusFragment", "Context missing " +
                    "question or UI interface methods");
        }
    }

    @Override
    public void onDetach() {
        mContext = null;
        super.onDetach();
    }

    public static QuizIconFragment newInstance(int position) {
        QuizIconFragment fragment = new QuizIconFragment();
        Bundle b = new Bundle();
        b.putInt(POSITION, position);
        fragment.setArguments(b);
        return fragment;
    }


    public QuizIconFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(com.hamdam.hamdam.R.layout.fragment_quiz_title, container, false);
        icon = (AppCompatImageButton) view.findViewById(com.hamdam.hamdam.R.id.icon_image);
        icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE); // Keep fit image to centre of parent
        topicTitle = (TextView) view.findViewById(com.hamdam.hamdam.R.id.topic_icon_text);

        icon.setBackgroundColor(Color.TRANSPARENT);
        setInactiveIcon(mPosition); // Individually set to active if matching records found
        setTextLabel(mPosition);

        if (mPosition == mContext.getCurrentPosition()) {
            AnimateUtils.scaleUp(icon, 0); // Animation duration of 0ms
        } else {
            // When created, animate immediately (0ms) with defocused/faded view.
            AnimateUtils.scaleDown(icon, 0);
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(POSITION, mPosition);
        super.onSaveInstanceState(outState);
    }


    @Override
    public AppCompatImageButton getIcon() {
        return icon;
    }

    public TextView getTopicText() { return topicTitle; }

    @Override // Future fix: use selectors
    public void setInactiveIcon(int position) {
        @ArrayRes
        final TypedArray titleIcons = getActivity().getApplicationContext().getResources()
                .obtainTypedArray(com.hamdam.hamdam.R.array.titleIconsUnselected);

        icon.setImageResource(titleIcons.getResourceId
                (position, com.hamdam.hamdam.R.drawable.icon_startperiod));
        titleIcons.recycle();
    }

    @Override
    public void setActiveIcon(int position) {
        final TypedArray activeTitleIcons = getActivity().getApplicationContext().getResources()
                .obtainTypedArray(com.hamdam.hamdam.R.array.titleIconsSelected);

        AppCompatImageButton focusedIcon = getIcon();
        focusedIcon.setImageResource(activeTitleIcons.getResourceId
                (position, com.hamdam.hamdam.R.drawable.icon_startperiod));
        activeTitleIcons.recycle();
    }

    private void setTextLabel(int position) {

        TextView textView = getTopicText();

        StatusEnum.StatusType statusType = StatusEnum.StatusType.getByTag(position);
        if (statusType != null) {
            String label = UtilWrapper.getStatusLabel(getContext(), statusType);
            textView.setText(label);
        }
    }

    // If user has filled out data for this quiz day before, receive data and update UI
    @Subscribe(sticky = true)
    public void onIconChangeEvent(PagerEvent event) {
        if (event.getTag() == this.mPosition
                && event.getDate() == mContext.getSelectedDate()) {
            EventBus.getDefault().removeStickyEvent(event);
            if (event.isEnabled()) {
                setActiveIcon(event.getTag());
            } else {
                setInactiveIcon(event.getTag());
            }
        }
    }
}
