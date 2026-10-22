package com.example.Persistence;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.example.Database.DataDirectory;
import com.example.Database.DataFiles;
import com.example.Database.RowFormats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class LegacyDataMigratorTest {

    private static final String HASH = "$2a$10$jApN1zYYVxlVAgHbqfZEOuKLm2kDtC09Baej4iQVWi.GLpJw/zE0q";

    @TempDir
    Path tmp;

    private DataDirectory dataDirectory;
    private UserRepository users;
    private RoundRepository rounds;
    private QuestionRepository questions;
    private LegacyDataMigrator migrator;

    @BeforeEach
    void setUp() {
        dataDirectory = new DataDirectory(tmp.toString());
        users = mock(UserRepository.class);
        rounds = mock(RoundRepository.class);
        questions = mock(QuestionRepository.class);
        when(users.insert(anyString(), anyString(), anyString())).thenReturn(true);
        migrator = new LegacyDataMigrator(dataDirectory, new RowFormats(), users, rounds, questions);
    }

    private void given(String fileName, String[]... rows) {
        ByteArrayOutputStream rendered = new ByteArrayOutputStream();
        new RowFormats().forFileName(fileName).write(rendered, List.of(rows));
        try {
            Files.write(dataDirectory.resolve(fileName), rendered.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void movesTheOldAccountsAndScoresIn() {
        given(DataFiles.USERS,
                new String[] {"Ada Lovelace", "ada", HASH},
                new String[] {"Grace Hopper", "grace", HASH});
        given(DataFiles.STATS,
                new String[] {"ada", "40"},
                new String[] {"ada", "70"},
                new String[] {"grace", "10"});

        migrator.run(null);

        verify(users).insert("Ada Lovelace", "ada", HASH);
        verify(users).insert("Grace Hopper", "grace", HASH);
        verify(rounds).record("ada", 40, 0, 0);
        verify(rounds).record("ada", 70, 0, 0);
        verify(rounds).record("grace", 10, 0, 0);
    }

    /** The cleartext is gone, so a hash that does not copy across verbatim is an account lost. */
    @Test
    void thePasswordHashesCopyAcrossUntouched() {
        given(DataFiles.USERS, new String[] {"Ada Lovelace", "ada", HASH});

        migrator.run(null);

        verify(users).insert(anyString(), anyString(), eq(HASH));
    }

    @Test
    void aDatabaseWithUsersInItIsNeverTouched() {
        when(users.count()).thenReturn(3L);
        given(DataFiles.USERS, new String[] {"Ada Lovelace", "ada", HASH});

        migrator.run(null);

        verify(users, never()).insert(anyString(), anyString(), anyString());
        verifyNoInteractions(rounds, questions);
    }

    @Test
    void noLegacyFilesMeansNothingToDo() {
        migrator.run(null);

        verify(users, never()).insert(anyString(), anyString(), anyString());
        verifyNoInteractions(rounds, questions);
    }

    @Test
    void aLocallyImportedQuestionSetComesAcrossToo() {
        given(DataFiles.USERS, new String[] {"Ada Lovelace", "ada", HASH});
        given(DataFiles.QUESTIONS,
                QuestionRepository.LEGACY_HEADER,
                new String[] {"Largest ocean?", "Atlantic", "Indian", "Pacific", "Arctic", "Pacific"});

        migrator.run(null);

        verify(questions).replaceAll(any());
    }

    @Test
    void aQuestionFileWithNothingBelowTheHeaderLeavesTheShippedSetAlone() {
        given(DataFiles.USERS, new String[] {"Ada Lovelace", "ada", HASH});
        given(DataFiles.QUESTIONS, QuestionRepository.LEGACY_HEADER);

        migrator.run(null);

        verifyNoInteractions(questions);
    }

    /** A row the schema would refuse is not worth losing the shipped set over. */
    @Test
    void anUnanswerableLegacyQuestionLeavesTheShippedSetAlone() {
        org.mockito.Mockito.doThrow(new IllegalArgumentException("Invalid answer")).when(questions).replaceAll(any());
        given(DataFiles.USERS, new String[] {"Ada Lovelace", "ada", HASH});
        given(DataFiles.QUESTIONS,
                QuestionRepository.LEGACY_HEADER,
                new String[] {"Largest ocean?", "Atlantic", "Indian", "Pacific", "Arctic", "Caspian"});

        migrator.run(null);

        verify(questions).replaceAll(any());
    }

    @Test
    void aShortQuestionRowLeavesTheShippedSetAlone() {
        org.mockito.Mockito.doThrow(new IllegalArgumentException("Invalid row")).when(questions).replaceAll(any());
        given(DataFiles.USERS, new String[] {"Ada Lovelace", "ada", HASH});
        given(DataFiles.QUESTIONS, QuestionRepository.LEGACY_HEADER, new String[] {"Largest ocean?", "Atlantic"});

        migrator.run(null);

        verify(questions).replaceAll(any());
    }

    @Test
    void handEditedRowsAreSkippedRatherThanFailingTheStartup() {
        given(DataFiles.USERS,
                new String[] {"Ada Lovelace", "ada", HASH},
                new String[] {"Truncated"},
                new String[] {"No username", "", HASH});
        given(DataFiles.STATS,
                new String[] {"ada", "not a number"},
                new String[] {"ada"},
                new String[] {"ada", "40"});

        migrator.run(null);

        verify(users, times(1)).insert(anyString(), anyString(), anyString());
        verify(rounds, times(1)).record(anyString(), anyInt(), anyInt(), anyInt());
    }

    @Test
    void aUserRowTheDatabaseRefusesDoesNotStopTheRest() {
        when(users.insert(anyString(), eq("ada"), anyString()))
                .thenThrow(new IllegalStateException("column too long"));
        given(DataFiles.USERS,
                new String[] {"Ada Lovelace", "ada", HASH},
                new String[] {"Grace Hopper", "grace", HASH});

        migrator.run(null);

        verify(users).insert("Grace Hopper", "grace", HASH);
    }
}
