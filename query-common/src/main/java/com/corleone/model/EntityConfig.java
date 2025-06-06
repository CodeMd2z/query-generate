package com.corleone.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
public class EntityConfig {

    private String clazzName;
    private String table;
    private Map<String, String> columns;
    private Set<String> numFields;
}
