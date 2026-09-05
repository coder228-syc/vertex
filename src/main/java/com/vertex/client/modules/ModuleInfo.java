package com.vertex.client.modules;

import com.vertex.client.modules.ModuleCategory;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(value = RetentionPolicy.RUNTIME)
public @interface ModuleInfo {
    String name();
    String desc() default "";
    int key() default 0;
    ModuleCategory type();
    String[] keywords() default {};
}
