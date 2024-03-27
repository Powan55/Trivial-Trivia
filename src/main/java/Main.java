import Game.Menu;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) {
        Logger logger = Logger.getLogger(Main.class.getName());
        logger.log(Level.WARNING, "######################################################################");
        logger.log(Level.WARNING, "Here in the Main Before calliing Menu!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        logger.log(Level.WARNING, "######################################################################");
        // Create an application context
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

        // Retrieve the Menu bean from the context
        Menu menu = context.getBean(Menu.class);

        // Call the menu method
        menu.menu();

        logger.log(Level.WARNING, "=======================================================================");
        logger.log(Level.WARNING, "Here in the Main After Menu!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        logger.log(Level.WARNING, "=======================================================================");

        // Close the context
        context.close();
    }
}
