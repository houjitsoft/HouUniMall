package com.houjiang.commons.util;

import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JSEncryptTest {

    @Test
    void decryptsRsaPasswordWithoutWritingSensitiveDataToStdout() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();

        Cipher encryptor = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        encryptor.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
        String encrypted = Base64.getEncoder().encodeToString(
                encryptor.doFinal("credential-test".getBytes(StandardCharsets.UTF_8)));
        String privateKey = "-----BEGIN PRIVATE KEY-----\n"
                + Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(keyPair.getPrivate().getEncoded())
                + "\n-----END PRIVATE KEY-----";

        PrintStream originalOut = System.out;
        ByteArrayOutputStream capturedOut = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(capturedOut, true, StandardCharsets.UTF_8));
            assertEquals("credential-test", JSEncrypt.decrypt(encrypted, privateKey));
        } finally {
            System.setOut(originalOut);
        }

        assertTrue(capturedOut.toString(StandardCharsets.UTF_8).isEmpty());
    }
}
