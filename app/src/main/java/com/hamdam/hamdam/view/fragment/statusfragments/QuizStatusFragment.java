package com.hamdam.hamdam.view.fragment.statusfragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatImageButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hamdam.hamdam.enums.StatusEnum;
import com.hamdam.hamdam.model.DailyStatus;
import com.hamdam.hamdam.presenter.PresenterContracts;
import com.hamdam.hamdam.service.eventbus.PagerEvent;
import com.hamdam.hamdam.util.DateUtil;
import com.hamdam.hamdam.service.eventbus.StatusListEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Set;

public class QuizStatusFragment extends Fragment implements PresenterContracts.QuestionListener {
    private static final String TAG = "QuizStatusFragment";

    private View fragmentView;
    protected static final String MULTIANSWER = "MultiAnswers";
    protected int mPositionId;
    protected static final String POSITION = "Position";
    protected boolean allowMultiAnswer; // Whether multiple status answers can be selected

    protected PresenterContracts.QuestionView mContext; // Activity: UI logic, current Date, calls to presenter

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            Bundle b = getArguments();
            mPositionId = b.getInt(POSITION);
            allowMultiAnswer = b.getBoolean(MULTIANSWER);
        } else {
            mPositionId = savedInstanceState.getInt(POSITION);
            allowMultiAnswer = savedInstanceState.getBoolean(MULTIANSWER);
        }
    }

    // Change other button states to "deselected." This method is called only when multiple selections are
    // not supported.
    // note: see https://stackoverflow.com/questions/2604599/android-imagebutton-with-a-selected-state
    public void deselectOtherButtons(View view, int currentId) {
        AppCompatImageButton button;
        Set<Integer> allButtons = mContext.getQuizButtons();
        for (int b : allButtons) {
            if (b != currentId) {
                button = (AppCompatImageButton) view.findViewById(b);
                if (!(button.getVisibility() == View.GONE)) {
                    button.setSelected(false);
                }
            }
        }
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

    public QuizStatusFragment() {
        // Required empty public constructor
    }

    public static QuizStatusFragment newInstance(int positionId, boolean allowMultiClicks) {
        QuizStatusFragment fragment = new QuizStatusFragment();
        Bundle b = new Bundle();
        b.putInt(POSITION, positionId);
        b.putBoolean(MULTIANSWER, allowMultiClicks);
        fragment.setArguments(b);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                          Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(com.hamdam.hamdam.R.layout.fragment_status_4option, container, false);
        setDrawableButtons(fragmentView);
        setButtonLabels(fragmentView);

        return fragmentView;
    }

    /*
     * Subscribe for record postings from database.
     */
    @Subscribe(sticky = true)
    public void onStatusListEvent(StatusListEvent event) {
        if (event.getDate() == mContext.getSelectedDate()) {
            for (DailyStatus status : event.getStatuses()) {
                if (status.getType().getTag() == mPositionId) {
                    EventBus.getDefault().removeStickyEvent(event);
                    setButtonChecked(status.getStatusValue().getOrdinal());

                    // Update the title icon to be coloured in.
                    EventBus.getDefault().postSticky
                            (new PagerEvent(mContext.getSelectedDate(), mPositionId, true));

                }
            }
        }
    }


    @Override
    public void setButtonChecked(StatusEnum.Options which) {

        AppCompatImageButton buttonOne = (AppCompatImageButton) fragmentView.findViewById(com.hamdam.hamdam.R.id.optionOne);
        AppCompatImageButton buttonTwo = (AppCompatImageButton) fragmentView.findViewById(com.hamdam.hamdam.R.id.optionTwo);
        AppCompatImageButton buttonThree = (AppCompatImageButton) fragmentView.findViewById(com.hamdam.hamdam.R.id.optionThree);
        AppCompatImageButton buttonFour = (AppCompatImageButton) fragmentView.findViewById(com.hamdam.hamdam.R.id.optionFour);

        switch (which) {
            case ONE:
                buttonOne.setSelected(true);
                break;
            case TWO:
                buttonTwo.setSelected(true);
                break;
            case THREE:
                buttonThree.setSelected(true);
                break;
            case FOUR:
                buttonFour.setSelected(true);
                break;
        }
    }

    private void setButtonLabels(View view) {
        mContext.setButtonLabelsById(view, mPositionId);
    }

    // Assign images and click handlers to buttons
    public void setDrawableButtons(View view) {
        mContext.setButtonsById(view, mPositionId, new View.OnClickListener() {

                    // If a question allows for only one selected answer, deselect all other buttons.
                    // If a button is selected, tell presenter to add data. If a button is deselected,
                    // tell the presenter to delete data.
                    @Override
                    public void onClick(View view) {
                        AppCompatImageButton b = (AppCompatImageButton) view;
                        b.setSelected(!b.isSelected());
                        if (b.isSelected()) {
                            if (!allowMultiAnswer) {
                                deselectOtherButtons(fragmentView, view.getId());
                                EventBus.getDefault().postSticky
                                        (new PagerEvent(mContext.getSelectedDate(), mPositionId, false));
                            }
                            mContext.getPresenter()
                                    .addDataById(DateUtil
                                                    .persianToGregorianDate(mContext.getSelectedDate()),
                                            mContext.getOptionChoice(view.getId()), mPositionId); // Adding data

                            // When a user selects an answer, update the title icon to be coloured in.
                            EventBus.getDefault().post
                                    (new PagerEvent(mContext.getSelectedDate(), mPositionId, true));

                        } else { // Deselecting/removing data.
                            mContext.getPresenter()
                                    .deleteDataById(DateUtil
                                                    .persianToGregorianDate(mContext.getSelectedDate()),
                                            mContext.getOptionChoice(view.getId()), mPositionId);

                            // Check if all buttons are deselected; if so, deselect icons.
                            Set<Integer> buttons = mContext.getQuizButtons();
                            boolean anySelected = false;
                            for (Integer buttonId : buttons) {
                                AppCompatImageButton button =
                                        (AppCompatImageButton) fragmentView.findViewById(buttonId);
                                if (button.isSelected()) {
                                    anySelected = true;
                                    break;
                                }
                            }

                            if (!anySelected) {
                                EventBus.getDefault().post
                                        (new PagerEvent(mContext.getSelectedDate(),
                                                mPositionId, false));
                            }

                        }
                    }
                }

        );
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(POSITION, mPositionId);
        super.onSaveInstanceState(outState);
    }
}
