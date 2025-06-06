package com.corleone.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldConfig {

    public static final FieldConfig PK = new FieldConfig("id", "ID", "Long", false);

    private String key;
    private String label;
    private String type;
    private Boolean typeIsNum;
}
