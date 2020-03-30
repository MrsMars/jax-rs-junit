package com.aoher.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;

import static com.aoher.util.Utilities.cleanUp;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(Parameterized.class)
public class UtilitiesCleanUpTest {

    private static final String DEFAULT_VALUE = "default";

    private final String value;
    private final String expectedResult;

    public UtilitiesCleanUpTest(String value, String expectedResult) {
        this.value = value;
        this.expectedResult = expectedResult;
    }

    @Test
    public void testCleanUpWithDefault() {
        assertEquals(expectedResult, cleanUp(value, DEFAULT_VALUE));
    }

    @Test
    public void testCleanUpWithoutDefault() {
        if (expectedResult.equals(DEFAULT_VALUE)) {
            assertNull(cleanUp(value));
        } else {
            assertEquals(expectedResult, cleanUp(value));
        }
    }

    @Parameterized.Parameters
    public static List<Object[]> getParameters() {
        Object[][] objects = new Object[][] {
                {null, DEFAULT_VALUE},
                {"", DEFAULT_VALUE},
                {" ", DEFAULT_VALUE},
                {"\t", DEFAULT_VALUE},
                {"\n", DEFAULT_VALUE},
                {"x", "x"},
                {"  x", "x"},
                {"x  ", "x"},
                {"  x  ", "x"}};
        return Arrays.asList(objects);
    }
}