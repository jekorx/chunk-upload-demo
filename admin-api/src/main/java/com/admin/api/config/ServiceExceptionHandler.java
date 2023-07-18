package com.admin.api.config;

import java.lang.annotation.*;

/**
 * 自定义Service异常注解
 * @author wang_dgang
 * @date 2019/6/5 0005 9:47
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ServiceExceptionHandler {
}
