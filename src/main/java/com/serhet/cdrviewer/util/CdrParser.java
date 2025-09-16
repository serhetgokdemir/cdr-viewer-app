package com.serhet.cdrviewer.util;

import org.springframework.stereotype.Component;
import com.serhet.cdrviewer.model.CdrBlock;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/**
 * CDR dosyalarini satir satir okuyup her bir mantiksal blogu (CdrBlock)
 * olusturan ve istenirse flatten map ureten parser.
 */
@Component
public class CdrParser {

    /**
     * Tum dosyayi okuyup tum CdrBlock'lari list olarak dondurur.
     * Her blok icin flatten key-value map de set edilir.
     */
    public List<CdrBlock> parseFile(String path) {
        List<CdrBlock> blocks = new ArrayList<>();
        String fileName = new File(path).getName();

        List<String> current = new ArrayList<>();
        String pendingHeader = null;
        int depth = 0;
        boolean inBlock = false;

        try (BufferedReader br = Files.newBufferedReader(Paths.get(path))) {
            String lineRaw;
            while ((lineRaw = br.readLine()) != null) {
                String raw = lineRaw.trim();
                if (raw.isEmpty()) continue;

                // Kapanis satiri
                if (raw.equals("}")) {
                    if (depth > 0) depth--;
                    if (inBlock) {
                        current.add("\t".repeat(depth) + "}");
                        if (depth == 0) {
                            CdrBlock block = new CdrBlock(new ArrayList<>(current), fileName);
                            block.setFlatKeyValues(flattenLines(current));
                            blocks.add(block);
                            current.clear();
                            inBlock = false;
                            pendingHeader = null;
                        }
                    }
                    continue;
                }

                // Sadece '{' satiri
                if (raw.equals("{")) {
                    if (!inBlock) {
                        current.clear();
                        if (pendingHeader != null) {
                            current.add(pendingHeader);
                            pendingHeader = null;
                        }
                        inBlock = true;
                    }
                    current.add("\t".repeat(depth) + "{");
                    depth++;
                    continue;
                }

                // Tek satirda baslik + '{'
                if (raw.endsWith("{")) {
                    if (!inBlock) {
                        current.clear();
                        if (pendingHeader != null) {
                            current.add(pendingHeader);
                            pendingHeader = null;
                        }
                        inBlock = true;
                    }
                    current.add("\t".repeat(depth) + raw);
                    depth++;
                    continue;
                }

                // Normal satir
                if (inBlock) {
                    current.add("\t".repeat(depth) + raw);
                } else {
                    pendingHeader = raw;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return blocks;
    }

    /**
     * Lazy load: belirli blok araligini dondurur.
     * start -> baslangic blok indexi
     * count -> kac blok dondurulecek
     */
    public List<CdrBlock> parseFileChunk(String path, int start, int count) {
        List<CdrBlock> result = new ArrayList<>();
        String fileName = new File(path).getName();

        List<String> current = new ArrayList<>();
        String pendingHeader = null;
        int depth = 0;
        boolean inBlock = false;
        int idx = 0;

        try (BufferedReader br = Files.newBufferedReader(Paths.get(path))) {
            String lineRaw;
            while ((lineRaw = br.readLine()) != null) {
                String raw = lineRaw.trim();
                if (raw.isEmpty()) continue;

                if (raw.equals("}")) {
                    if (depth > 0) depth--;
                    if (inBlock) {
                        current.add("\t".repeat(depth) + "}");
                        if (depth == 0) {
                            if (idx >= start && result.size() < count) {
                                CdrBlock block = new CdrBlock(new ArrayList<>(current), fileName);
                                block.setFlatKeyValues(flattenLines(current));
                                result.add(block);
                            }
                            idx++;
                            current.clear();
                            inBlock = false;
                            pendingHeader = null;

                            if (result.size() >= count) break;
                        }
                    }
                    continue;
                }

                if (raw.equals("{")) {
                    if (!inBlock) {
                        current.clear();
                        if (pendingHeader != null) {
                            current.add(pendingHeader);
                            pendingHeader = null;
                        }
                        inBlock = true;
                    }
                    current.add("\t".repeat(depth) + "{");
                    depth++;
                    continue;
                }

                if (raw.endsWith("{")) {
                    if (!inBlock) {
                        current.clear();
                        inBlock = true;
                    }
                    current.add("\t".repeat(depth) + raw);
                    depth++;
                    continue;
                }

                if (inBlock) {
                    current.add("\t".repeat(depth) + raw);
                } else {
                    pendingHeader = raw;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /** Stream tabanli okuma, service icin kolay kullanim. */
    public Stream<CdrBlock> streamFile(String path) throws IOException {
        return parseFile(path).stream();
    }

    /**
     * Satirlari tek duzeye indirip (flatten)
     * her key-value'yu "parent.child.key" formatinda map'e koyar.
     * Root blok ismi path'e eklenmez.
     */
    private Map<String, String> flattenLines(List<String> lines) {
        Map<String, String> flat = new HashMap<>();
        Deque<String> stack = new ArrayDeque<>();

        String pendingBlockKey = null;
        int depth = 0;

        for (String l : lines) {
            if (l == null) continue;
            String s = l.trim();
            if (s.isEmpty()) continue;

            if (s.equals("}")) {
                if (depth > 0) {
                    depth--;
                    if (!stack.isEmpty()) stack.pop();
                }
                pendingBlockKey = null;
                continue;
            }

            if (s.equals("{")) {
                if (pendingBlockKey != null) {
                    if (depth > 0) stack.push(pendingBlockKey);
                    pendingBlockKey = null;
                }
                depth++;
                continue;
            }

            if (s.endsWith("{")) {
                String key = s.substring(0, s.length() - 1).trim();
                if (!key.isEmpty() && depth > 0) stack.push(key);
                depth++;
                pendingBlockKey = null;
                continue;
            }

            int colon = s.indexOf(':');
            if (colon >= 0) {
                String k = s.substring(0, colon).trim();
                String v = ValueNormalizer.normalize(s.substring(colon + 1));
                String full = buildPath(stack, k);
                flat.put(full, v);
                pendingBlockKey = null;
            } else {
                pendingBlockKey = s;
            }
        }
        return flat;
    }

    // Stack uzerindeki parent path’leri ters cevirip birlestirerek tek string yapar.
    private String buildPath(Deque<String> stack, String key) {
        if (stack.isEmpty()) return key;
        List<String> list = new ArrayList<>(stack);
        Collections.reverse(list);
        return String.join(".", list) + "." + key;
    }
}