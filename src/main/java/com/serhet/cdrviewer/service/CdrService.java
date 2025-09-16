package com.serhet.cdrviewer.service;

import com.serhet.cdrviewer.exception.CdrNotFoundException;
import com.serhet.cdrviewer.model.CdrBlock;
import com.serhet.cdrviewer.util.KeyNormalizer;
import com.serhet.cdrviewer.util.ValueNormalizer;
import com.serhet.cdrviewer.util.CdrParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Service katmani.
 * CDR dosyalari uzerinde arama ve filtreleme mantigini barindirir.
 */
@Service
public class CdrService {

    @Autowired
    private CdrParser parser;

    /**
     * Verilen dosya yolunda key / value kriterlerine gore CdrBlock listesi dondurur.
     *
     * @param filePath Dosya yolu
     * @param key      Aranacak key (opsiyonel)
     * @param value    Aranacak value (opsiyonel)
     * @return Eslestirilen CdrBlock listesi
     * @throws CdrNotFoundException Eslestirme yoksa atilir
     */
    public List<CdrBlock> filterCdrBlocks(String filePath, String key, String value) {
        List<CdrBlock> result = new ArrayList<>();

        String k = KeyNormalizer.normalize(key);
        String v = ValueNormalizer.normalize(value);

        boolean wantEmpty = v != null && v.equalsIgnoreCase("bos");

        try (Stream<CdrBlock> stream = parser.streamFile(filePath)) {
            stream.forEach(block -> {
                Map<String, String> kv = block.getFlatKeyValues(); // Flatten map
                boolean match = false;

                if (k != null && !k.isEmpty() && (v == null || v.isEmpty())) {
                    // Sadece key verildi -> key var mi?
                    match = kv.containsKey(k);

                } else if ((k == null || k.isEmpty()) && v != null && !v.isEmpty()) {
                    // Sadece value verildi
                    if (wantEmpty) {
                        for (String val : kv.values()) {
                            if (val == null || val.isEmpty()) {
                                match = true;
                                break;
                            }
                        }
                    } else {
                        match = kv.containsValue(v);
                    }

                } else if (k != null && !k.isEmpty() && v != null && !v.isEmpty()) {
                    // Hem key hem value verildi
                    if (wantEmpty) {
                        match = kv.containsKey(k) && (kv.get(k) == null || kv.get(k).isEmpty());
                    } else {
                        match = kv.containsKey(k) && v.equals(kv.get(k));
                    }
                }

                if (match) {
                    result.add(block);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("Dosya okunamadi: " + e.getMessage(), e);
        }

        if (result.isEmpty()) {
            throw new CdrNotFoundException("Verilen kriterlerle eslesen kayit bulunamadi!");
        }

        return result;
    }
}