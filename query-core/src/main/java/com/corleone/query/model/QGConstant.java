package com.corleone.query.model;

public interface QGConstant {
    String I18N_KEY_PREFIX_FORMAT = "label.query-config.jsc.%s.";

    String LABEL = "label";
    String KEY = "key";

    static String i18nPrefix(Long id) {
        return String.format(I18N_KEY_PREFIX_FORMAT, id);
    }
}
