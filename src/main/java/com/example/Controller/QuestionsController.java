package com.example.Controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import com.example.Authentication.User;
import com.example.Database.DataDirectory;
import com.example.Database.DataFiles;
import com.example.Database.Database;
import com.example.Database.FileDatabase;
import com.example.Game.Questions;
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
 */
@Controller
public class QuestionsController {

    /** The question, four options and the answer. */
    private static final int COLUMNS = 6;

    private final Database database;
    private final DataDirectory dataDirectory;
    private final Questions questions;
    private final User user;

    public QuestionsController(Database database, DataDirectory dataDirectory,
                               Questions questions, User user) {
        this.database = database;
        this.dataDirectory = dataDirectory;
        this.questions = questions;
        this.user = user;
    }

    @GetMapping("/questions")
    public String showPage(Model model) {
        model.addAttribute("player", user.getUserInfo());
        model.addAttribute("authenticated", user.isAuthenticated());
        model.addAttribute("count", questions.getQuestion().size());
        model.addAttribute("formats", FileDatabase.FORMATS);
        return "questions";
    }

    @GetMapping("/questions/export")
    public ResponseEntity<ByteArrayResource> export(@RequestParam(defaultValue = "csv") String format)
            throws IOException {
        if (!FileDatabase.FORMATS.contains(format)) {
            return ResponseEntity.badRequest().build();
        }
        // The adapters write files, so the export is rendered to a scratch file and streamed back.
        // The name is unique per request: two people exporting at once would otherwise collide.
        String scratch = "export-" + UUID.randomUUID() + "." + format;
        Path path = dataDirectory.resolve(scratch);
        try {
            database.writeFile(scratch, database.readFile(DataFiles.QUESTIONS));
            ByteArrayResource body = new ByteArrayResource(Files.readAllBytes(path));
            return ResponseEntity.ok()
                    .contentType(mediaTypeFor(format))
                    .header("Content-Disposition", ContentDisposition.attachment()
                            .filename("questions." + format).build().toString())
                    .contentLength(body.contentLength())
                    .body(body);
        } finally {
            Files.deleteIfExists(path);
        }
    }

    @PostMapping("/questions/import")
    public String importQuestions(@RequestParam("file") MultipartFile file, Model model) throws IOException {
        if (!user.isAuthenticated()) {
            model.addAttribute("error", "Sign in to replace the question set.");
            return showPage(model);
        }
        Optional<String> problem = replaceQuestions(file);
        model.addAttribute(problem.isPresent() ? "error" : "message",
                problem.orElse("Question set replaced."));
        return showPage(model);
    }

    private Optional<String> replaceQuestions(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return Optional.of("Choose a file first.");
        }
        String format = FileDatabase.extensionOf(file.getOriginalFilename());
        if (!FileDatabase.FORMATS.contains(format)) {
            return Optional.of("Use a .csv, .json or .xml file.");
        }

        // Parsed from a scratch copy and only written over the real file once it looks right.
        // The uploaded name is never used as a path.
        String scratch = "import-" + UUID.randomUUID() + "." + format;
        Path path = dataDirectory.resolve(scratch);
        try {
            file.transferTo(path);
            ArrayList<String[]> rows = database.readFile(scratch);
            if (rows.size() < 2) {
                return Optional.of("That file has a header and no questions.");
            }
            for (String[] row : rows.subList(1, rows.size())) {
                if (row.length < COLUMNS) {
                    return Optional.of("Every row needs " + COLUMNS
                            + " columns: the question, four options and the answer.");
                }
            }
            database.writeFile(DataFiles.QUESTIONS, rows);
            return Optional.empty();
        } finally {
            Files.deleteIfExists(path);
        }
    }

    private static MediaType mediaTypeFor(String format) {
        return switch (format) {
            case FileDatabase.JSON -> MediaType.APPLICATION_JSON;
            case FileDatabase.XML -> MediaType.APPLICATION_XML;
            default -> MediaType.valueOf("text/csv");
        };
    }
}
