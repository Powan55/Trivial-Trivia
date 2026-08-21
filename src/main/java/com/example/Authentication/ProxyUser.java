package com.example.Authentication;

import com.example.Database.Database;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

/**
 * Stands in for whoever is playing in this session, and refuses the things a guest may not do.
 *
 * <p>One instance per HTTP session. It used to hold the current user in a {@code static} field,
 * which made everyone signed in at once the same person.</p>
 *
 * @author Laxmi Poudel
 */
@Component
@Primary
@SessionScope
public class ProxyUser implements User {

    private RealUser signedIn;

    public void signIn(RealUser user) {
        this.signedIn = user;
    }

    public void signOut() {
        this.signedIn = null;
    }

    @Override
    public boolean isAuthenticated() {
        return signedIn != null;
    }

    @Override
    public String getUsername() {
        return signedIn == null ? null : signedIn.getUsername();
    }

    @Override
    public String getUserInfo() {
        return signedIn == null ? "Guest" : signedIn.getUserInfo();
    }

    /** A guest's score is not stored -- there is nothing to key it to. */
    @Override
    public void saveScore(Database database, int score) {
        if (signedIn != null) {
            signedIn.saveScore(database, score);
        }
    }
}
