package com.blustream.view.base;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;

import io.blustream.sulley.repository.Repository;
import io.blustream.sulley.repository.RepositoryImpl;

public class BaseViewModel extends AndroidViewModel {
    private Repository repository;

    public BaseViewModel(Application app) {
        super(app);
        repository = RepositoryImpl.getRepository(app);
    }

    public Repository getRepository() {
        return repository;
    }
}
