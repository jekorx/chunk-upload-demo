package com.admin.api.config;

import com.admin.api.constant.ResultEnums;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 自定义Service异常处理AOP
 * @Author wang_dgang
 * @Date 2019/6/5 0005 9:52
 */
@Component
@Aspect
public class ServiceExceptionAspect {

    private static final Logger log = LoggerFactory.getLogger(ServiceExceptionAspect.class);

    public ServiceExceptionAspect() {}

    /**
     * 异常通知, 在方法抛出异常之后
     * pointcut：切入点是带有@ServiceExceptionHandler注解的方法
     * @param joinPoint
     * @param throwable
     * @throws BusinessException
     */
    @AfterThrowing(
        pointcut = "@annotation(com.admin.api.config.ServiceExceptionHandler)",
        throwing = "throwable"
    )
    public void afterRuntimeException(JoinPoint joinPoint, Throwable throwable) throws BusinessException {
        // 如果是业务异常，直接抛出
        if (throwable instanceof BusinessException) {
            throw (BusinessException) throwable;
        } else if (throwable instanceof RuntimeException) {
            // 如果是运行时异常，记录异常之后，包装成业务异常在抛出
            log.error("service: {}", throwable.getMessage(), throwable);
            throw new BusinessException(ResultEnums.ERROR);
        }
    }

}
