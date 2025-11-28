package com.sky.aspect;


import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面，用于自动填充实体类的创建时间、创建人、更新时间、更新人字段
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {

    }

    /**
     * 自动填充公共字段（创建时间、创建人、更新时间、更新人）的通知方法
     * 
     * 该方法在Mapper层方法执行前被调用，通过反射机制为实体对象自动设置公共字段值，
     * 避免在每个方法中重复编写相同的字段设置代码。
     * 
     * @param joinPoint 连接点对象，包含被拦截方法的信息和参数
     */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint){
        log.info("自动填充开始");
    
        // 1、获取当前被拦截的方法上的数据库操作类型
        // 通过JoinPoint获取方法签名，再获取方法上的AutoFill注解，最后从注解中获取操作类型
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        AutoFill annotation = signature.getMethod().getAnnotation(AutoFill.class);
        OperationType value = annotation.value();
        
        // 2、根据数据库操作类型，为对应的字段赋值
        // 获取被拦截方法的参数列表，通常第一个参数是实体对象
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return; // 如果没有参数，直接返回
        }
        
        // 获取第一个参数，即实体对象
        Object entity = args[0];
        
        // 获取当前时间和当前登录用户ID
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();
        
        try {
            // 如果是插入操作，需要设置创建时间和创建人
            if (value == OperationType.INSERT) {
                // 通过反射获取并调用setCreateTime方法，设置创建时间
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                setCreateTime.invoke(entity, now);
                
                // 通过反射获取并调用setCreateUser方法，设置创建人ID
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                setCreateUser.invoke(entity, currentId);
            }
            
            // 无论是插入还是更新操作，都需要设置更新时间和更新人
            // 通过反射获取并调用setUpdateTime方法，设置更新时间
            Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            setUpdateTime.invoke(entity, now);
            
            // 通过反射获取并调用setUpdateUser方法，设置更新人ID
            Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
            setUpdateUser.invoke(entity, currentId);
        } catch (Exception e) {
            // 如果反射调用过程中出现异常，记录错误日志
            log.error("自动填充更新时间、更新人失败", e);
        }
    }
}