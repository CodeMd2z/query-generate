package com.corleone.query.model.convert;

import com.alibaba.fastjson2.JSONObject;
import com.corleone.query.model.QGConstant;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class I18nConfig {

    private Map<String, JSONObject> i18n;
    private String keyPrefix;

    public void init(Long id) {
        keyPrefix = QGConstant.i18nPrefix(id);
    }
}
