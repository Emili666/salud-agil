package co.edu.uniquindio.saludagil.service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Guarda las contraseñas con PBKDF2 (incluido en el JDK, sin dependencias extra).
 * Formato almacenado: {@code iteraciones:sal:hash} en Base64.
 */
public final class ClaveHasher {

    private static final int ITERACIONES = 60_000;
    private static final SecureRandom AZAR = new SecureRandom();

    private ClaveHasher() {
    }

    public static String hash(String clave) {
        byte[] sal = new byte[16];
        AZAR.nextBytes(sal);
        return ITERACIONES + ":" + b64(sal) + ":" + b64(derivar(clave, sal, ITERACIONES));
    }

    public static boolean coincide(String clave, String guardado) {
        if (clave == null || guardado == null) {
            return false;
        }
        String[] partes = guardado.split(":");
        if (partes.length != 3) {
            return false;
        }
        byte[] sal = Base64.getDecoder().decode(partes[1]);
        byte[] esperado = Base64.getDecoder().decode(partes[2]);
        return MessageDigest.isEqual(esperado, derivar(clave, sal, Integer.parseInt(partes[0])));
    }

    private static byte[] derivar(String clave, byte[] sal, int iteraciones) {
        try {
            PBEKeySpec spec = new PBEKeySpec(clave.toCharArray(), sal, iteraciones, 256);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo calcular el hash de la contraseña", e);
        }
    }

    private static String b64(byte[] datos) {
        return Base64.getEncoder().encodeToString(datos);
    }
}
