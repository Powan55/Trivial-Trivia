package Authentication;

/**
 * @author Laxmi Poudel
 */
public class ProxyUser implements User{

    private static ProxyUser instance;
    private boolean isAuthenticated;
    private static User user;

    private ProxyUser()
    {
        isAuthenticated = false;
    }

    public static User getInstance() {
        if (instance == null) {
            instance = new ProxyUser();
            user = new RealUser();
        }
        return instance;
    }


    @Override
    public void play() {
        user.play();
    }

    @Override
    public String getUserInfo() {
        if(isAuthenticated)
            return user.getUserInfo();
        else
            return "Authentication.User: Guest\nScore: "+ user.getScore();
    }

    @Override
    public void saveScore(int score) {

    }

    @Override
    public void importData(String fileName) {
        if(isAuthenticated)
            user.importData(fileName);
        else
            System.out.println("Please login to use this function!!");
    }

    @Override
    public void exportData(String fileName)
    {
        if(isAuthenticated)
            user.exportData(fileName);
        else
            System.out.println("Please login to use this function!!");
    }

    @Override
    public void addQuestions(String data[]) {
        if(isAuthenticated)
            user.addQuestions(data);
        else
            System.out.println("Please login to use this function!!");
    }

    @Override
    public int getScore() {
        return user.getScore();
    }

    @Override
    public void setScore(int score) {
        user.setScore(score);
    }

    public void setAuthenticated(boolean authentication)
    {
        isAuthenticated = authentication;
    }

    public boolean getIsAuthenticated() {
        return isAuthenticated;
    }
}
