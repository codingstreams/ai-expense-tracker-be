package com.example.et.core.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class FeatureMethodLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(FeatureMethodLoggingAspect.class);

    // Pointcut matching any method inside any class under a 'features' package structure
    @Pointcut("execution(* *..features..*Controller.*(..)) || " +
        "@within(org.springframework.web.bind.annotation.RestController) && execution(* *..module..*.*(..)) || " +
        "@within(org.springframework.stereotype.Controller) && execution(* *..module..*.*(..))")
    public void inFeaturesPackage() {}

    @Around("inFeaturesPackage()")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();

        log.info("--> START: {}", methodName);
        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            log.info("<-- END: {} [Duration: {}ms]", methodName, duration);
            return result;
        } catch (Throwable ex) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("<-- ERROR: {} failed after {}ms with: {}", methodName, duration, ex.getMessage());
            throw ex;
        }
    }
}