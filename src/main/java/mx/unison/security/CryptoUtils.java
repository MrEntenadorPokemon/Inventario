package mx.unison.security;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

/** Utilidades de encriptacion de contrasenas usadas por el login. */
public class CryptoUtils {
    /**
     * Genera el hash MD5 hexadecimal de una cadena.
     *
     * @param input texto plano recibido desde la UI o pruebas.
     * @return hash MD5 en minusculas.
     */
    public static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
