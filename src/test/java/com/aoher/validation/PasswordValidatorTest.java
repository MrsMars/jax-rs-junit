package com.aoher.validation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class PasswordValidatorTest {

    private final String password;
    private final boolean expectedResult;

    public PasswordValidatorTest(String password, boolean expectedResult) {
        this.password = password;
        this.expectedResult = expectedResult;
    }

    @Parameterized.Parameters
    public static List<Object[]> getParameters() {
        Object[][] objects = new Object[][] {
                {null, false},
                {"", false},
                {" \t\n ", false},
                {" password", false},
                {"password ", false},
                {"passwor", false},
                {"password", true},
                {"P4$$vv0rD", true},
                {"super - secret - password", true},
                {"somepasswordthatiswaytoolongbutIdontreallyknowanywordlikethatso" +
                        "thatswhyImjusttypingwhatevercomestomindhereletshopeitsovertwo" +
                        "hundredandfiftysixcharacterslongsothetestwillfailbutitsprobably" +
                        "notsoIbettercopypasteitsomepasswordthatiswaytoolongbutIdont" +
                        "reallyknowanywordlikethatsothatswhyImjusttypingwhatevercomesto" +
                        "mindhereletshopeitsovertwohundredandfiftysixcharacterslongsothe" +
                        "testwillfailbutitsprobablynotsoIbettercopypasteit", false}
        };
        return Arrays.asList(objects);
    }

    @Test
    public void testPasswordValidation() {
        PasswordValidator validator = new PasswordValidator();
        assertEquals(expectedResult, validator.isValid(password, null));
    }
}