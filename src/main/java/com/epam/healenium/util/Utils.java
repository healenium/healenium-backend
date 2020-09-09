package com.epam.healenium.util;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.DigestUtils;

import javax.crypto.SecretKey;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Base64;

@Slf4j
@UtilityClass
public class Utils {

    /**
     * Builds ID for element that represent selector meta
     *
     * @param className  the fully qualified name of the class
     * @param methodName the name of the method
     * @param locator    the selector value
     * @return
     */
    public String buildKey(String className, String methodName, String locator) {
        String rawKey = className.concat(methodName) + locator.hashCode();
        return DigestUtils.md5DigestAsHex(rawKey.trim().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Builds ID for healing record
     *
     * @param elementId  unique identifier of selector META
     * @param pageSource searching context object
     * @return
     */
    public String buildHealingKey(String elementId, String pageSource) {
        String rawKey = elementId + pageSource.hashCode();
        return DigestUtils.md5DigestAsHex(rawKey.trim().getBytes(StandardCharsets.UTF_8));
    }

    @SneakyThrows
    public String getKeyFromKeyStore(String alias) {
        KeyStore keyStore = KeyStore.getInstance("JCEKS");
        InputStream keyStoreFile = new FileInputStream("src/main/resources/keystore/prometrics.ks");
        char[] keyStorePassword = "uSi51JkQTJlgi".toCharArray();
        keyStore.load(keyStoreFile, keyStorePassword);
        SecretKey key = (SecretKey) keyStore.getKey(alias, keyStorePassword);
        return new String(Base64.getDecoder().decode(key.getEncoded()));
    }

}