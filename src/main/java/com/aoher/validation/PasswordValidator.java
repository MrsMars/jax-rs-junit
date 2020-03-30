package com.aoher.validation;

import com.aoher.validation.annotation.ValidPassword;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static com.aoher.util.Utilities.cleanUp;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        return password != null && password.equals(cleanUp(password)) && password.length() >= 8 && password.length() <= 255;
    }
}
