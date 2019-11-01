package com.ectech.login.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ectech.login.entities.UserEntity;
import com.ectech.login.repositories.UserRepository;

@Service("authenticationProvider")
@Transactional(propagation = Propagation.REQUIRED, noRollbackFor = Exception.class)
public class LoginAuthenticationProvider implements AuthenticationProvider {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    // private static final int MAX_ATTEMPTS = 3;

    @Autowired
    UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (authentication.getPrincipal() == null || authentication.getPrincipal().toString().length() == 0) {
            throw new BadCredentialsException("Principal cannot be null or empty");
        }
        UserEntity user = null;
        String username = authentication.getPrincipal().toString();

        // Search for valid record by username in DB
        user = userRepository.findOneByUserName(username);
        if (user == null) {
            logger.info(String.format("No record was found for username %s ****** in the DB",
                    authentication.getPrincipal().toString().toUpperCase()));
            throw new BadCredentialsException(
                    String.format("Username %s is invalid", authentication.getPrincipal().toString()));
        } else {
            // Username + Password mismatch
            if (!verifyAuthentication(authentication, user))
                throw new BadCredentialsException(
                        "Failed authentication for " + authentication.getPrincipal().toString());
        }
        return new UsernamePasswordAuthenticationToken(authentication.getPrincipal(), null, null);
    }

    public Authentication fallbackAuthenticate(Authentication authentication, Throwable t) {
        logger.error(t.getMessage());
        throw new AuthenticationServiceException(t.getMessage());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.isAssignableFrom(UsernamePasswordAuthenticationToken.class);
    }

    private boolean verifyAuthentication(Authentication authentication, UserEntity user) {
        String encodedPassword = this.passwordEncoder.encode(authentication.getCredentials().toString());
        if (!user.getPassword().equals(encodedPassword)) {
            logger.info(String.format("No record was found for username %s + the password ****** in the DB",
                    authentication.getPrincipal().toString().toUpperCase()));
            return false;
        } else {
            return true;
        }
    }

}
