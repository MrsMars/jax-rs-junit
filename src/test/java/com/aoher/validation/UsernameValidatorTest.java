package com.aoher.validation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class UsernameValidatorTest {

    private final String username;
    private final boolean expectedResult;

    public UsernameValidatorTest(String username, boolean expectedResult) {
        this.username = username;
        this.expectedResult = expectedResult;
    }

    @Parameterized.Parameters
    public static List<Object[]> getParameters() {
        Object[][] objects = new Object[][] {
                {null, false},
                {"", false},
                {" \t\n ", false},
                {" username", false},
                {"username ", false},
                {"usernam", false},
                {"us3rname", false},
                {"user@domain.com", false},
                {"username", true},
                {"myawesomeusername", true},
                {"someusernamethatiswaytoolongbutIdontreallyknowanynamelikethatso" +
                        "thatswhyImjusttypingwhatevercomestomindhereletshopeitsovertwo" +
                        "hundredandfiftysixcharacterslongsothetestwillfailbutitsprobably" +
                        "notsoIbettercopypasteitsomeusernamethatiswaytoolongbutIdont" +
                        "reallyknowanynamelikethatsothatswhyImjusttypingwhatevercomesto" +
                        "mindhereletshopeitsovertwohundredandfiftysixcharacterslongsothe" +
                        "testwillfailbutitsprobablynotsoIbettercopypasteit", false}
        };
        return Arrays.asList(objects);
    }

    @Test
    public void testUsernameValidation() {
        UsernameValidator validator = new UsernameValidator();
        assertEquals(expectedResult, validator.isValid(username, null));
    }
}
