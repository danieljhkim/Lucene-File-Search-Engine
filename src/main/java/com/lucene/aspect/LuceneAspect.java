package com.lucene.aspect;

import com.lucene.util.logging.CustomLogger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

import java.util.Arrays;
import java.util.logging.Logger;


@Aspect
public class LuceneAspect {

    private static final Logger logger = CustomLogger.getLogger(LuceneAspect.class.getName());

    @Around("execution(* com.lucene..*.*(..)) && !within(com.lucene.util.logging..*)")
   public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
       long start = System.currentTimeMillis();
       Object result = joinPoint.proceed();
       long executionTime = System.currentTimeMillis() - start;
       logger.info(joinPoint.getSignature() + " executed in " + executionTime + "ms");
       return result;
   }

    @Before("execution(* *(..)) && (within(*..indexer+) || within(*..searcher+))")
    public void logBefore(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getName();
        Object[] args = joinPoint.getArgs();
        logger.info("Entering method " + className + "." + methodName + " with args: " + Arrays.toString(args));
    }

    @AfterReturning(pointcut = "execution(* *(..)) && (within(*..indexer+) || within(*..searcher+))", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getName();
        logger.info("Exiting method " + className + "." + methodName + " with return value: " + result);
    }

}
