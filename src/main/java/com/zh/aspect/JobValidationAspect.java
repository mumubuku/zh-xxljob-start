package com.zh.aspect;


import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Aspect to validate parameters of methods annotated with {@link com.zh.annotation.XxlJobTask}.
 */
@Aspect
@Component
public class JobValidationAspect {

    @Autowired
    private Validator validator;

    @Around("@annotation(com.zh.annotation.XxlJobTask)")
    public Object validateParams(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Object target = joinPoint.getTarget();
        Set<ConstraintViolation<Object>> violations =
                validator.forExecutables().validateParameters(target, method, joinPoint.getArgs());
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            throw new ConstraintViolationException(message, violations);
        }
        return joinPoint.proceed();
    }
}