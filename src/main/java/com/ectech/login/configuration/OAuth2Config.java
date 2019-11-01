package com.ectech.login.configuration;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtClaimsSetVerifier;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import com.ectech.login.security.LoginAuthenticationProvider;
import com.ectech.login.security.LoginClaimVerifier;
import com.ectech.login.security.LoginJwtAccessTokenConverter;
import com.ectech.login.security.UEPasswordEncoder;

/**
 * Configuration for OAuth2
 */
@Configuration
@EnableAuthorizationServer
public class OAuth2Config extends AuthorizationServerConfigurerAdapter {

    @Value("#{ @environment['identityserver.security.oauth2.token.ttl'] ?: 3600 }")
    private int accessTokenValiditySeconds;

    @Value("#{ @environment['identityserver.security.oauth2.refresh.ttl'] ?: 3600 }")
    private int refreshTokenValiditySeconds;

    @Value("${identityserver.security.oauth2.client.clientid}")
    private String clientId;

    @Value("${identityserver.security.oauth2.client.secret}")
    private String secret;

    @NotNull
    @Value("${identityserver.security.oauth2.privateKey}")
    private String privateKey;

    @NotNull
    @Value("${identityserver.security.oauth2.publicKey}")
    private String publicKey;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new UEPasswordEncoder();
    }

    @Autowired
    private AuthenticationManager authenticationManager;

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer configurer) throws Exception {
        configurer.authenticationManager(authenticationManager).tokenServices(tokenServices()).tokenStore(tokenStore())
                .accessTokenConverter(accessTokenConverter());
    }

    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(accessTokenConverter());
    }

    @Bean
    @Primary
    public DefaultTokenServices tokenServices() {
        DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
        defaultTokenServices.setTokenStore(tokenStore());
        defaultTokenServices.setSupportRefreshToken(true);
        defaultTokenServices.setTokenEnhancer(accessTokenConverter());
        defaultTokenServices.setAccessTokenValiditySeconds(accessTokenValiditySeconds);
        defaultTokenServices.setRefreshTokenValiditySeconds(refreshTokenValiditySeconds);
        return defaultTokenServices;
    }

    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
        JwtAccessTokenConverter converter = new LoginJwtAccessTokenConverter();
        converter.setSigningKey(privateKey);
        converter.setVerifierKey(publicKey);
        return converter;
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory().withClient(clientId).secret(secret)
                .authorizedGrantTypes("password", "credentials", "refresh_token").scopes("read", "write")
                .authorities("ROLE_TRUSTED_CLIENT").accessTokenValiditySeconds(accessTokenValiditySeconds)
                .refreshTokenValiditySeconds(refreshTokenValiditySeconds);
    }

    /**
     * This bean is required in order to have passwords properly compared during
     * oAuth2 authentication
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        LoginAuthenticationProvider authenticationProvider = new LoginAuthenticationProvider();
        return authenticationProvider;
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
        oauthServer.checkTokenAccess("isAuthenticated()");
    }

    @Bean
    public JwtClaimsSetVerifier issuerClaimVerifier() {
        return new LoginClaimVerifier();
    }
}