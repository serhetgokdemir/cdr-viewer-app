package com.serhet.cdrviewer.controller;

import com.serhet.cdrviewer.model.CdrBlock;
import com.serhet.cdrviewer.service.CdrService;
import com.serhet.cdrviewer.util.CdrParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * REST controller.
 * Frontend'den gelen istekleri karsilar ve uygun service / util siniflarini cagirir.
 * Ana endpoint: /api
 */
@RestController
@RequestMapping("/api")
public class CdrController {

    private final CdrService cdrService;

    @Autowired
    private CdrParser parser;

    public CdrController(CdrService cdrService) {
        this.cdrService = cdrService;
    }

    /**
     * Key-value arama endpoint'i.
     * GET /api/search
     * Query params: folder, name, key?, value?
     *
     * @return arama kriterlerine uyan CdrBlock listesi
     */
    @GetMapping("/search")
    public List<CdrBlock> search(
            @RequestParam String folder,
            @RequestParam String name,
            @RequestParam(required = false) String key,
            @RequestParam(required = false) String value) {

        String fullPath = Paths.get(folder, name).toString();
        return cdrService.filterCdrBlocks(fullPath, key, value);
    }

    /**
     * Verilen path altindaki dosyalari listeler.
     * POST /api/addPath
     * Body param: path
     *
     * @return klasordeki dosya adlari listesi veya hata mesaji
     */
    @PostMapping("/addPath")
    public ResponseEntity<?> addFilePath(@RequestParam String path) {
        try {
            Path filePath = Paths.get(path);
            if (!Files.exists(filePath) || !Files.isDirectory(filePath)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Gecersiz klasor yolu: " + path);
            }

            try (Stream<Path> stream = Files.list(filePath)) {
                List<String> fileNames = stream
                        .filter(Files::isRegularFile)
                        .map(p -> p.getFileName().toString())
                        .collect(Collectors.toList());
                return ResponseEntity.ok(fileNames);
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Hata: " + e.getMessage());
        }
    }

    /**
     * Bir dosyanin tum icerigini dondurur.
     * GET /api/file
     */
    @GetMapping("/file")
    public List<CdrBlock> getFileContent(@RequestParam String folder,
                                         @RequestParam String name) {
        String fullPath = Paths.get(folder, name).toString();
        return parser.parseFile(fullPath);
    }

    /**
     * Lazy loading icin belirli sayida blok dondurur.
     * GET /api/file/chunk
     * Query params: folder, name, start, lines
     */
    @GetMapping("/file/chunk")
    public List<CdrBlock> getFileChunk(
            @RequestParam String folder,
            @RequestParam String name,
            @RequestParam int start,
            @RequestParam int lines) {

        String fullPath = Paths.get(folder, name).toString();
        return parser.parseFileChunk(fullPath, start, lines);
    }
}