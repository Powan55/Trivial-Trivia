package com.example.Authentication;

import java.util.logging.Logger;

import com.example.Persistence.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.stereotype.Component;

/**
 * Makes one named account an administrator at boot.
 *
 * <p>There is no sign-up path to an admin account and no admin UI to grant one from, so the
 * operator sets {@code app.admin.username} (or {@code APP_ADMIN_USERNAME}) to an account that has
 * already registered and restarts. Plain SQL against the users table does the same job; this
 * exists because the hosted instance has a dashboard for environment variables and no shell.</p>
 *
 * <p>A name that does not exist stops the context, and that is the whole point of the class
 * being written this way. This runs on every boot, a free instance cold-starts on almost every
 * visit, and a variable naming an unregistered username would otherwise sit there waiting: the
 * first stranger to register that name would be an administrator on the next wake. Registration
 * hands out {@code ROLE_USER} and nothing else, so this is the only way to become one.</p>
 *
 * <p>There is no demotion here. Pointing the variable at a second account makes that one an
 * administrator too and leaves the first exactly as it was; taking the role away is a database
 * statement.</p>
 */
@Component
@ConditionalOnProperty(name = "app.admin.username")
@DependsOnDatabaseInitialization
public class AdminPromotion {

    private static final Logger logger = Logger.getLogger(AdminPromotion.class.getName());

    private final UserRepository users;
    private final String username;

    public AdminPromotion(UserRepository users, @Value("${app.admin.username}") String username) {
        this.users = users;
        this.username = username;
    }

    /**
     * During the refresh rather than after it. An ApplicationRunner runs once the connectors are
     * already accepting requests, which left the app serving for the few milliseconds between
     * "started" and the shutdown. Nothing could escalate in that window -- the thing that failed
     * is the promotion -- but a context that is going to refuse to start should not answer first.
     */
    @PostConstruct
    public void promote() {
        if (!users.promoteToAdmin(username)) {
            throw new IllegalStateException(
                    "app.admin.username names an account that has not registered. Register it "
                    + "first, then set the variable -- leaving it pointed at a free username "
                    + "would make an administrator of whoever claims it next.");
        }
        logger.info("Promoted an account to administrator");
    }
}
