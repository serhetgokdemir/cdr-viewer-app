package com.serhet.cdrviewer.util;

/**
 * Value icin normalizasyon islemleri yapar.
 * - Dis ve ic tirnaklari kaldirir.
 * - Sondaki H/D eklerini temizler (ornek: "10H" -> "10").
 */
public class ValueNormalizer {

    /**
     * Gelen value'yu temizler ve normalize eder.
     * Ornek:  "'12 H'"  ->  "12"
     *
     * @param value girilen value
     * @return normalize edilmis value
     */
    public static String normalize(String value) {
        if (value == null) return null;
        String v = value.trim();

        // dis tirnaklari kaldir
        if ((v.startsWith("\"") && v.endsWith("\"")) ||
                (v.startsWith("'") && v.endsWith("'"))) {
            v = v.substring(1, v.length() - 1);
        }

        // ic tirnaklari kaldir
        v = v.replace("\"", "").replace("'", "").trim();

        // sona eklenmis H veya D varsa kaldir
        if (v.endsWith(" H")) {
            v = v.substring(0, v.length() - 2).trim();
        } else if (v.endsWith("H") || v.endsWith("D")) {
            v = v.substring(0, v.length() - 1).trim();
        }

        return v;
    }
}