package com.hamdam.hamdam.di;

import com.hamdam.hamdam.view.activity.MainActivity;
import com.hamdam.hamdam.view.activity.OnboardingActivity;
import com.hamdam.hamdam.presenter.DatabaseHelperImpl;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {

    void inject(BaseApplication application);

    void inject(OnboardingActivity onboardingActivity);

    void inject(MainActivity mainActivity);

    void inject(DatabaseHelperImpl dataBaseHelper);
}
