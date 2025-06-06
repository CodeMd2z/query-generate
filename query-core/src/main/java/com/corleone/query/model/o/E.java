package com.corleone.query.model.o;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class E {

    protected static final String ALIAS_SPLIT = "__";

    private String entity;
    private String target;

    public E(String target) {
        this.target = target;
        if (target != null) {
            int index = target.indexOf(ALIAS_SPLIT);
            if (index > 0) {
                this.entity = target.substring(0, index);
                return;
            }
        }
        this.entity = target;
    }
}
