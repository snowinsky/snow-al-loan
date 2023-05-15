package cn.snow.loan.al.snowalapi.config.log.aop.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * this annotation is used for printing in parameters log and out parameters
 *
 * @author Jesse.Zhang1
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface PrintInAndOutLog {

}
