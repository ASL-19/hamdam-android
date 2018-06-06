package com.hamdam.hamdam.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.AppCompatImageButton;
import android.view.View;
import android.widget.TextView;

import com.github.ebraminio.droidpersiancalendar.calendar.PersianDate;
import com.hamdam.hamdam.enums.StatusEnum;

import com.hamdam.hamdam.model.DailyStatus;
import com.hamdam.hamdam.model.StaticFact;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interface class contracts for presenters and views.
 */
public interface PresenterContracts {

    /**
     * Required by in quiz views
     */
    interface IconView {
        AppCompatImageButton getIcon();

        void setInactiveIcon(int position);

        void setActiveIcon(int position);
    }

    /**
     * Required by onboarding fragments
     */
    interface OnboardView {
        void setInstructions(TextView view, int position);
    }

    /**
     * Required to set nav drawer properly.
     */
    interface NavigationView {
        void setNavigationPosition(int newPosition);
        int getNavigationPosition(String tag);
    }

    interface QuestionListener {
        void setButtonChecked(StatusEnum.Options which);
    }

    /**
     * Required by quiz fragments
     */
    interface QuestionView {

        PersianDate getSelectedDate();

        QuestionPresenter getPresenter();

        Set<Integer> getQuizButtons();

        void setButtonsById(View view, int position,
                            View.OnClickListener listener); // Initial button resource setup

        StatusEnum.Options getOptionChoice(int click);

        int getCurrentPosition();

        void setButtonLabelsById(View view, int pageId);

    }

    /**
     * Provided by Context/Presenter to handle onClick methods
     */
    interface QuestionPresenter {

        // Prompt database action
        void addDataById(@NonNull Date date, StatusEnum.Options choice, int pageId);

        void deleteDataById(@NonNull Date date, StatusEnum.Options choice, int pageId);

        void loadDailyData(@NonNull final PersianDate date); // Load past data entered

    }

    /**
     * Provided to load drawables on calendar and calculate menstruation statistics, including
     * projecting approximate menstruation/fertility cycles
     */
    interface FertilityPresenter {
        void updatePeriodInfo(@NonNull PersianDate date, Integer duration, Integer tag);

        void deletePeriodInfo(@NonNull PersianDate date, Integer tag);

        void launchPeriodDialog(@NonNull PersianDate date, FragmentManager fm, int tag);

        void daysTillCycle(@NonNull PersianDate persianDate);

        void togglePeriod(@NonNull PersianDate persianDate);

    }

    /**
     * Call database queries and retrieve/store data.
     */
    interface DatabasePresenter {

        ArrayList<DailyStatus> getStatusToday(@NonNull Date date);

        int daysTillStartDate(@NonNull Date date);

        int getAverageCycleLength();

        int getCycleIncrement();

        Map<Date, Integer> projectRecordsBetween(@NonNull Date start, @NonNull Date end, boolean isPeriodProjection);

        int calculateCyclePosition(@NonNull Date date);

        HashMap<Date, Integer> getCycleLengths();

        List<Date> getPastOvulationDates();

        Date calculateOvulationStart(Date date);

        Date getLastStartDate(@NonNull Date current); // search backwards for records preceding current date

        Date projectNextStartDate(@NonNull Date date);

        Date generateEndDate(@NonNull Date startDate, Integer duration);

        boolean updatePeriodStats(@NonNull Date startDate, @Nullable Date endDate, Integer pmsLength);

        boolean updatePeriodStats(@NonNull Date startDate, @NonNull Integer duration);

        boolean isActivePeriodDate(@NonNull Date testDate);

        boolean updateStatus(@NonNull DailyStatus status);

        boolean deleteDailyStatus(DailyStatus status);

        boolean deletePeriod(@NonNull Date startDate);

        @NonNull Map<Date, Integer> getRecordsBetween(@NonNull Date start, @NonNull Date stop);

        int getAveragePeriodLength();

        @NonNull HashMap<Date, Integer> getPeriodLengths();

        @NonNull HashMap<StatusEnum.StatusType, Integer> getStatusHistory();

        @NonNull HashMap<StatusEnum.StatusValue, Integer> getStatusValueSummary(StatusEnum.StatusType type);

        void clearUserHistory();

    }

    interface StaticContentPresenter {
        ArrayList<StaticFact> getStaticContent();
        void showContentFragment(Fragment target, String tag);
    }

    interface StaticContentProvider {
        ArrayList<StaticFact> loadStaticContent(BufferedReader reader, StaticFact.TOPIC_TYPES type);

    }
}
