package com.corleone.annotations;

import java.lang.annotation.*;

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EntityFieldConfig {

    String name() default "";

    String column() default "";
}
