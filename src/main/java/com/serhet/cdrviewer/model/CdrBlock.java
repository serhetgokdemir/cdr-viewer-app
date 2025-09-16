package com.serhet.cdrviewer.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tek bir CDR (Call Detail Record) blogunu temsil eder.
 * - lines: Dosyadan okunan satirlar (girintili orijinal yapi).
 * - fileName: Bu blogun geldigi dosyanin adi.
 * - flatKeyValues: Tum key-value ciftlerinin flatten edilmis map hali.
 * - tempRegex: Key ve value'yu ayirmak icin kullanilan regex (varsayilan "\\s*:\\s*").
 */
public class CdrBlock {

    private List<String> lines; // Satirlar
    private String fileName;    // Dosya adi
    public String tempRegex = "\\s*:\\s*"; // Key-value ayirma regex'i

    // Flatten edilmis key-value map
    private Map<String, String> flatKeyValues = new HashMap<>();

    // Constructor
    public CdrBlock(List<String> lines, String fileName) {
        this.lines = lines;
        this.fileName = fileName;
    }

    // Getter & Setter'lar
    public List<String> getLines() {
        return lines;
    }

    public void setLines(List<String> lines) {
        this.lines = lines;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Map<String, String> getFlatKeyValues() {
        return flatKeyValues;
    }

    public void setFlatKeyValues(Map<String, String> flatKeyValues) {
        this.flatKeyValues = flatKeyValues;
    }
}