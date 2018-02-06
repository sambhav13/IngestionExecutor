package com.app.ingestion.validator.design;

import java.util.List;
import java.util.Map;

/**
 * Created by sgu197 on 9/24/2017.
 */
public interface Validator<T> {

    Map<T,Boolean> isValid(List<T> metaList);
    Boolean isValid(T meta);
}
