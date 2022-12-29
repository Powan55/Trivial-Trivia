package Authentication;

/**
 * @author Laxmi Poudel
 */
public class ProxyUser implements User{

    private boolean isAuthenticated;
    private User user;

    public ProxyUser()
    {
        isAuthenticated = false;
        user = new RealUser();
    }
    @Override
    public void play() {
        user.play();
    }

    @Override
    public String getUserInfo() {
        return null;
    }

    @Override
    public void saveScore(int score) {

    }

    @Override
    public void importData(String fileName) {

    }

    @Override
    public void exportData(String fileName) {
    }

    @Override
    public void addQuestions(String data[]) {

    }

    @Override
    public int getScore() {
        return 0;
    }

    @Override
    public void setScore(int score) {

    }

    public void setAuthenticated(boolean authentication)
    {
        isAuthenticated = authentication;
    }

    public boolean getIsAuthenticated() {
        return isAuthenticated;
    }
}
