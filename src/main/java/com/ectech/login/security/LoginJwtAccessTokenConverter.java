package com.ectech.login.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.MacSigner;
import org.springframework.security.jwt.crypto.sign.SignatureVerifier;
import org.springframework.security.jwt.crypto.sign.Signer;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.common.util.JsonParser;
import org.springframework.security.oauth2.common.util.JsonParserFactory;
import org.springframework.security.oauth2.common.util.RandomValueStringGenerator;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtClaimsSetVerifier;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ectech.login.entities.UserEntity;
import com.ectech.login.repositories.UserRepository;

@Transactional(propagation = Propagation.REQUIRED, readOnly = true, noRollbackFor = Exception.class)
public class LoginJwtAccessTokenConverter extends JwtAccessTokenConverter {

    private JsonParser objectMapper = JsonParserFactory.create();
    private String verifierKey = new RandomValueStringGenerator().generate();
    private Signer signer = new MacSigner(verifierKey);

    @Autowired
    UserRepository userRepository;

    @Autowired
    private JwtClaimsSetVerifier issuerClaimVerifier;

    @Override
    protected String encode(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        String content;
        try {
            content = objectMapper.formatMap(this.convertAccessToken(accessToken, authentication));
        } catch (Exception e) {
            throw new IllegalStateException("Cannot convert access token to JSON", e);
        }
        return JwtHelper.encode(content, signer).getEncoded();
    }

    @Override
    protected Map<String, Object> decode(String token) {
        try {
            Jwt jwt = JwtHelper.decodeAndVerify(token, (SignatureVerifier) signer);
            String claimsStr = jwt.getClaims();
            Map<String, Object> claims = objectMapper.parseMap(claimsStr);
            if (claims.containsKey(EXP) && claims.get(EXP) instanceof Integer) {
                Integer intValue = (Integer) claims.get(EXP);
                claims.put(EXP, new Long(intValue));
            }
            this.issuerClaimVerifier.verify(claims);
            return claims;
        } catch (Exception e) {
            throw new InvalidTokenException("The received token could not be properly validated.");
        }
    }

    @Override
    public Map<String, ?> convertAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication) {
        Map<String, ?> original = super.convertAccessToken(token, authentication);
        Map<String, Object> mapWithAdditionalProperties = new HashMap<>();
        UserEntity user = null;
        try {
            user = userRepository.findOneByUserName(authentication.getPrincipal().toString());
        } catch (Exception e) {

        }
        List<String> roles = new ArrayList<>();

        roles.add("USER");
        mapWithAdditionalProperties.put(Claims.ROLES, roles);

        mapWithAdditionalProperties.putAll(original);
        return mapWithAdditionalProperties;
    }
}
