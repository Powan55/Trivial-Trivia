package com.example.Authentication;

import com.example.Database.Database;

public interface User
{
    public String getUserInfo();
    public void saveScore(Database database);
    public void importData(String fileName);
    public void exportData(String fileName);
    public void addQuestions(String data[]);
    public int getScore();
    public void setScore(int score);
    public void setAuthenticated(boolean authentication);
    public String getSalt();
    public void setSalt(String salt);
    public int getRight();
    public void setRight(int right);
    public int getWrong();
    public void setWrong(int wrong);

}
