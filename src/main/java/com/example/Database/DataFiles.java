package com.example.Database;

/**
 * Names of the data files the application reads and writes.
 *
 * <p>These used to be spelled inline at every call site, and no two call sites agreed:
 * {@code "userData.csv"} in one place, {@code "Trivial-Trivia/src/Data/userData.csv"} in
 * another. Both were wrong, in different ways, which is why login and account creation
 * could never work at the same time.</p>
 */
public final class DataFiles {

    public static final String QUESTIONS = "QuestionData.csv";
    public static final String USERS = "userData.csv";
    public static final String STATS = "StatData.csv";

    private DataFiles() {
    }
}
