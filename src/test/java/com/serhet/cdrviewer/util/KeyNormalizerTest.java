package com.serhet.cdrviewer.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KeyNormalizerTest {

    @Test
    void normalize_Key_Normal() {
        String result = KeyNormalizer.normalize("globalCallId");
        assertEquals("globalCallId", result);
    }

    @Test
    void normalize_Key_WithQuotes() {
        String result = KeyNormalizer.normalize("\"sessionID\"");
        assertEquals("sessionID", result);
    }

    @Test
    void normalize_Key_Null() {
        String result = KeyNormalizer.normalize(null);
        assertNull(result);
    }
}