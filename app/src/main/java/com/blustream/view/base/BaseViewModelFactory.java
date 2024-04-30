package com.blustream.view.base;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class BaseViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    @NonNull
    private final Application application;

    public BaseViewModelFactory(@NonNull Application application) {
        this.application = application;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new BaseViewModel(application);
    }
}
