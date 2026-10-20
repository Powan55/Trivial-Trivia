package com.example.api.v1;

import com.example.Authentication.CreateUser;
import com.example.Authentication.CurrentPlayer;
import com.example.Authentication.PlayerPrincipal;
import com.example.api.v1.dto.CurrentUserDto;
import com.example.api.v1.dto.RegistrationRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@code AccountController.register}'s own sign-in step is private and that controller stays
 * untouched this milestone, so the same session-fixation-safe sign-in is repeated here rather
 * than extracted -- same behaviour, no shared dependency between the JSP and REST paths yet.
 */
@RestController
public class RegistrationController {

    private final CreateUser createUser;
    private final CurrentPlayer currentPlayer;
    private final SecurityContextRepository contexts;

    public RegistrationController(CreateUser createUser, CurrentPlayer currentPlayer,
            SecurityContextRepository contexts) {
        this.createUser = createUser;
        this.currentPlayer = currentPlayer;
        this.contexts = contexts;
    }

    @PostMapping("/api/v1/users")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CurrentUserDto> register(@Valid @RequestBody RegistrationRequest request,
            HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        boolean created = createUser.makeUser(
                new String[] {request.name(), request.username(), request.password()});
        if (!created) {
            throw new DuplicateUsernameException();
        }
        signIn(PlayerPrincipal.newlyRegistered(request.username(), request.name()), servletRequest, servletResponse);
        return ResponseEntity.status(HttpStatus.CREATED).body(CurrentUserDto.from(currentPlayer));
    }

    /** The same session fixation and storage the form login path gets, done by hand. */
    private void signIn(PlayerPrincipal player, HttpServletRequest request, HttpServletResponse response) {
        if (request.getSession(false) != null) {
            request.changeSessionId();
        }
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(UsernamePasswordAuthenticationToken
                .authenticated(player, null, player.getAuthorities()));
        SecurityContextHolder.setContext(context);
        contexts.saveContext(context, request, response);
    }
}
