package com.example.Controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import com.example.Authentication.CurrentPlayer;
import com.example.Database.RowFormats;
import com.example.Persistence.QuestionRepository;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/**
 * Exporting the question set and importing a replacement, as CSV, JSON or XML.
 *
 * <p>Export is open to anyone. Import replaces the set for every player, so the filter chain
 * keeps it to {@code ROLE_ADMIN} and this class never checks for itself.</p>
 */
@Controller
public class QuestionsController {

    private static final int COLUMNS = QuestionRepository.COLUMNS;

    private final RowFormats formats;
    private final QuestionRepository questions;
    private final CurrentPlayer user;

    public QuestionsController(RowFormats formats, QuestionRepository questions, CurrentPlayer user) {
        this.formats = formats;
        this.questions = questions;
        this.user = user;
    }

    @GetMapping("/questions")
    public String showPage(Model model) {
        model.addAttribute("admin", user.isAdmin());
        model.addAttribute("count", questions.count());
        model.addAttribute("formats", RowFormats.FORMATS);
        return "questions";
    }

    @GetMapping("/questions/export")
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

    @PostMapping("/questions/import")
    public String importQuestions(@RequestParam("file") MultipartFile file, Model model) throws IOException {
        Optional<String> problem = replaceQuestions(file);
        model.addAttribute(problem.isPresent() ? "error" : "message",
                problem.orElse("Question set replaced."));
        return showPage(model);
    }

    /** Everything here runs on bytes a stranger uploaded, so nothing is stored until it all checks out. */
    private Optional<String> replaceQuestions(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return Optional.of("Choose a file first.");
        }
        String format = RowFormats.extensionOf(file.getOriginalFilename());
        if (!RowFormats.FORMATS.contains(format)) {
            return Optional.of("Use a .csv, .json or .xml file.");
        }

        List<String[]> rows;
        try (InputStream upload = file.getInputStream()) {
            rows = formats.of(format).read(upload);
        }
        if (rows.size() < 2) {
            return Optional.of("That file has a header and no questions.");
        }
        for (String[] row : rows.subList(1, rows.size())) {
            if (row.length < COLUMNS) {
                return Optional.of("Every row needs " + COLUMNS
                        + " columns: the question, four options and the answer.");
            }
            if (!isAnswerable(row)) {
                return Optional.of("Every answer has to be one of that question's four options.");
            }
        }
        questions.replaceAll(rows);
        return Optional.empty();
    }

    private static boolean isAnswerable(String[] row) {
        for (int option = 1; option <= 4; option++) {
            if (row[option].equals(row[5])) {
                return true;
            }
        }
        return false;
    }

    private static MediaType mediaTypeFor(String format) {
        return switch (format) {
            case RowFormats.JSON -> MediaType.APPLICATION_JSON;
            case RowFormats.XML -> MediaType.APPLICATION_XML;
            default -> MediaType.valueOf("text/csv");
        };
    }
}
