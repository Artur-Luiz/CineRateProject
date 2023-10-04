package app.cinerate.internal.security;

import app.cinerate.internal.user.UserDAO;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AuthProvider implements AuthenticationProvider {


    private final BCryptPasswordEncoder passwordEncoder;

    private final UserDAO userDAO;

    public AuthProvider(BCryptPasswordEncoder passwordEncoder, UserDAO userDAO) {
        this.passwordEncoder = passwordEncoder;
        this.userDAO = userDAO;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        String name = authentication.getName();
        String password = authentication.getCredentials().toString();

        var account = userDAO.findByNickname(name).orElse(null);
        if (account == null)
            return null;

        if (passwordEncoder.matches(password, account.getPassword())) {
            return new UsernamePasswordAuthenticationToken(account, password, account.getAuthorities());
        }

        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

}
