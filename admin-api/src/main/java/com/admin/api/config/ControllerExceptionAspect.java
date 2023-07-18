package com.admin.api.config;

import com.admin.api.constant.ResultEnums;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 自定义Controller异常处理AOP
 * @Author wang_dgang
 * @Date 2019/6/5 0005 9:48
 */
@Component
@Aspect
public class ControllerExceptionAspect {

    private static final Logger log = LoggerFactory.getLogger(ControllerExceptionAspect.class);

    /**
     * 环绕通知, 围绕着方法执行
     * 切入点是带有@ControllerExceptionHandler注解的方法
     * @param joinPoint
     * @return
     */
    @Around("@annotation(com.admin.api.config.ControllerExceptionHandler)")
    public Object controllerMethodHandler(ProceedingJoinPoint joinPoint) {
        try {
            return joinPoint.proceed();
        } catch (Throwable throwable) {
            // 如果是业务异常，直接抛出
            if (throwable instanceof BusinessException) {
                throw (BusinessException) throwable;
            } else {
                // 非业务异常，记录异常之后，包装成业务异常在抛出
                log.error("Controller: {}", throwable.getMessage(), throwable);
                throw new BusinessException(ResultEnums.ERROR);
            }
        }
    }

}
