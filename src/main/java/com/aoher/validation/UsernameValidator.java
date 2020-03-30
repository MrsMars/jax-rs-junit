package com.aoher.validation;

import com.aoher.validation.annotation.ValidUsername;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static com.aoher.util.Constants.USER_NAME_MATCHER;

public class UsernameValidator implements ConstraintValidator<ValidUsername, String> {

    @Override
    public boolean isValid(String username, ConstraintValidatorContext context) {
        return username != null && username.matches(USER_NAME_MATCHER);
    }
}
