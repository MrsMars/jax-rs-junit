package com.aoher.domain;

import com.aoher.ArchiveFactory;
import com.aoher.validation.OnPasswordUpdate;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class UserValidationTest {

    @Deployment
    public static WebArchive createArchive() {
        return ArchiveFactory.createArchive();
    }

    @Resource
    private Validator validator;

    @Test
    public void testUsernameValidated() {
        User user = new User();
        user.setUsername("inval1d");
        user.setPassword("supersecret");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(1, violations.size());
    }

    @Test
    public void testPasswordValidated() {
        User user = new User();
        user.setUsername("someuser");
        user.setPassword("2short");

        Set<ConstraintViolation<User>> violations = validator.validate(user, OnPasswordUpdate.class);
        assertEquals(1, violations.size());
    }

    @Test
    public void testPasswordRequired() {
        User user = new User();
        user.setUsername("someuser");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(1, violations.size());
    }
}
