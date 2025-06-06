package com.corleone.model;

import java.util.Set;

public interface GenerateEntityConfigInterface {

    EntityConfig getConfig(String name);

    Set<String> keySet();
}
