package com.yoloo.android.data.repository;

import com.yoloo.android.data.model.QuestionModel;
import com.yoloo.android.data.repository.specification.Specification;

import java.util.List;

import rx.Observable;

public class QuestionRepository implements Repository<QuestionModel> {

    @Override
    public void create(QuestionModel item) {

    }

    @Override
    public void update(QuestionModel item) {

    }

    @Override
    public void delete(QuestionModel item) {

    }

    @Override
    public void delete(Specification specification) {

    }

    @Override
    public Observable<List<QuestionModel>> query(Specification specification) {
        return null;
    }
}
