package com.homecloud.planner.export;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/export")
class ExportController {

    private static final MediaType MARKDOWN = new MediaType("text", "markdown", java.nio.charset.StandardCharsets.UTF_8);

    private final ExportService exportService;
    private final MarkdownExportRenderer markdownExportRenderer;

    ExportController(ExportService exportService, MarkdownExportRenderer markdownExportRenderer) {
        this.exportService = exportService;
        this.markdownExportRenderer = markdownExportRenderer;
    }

    @GetMapping("/json")
    ResponseEntity<ExportBundle> exportJson() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, attachment("homecloud-planner-export.json"))
                .body(exportService.buildExport());
    }

    @GetMapping("/markdown")
    ResponseEntity<String> exportMarkdown() {
        String markdown = markdownExportRenderer.render(exportService.buildExport());
        return ResponseEntity.ok()
                .contentType(MARKDOWN)
                .header(HttpHeaders.CONTENT_DISPOSITION, attachment("homecloud-planner-export.md"))
                .body(markdown);
    }

    private String attachment(String filename) {
        return ContentDisposition.attachment().filename(filename).build().toString();
    }
}
