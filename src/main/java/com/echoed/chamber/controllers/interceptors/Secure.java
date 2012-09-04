package com.echoed.chamber.controllers.interceptors;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Secure {

    boolean redirect() default true;

	String redirectToPath() default "";

	String redirectParam() default "redirect";

	boolean addRedirectParam() default true;

}
