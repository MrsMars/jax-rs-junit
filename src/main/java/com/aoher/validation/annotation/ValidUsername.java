package com.aoher.validation.annotation;

import com.aoher.validation.UsernameValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.aoher.util.Constants.USER_USERNAME;

@Constraint(validatedBy = UsernameValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidUsername {
    String message() default USER_USERNAME;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
