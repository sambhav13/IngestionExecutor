package com.app.ingestion.validator.design;

import java.util.List;

/**
 * Created by sgu197 on 9/24/2017.
 */
public interface Criterion<T> {
    boolean addPositiveCriteria(T regex);

    boolean addPositiveCriteria(List<T> regexS);

    boolean addNegativeCriteria(T regex);

    boolean addNegativeCriteria(List<T> regexS);

    boolean removePositiveCriteria(T regex);

    boolean removePositiveCriteria(List<T> regexS);

    boolean removeNegativeCriteria(T regex);

    boolean removeNegativeCriteria(List<T> regexS);

    T producePositiveCriteria();

    T produceNegativeCriteria();
}
