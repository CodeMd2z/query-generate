package com.corleone.query.model.convert;

import com.alibaba.fastjson2.JSONObject;
import com.corleone.query.model.QGConstant;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import static com.corleone.query.model.QGConstant.KEY;
import static com.corleone.query.model.QGConstant.LABEL;


@Data
@NoArgsConstructor
public class FormConfig {

    private List<JSONObject> form;
    private JSONObject editForms;
    private String service;

    public void init(Long id) {
        for (JSONObject o : form) {
            o.put(LABEL, QGConstant.i18nPrefix(id) + o.get(KEY));
        }
    }
}
