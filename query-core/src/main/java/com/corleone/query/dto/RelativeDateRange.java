package com.corleone.query.dto;

import cn.hutool.core.util.ObjectUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RelativeDateRange {
    RelativeDate from;
    RelativeDate to;

    public boolean validate() {
        return !ObjectUtil.hasNull(from, to) && from.validate() && to.validate();
    }
}
