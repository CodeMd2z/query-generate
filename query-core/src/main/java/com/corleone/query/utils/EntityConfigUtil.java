package com.corleone.query.utils;

import com.corleone.model.EntityConfig;
import com.corleone.model.GenerateEntityConfigInterface;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class EntityConfigUtil {

    @Resource
    ApplicationContext context;

    private static Collection<GenerateEntityConfigInterface> configBeans;

    @PostConstruct
    public void init() {
        configBeans = context.getBeansOfType(GenerateEntityConfigInterface.class).values();
    }

    public static Map<String, EntityConfig> get(Collection<String> clazz) {
        Map<String, EntityConfig> result = new HashMap<>();
        if (Objects.nonNull(clazz)) {
            for (String name : clazz) {
                EntityConfig config = get(name);
                if (Objects.isNull(config)) {
                    continue;
                }
                result.put(name, config);
            }
        }
        return result;
    }

    public static EntityConfig get(String name) {
        EntityConfig config = null;
        for (GenerateEntityConfigInterface bean : configBeans) {
            EntityConfig temp = bean.getConfig(name);
            if (Objects.nonNull(temp)) {
                config = temp;
                break;
            }
        }
        return config;
    }

    public static Set<String> getNumField(String name) {
        return get(name).getNumFields();
    }

    public static Set<String> entitiesSet() {
        Set<String> es = new HashSet<>();
        configBeans.forEach(o -> es.addAll(o.keySet()));
        return es;
    }
}
