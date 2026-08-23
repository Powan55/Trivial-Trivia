package com.example.Database;

/**
 * Names of the CSV files a 1.0 install left in the data directory.
 *
 * <p>Nothing writes these any more. They are read once, under the development profile, by the
 * migrator that moves a developer's old accounts into the database.</p>
 */
public final class DataFiles {

    public static final String QUESTIONS = "QuestionData.csv";
    public static final String USERS = "userData.csv";
    public static final String STATS = "StatData.csv";

    private DataFiles() {
    }
}
