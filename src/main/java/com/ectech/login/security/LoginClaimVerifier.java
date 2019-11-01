package com.ectech.login.security;

import java.util.Map;

import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.token.store.JwtClaimsSetVerifier;

import com.ectech.login.utils.Util;

public class LoginClaimVerifier implements JwtClaimsSetVerifier {

    @Override
    public void verify(Map<String, Object> claims) throws InvalidTokenException {
        String username = (String) claims.get("user_name");
        if (Util.isEmpty(username)) {
            throw new InvalidTokenException("user_name claim is empty");
        }
    }

}
