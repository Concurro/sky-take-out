package com.sky.annotation;



// 自定义注解，用于自动填充实体类的创建时间、创建人、更新时间、更新人字段

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoFill {
    /**
     * 操作类型：INSERT或UPDATE
     * @return 操作类型
     */
    OperationType value();

}
