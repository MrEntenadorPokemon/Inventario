package mx.unison;

import mx.unison.security.CryptoUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CryptoUtilsTest {
    @Test
    void md5GeneraHashEsperado() {
        assertEquals("c289ffe12a30c94530b7fc4e532e2f42", CryptoUtils.md5("admin23"));
    }
}
