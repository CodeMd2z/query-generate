package com.corleone.query.model.o;


import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TACV extends TAC {
    private String value;

    public TACV(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
