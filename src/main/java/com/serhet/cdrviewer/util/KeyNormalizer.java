package com.serhet.cdrviewer.util;

/**
 * Key icin basit normalizasyon islemleri yapar.
 * - Tirnaklari kaldirir.
 * - Bosluklari temizler.
 */
public class KeyNormalizer {

    /**
     * Gelen key'i temizler ve normalize eder.
     * Ornek:  "\"myKey\""  ->  "myKey"
     *
     * @param key girilen key
     * @return normalize edilmis key
     */
    public static String normalize(String key) {
        if (key == null) return null;
        String k = key.trim();

        // dis tirnaklari kaldir
        if (k.length() >= 2 &&
                ((k.startsWith("\"") && k.endsWith("\"")) ||
                        (k.startsWith("'") && k.endsWith("'")))) {
            k = k.substring(1, k.length() - 1);
        }

        // ic tek tirnaklari sil ve trim
        return k.replace("'", "").trim();
    }
}