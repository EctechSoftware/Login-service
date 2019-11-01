package com.ectech.login.security;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UEPasswordEncoder implements PasswordEncoder {
    private final static Logger logger = LoggerFactory.getLogger(UEPasswordEncoder.class);
    public final static String ENCRYPTION_ALGORITHM = "SHA";
    public final static String ENCODING = CharEncoding.UTF_8;

    @Override
    public String encode(CharSequence rawPassword) {
        MessageDigest md = null;
        String codedString = null;

        try {
            md = MessageDigest.getInstance(ENCRYPTION_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            logger.error(ENCRYPTION_ALGORITHM + " is not present in the system.", e);
        }

        try {
            md.update(StringUtils.getBytesUtf8(rawPassword.toString()));
            byte[] raw = md.digest();
            byte[] hash = Base64.encodeBase64(raw);
            codedString = new String(hash, ENCODING);
        } catch (UnsupportedEncodingException e) {
            logger.error(ENCODING + " encoding is not supported.", e);
        }
        return codedString;
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return false;
    }

}
