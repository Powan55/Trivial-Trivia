package Authentication;

public interface User
{
    public void play();
    public String getUserInfo();
    public void saveScore(int score);
    public void importData(String fileName);
    public void exportData(String fileName);
    public void addQuestions(String data[]);
    public int getScore();
    public void setScore(int score);
    public void setAuthenticated(boolean authentication);
    public String getSalt();
    public void setSalt(String salt);

}
