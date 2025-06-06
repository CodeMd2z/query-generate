package com.corleone.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ClazzConfig {

    private String name;
    private String label;
    private List<FieldConfig> fields;
}
