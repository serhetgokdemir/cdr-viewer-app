package com.serhet.cdrviewer.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValueNormalizerTest {

    @Test
    void normalize_Value_String() {
        String result = ValueNormalizer.normalize("\"SUPERONLINE\"");
        assertEquals("SUPERONLINE", result);
    }

    @Test
    void normalize_Value_Numerical_WithD() {
        String result = ValueNormalizer.normalize("\"0\"D");
        assertEquals("0", result);
    }

    @Test
    void normalize_Value_Numerical_WithH() {
        String result = ValueNormalizer.normalize("\"190617\"H");
        assertEquals("190617", result);
    }

    @Test
    void normalize_Value_Null() {
        String result = ValueNormalizer.normalize(null);
        assertNull(result);
    }
}