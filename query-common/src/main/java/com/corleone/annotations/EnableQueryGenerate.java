package com.corleone.annotations;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableQueryGenerate {
    /**
     * @return 别称, eg. 用户信息
     */
    String name() default "";

    /**
     * @return 表名, eg. user_info
     */
    String table() default "";
}
