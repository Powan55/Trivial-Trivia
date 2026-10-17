package com.example.api.v1;

import java.io.ByteArrayOutputStream;
import java.util.List;

import com.example.Database.RowFormats;
import com.example.Persistence.QuestionRepository;
import com.example.api.v1.dto.QuestionDto;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController("apiV1QuestionsController")
public class QuestionsController {

    private final QuestionRepository questions;
    private final RowFormats formats;

    public QuestionsController(QuestionRepository questions, RowFormats formats) {
        this.questions = questions;
        this.formats = formats;
    }

    @GetMapping("/api/v1/questions")
    public List<QuestionDto> allQuestions() {
        return questions.findAll().stream().map(QuestionDto::from).toList();
    }

    @GetMapping("/api/v1/questions/export")
    public ResponseEntity<ByteArrayResource> export(@RequestParam(defaultValue = "csv") String format) {
        if (!RowFormats.FORMATS.contains(format)) {
            return ResponseEntity.badRequest().build();
        }
        ByteArrayOutputStream rendered = new ByteArrayOutputStream();
        formats.of(format).write(rendered, questions.export());
        ByteArrayResource body = new ByteArrayResource(rendered.toByteArray());
        return ResponseEntity.ok()
                .contentType(mediaTypeFor(format))
                .header("Content-Disposition", ContentDisposition.attachment()
                        .filename("questions." + format).build().toString())
                .contentLength(body.contentLength())
                .body(body);
    }

    private static MediaType mediaTypeFor(String format) {
        return switch (format) {
            case RowFormats.JSON -> MediaType.APPLICATION_JSON;
            case RowFormats.XML -> MediaType.APPLICATION_XML;
            default -> MediaType.valueOf("text/csv");
        };
    }
}
