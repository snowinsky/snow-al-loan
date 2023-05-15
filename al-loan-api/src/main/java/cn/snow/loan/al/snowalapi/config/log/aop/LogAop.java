package cn.snow.loan.al.snowalapi.config.log.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import cn.snow.loan.utils.JsonUtil;


/**
 * @author dddt dev
 */
@Component
@Aspect
@SuppressWarnings("all")
public class LogAop {

    /**
     * from object array to String
     *
     * @param args object array
     * @return
     */
    private String assembleArgsString(Object[] args) {
        if (args == null || args.length <= 0) {
            return "";
        }
        StringBuilder buffer = new StringBuilder();
        for (Object arg : args) {
            if (arg != null) {
                buffer.append(JsonUtil.toJson(arg));
                buffer.append("|");
            }
        }
        return buffer.toString();
    }

    @Before(value = "@annotation(cn.snow.loan.al.snowalapi.config.log.aop.annotation.PrintInAndOutLog)")
    public void beforeMethod(JoinPoint joinPoint) {
        Object target = joinPoint.getTarget();
        @SuppressWarnings("rawtypes")
        Class targetClass = target.getClass();
        Logger logger = LoggerFactory.getLogger(targetClass);
        Signature method = joinPoint.getSignature();
        Object[] args = joinPoint.getArgs();
        if (logger.isInfoEnabled()) {
            logger.info("####==>method={}; input={}", method.getName(), this.assembleArgsString(args));
        }
    }

    @AfterReturning(returning = "result", pointcut = "@annotation(cn.snow.loan.al.snowalapi.config.log.aop.annotation.PrintInAndOutLog)")
    public void afterReturningMethod(JoinPoint joinPoint, Object result) {
        Object target = joinPoint.getTarget();
        @SuppressWarnings("rawtypes")
        Class targetClass = target.getClass();
        Logger logger = LoggerFactory.getLogger(targetClass);
        Signature method = joinPoint.getSignature();
        if (result != null) {
            logger.info("####<==method={}; output={}", method.getName(), JsonUtil.toJson(result));
        }
    }

}
