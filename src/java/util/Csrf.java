package util;

import java.security.SecureRandom;
import java.util.Base64;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Token anti-CSRF para formularios que modifican datos importantes.
 * Se genera uno por sesión; el formulario lo envía en un campo oculto "csrf".
 */
public final class Csrf {

    private static final String ATRIBUTO = "csrfToken";
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private Csrf() {
    }

    /** Devuelve el token de la sesión (lo crea si no existe). */
    public static String token(HttpSession sesion) {
        String t = (String) sesion.getAttribute(ATRIBUTO);
        if (t == null) {
            byte[] b = new byte[24];
            ALEATORIO.nextBytes(b);
            t = Base64.getUrlEncoder().withoutPadding().encodeToString(b);
            sesion.setAttribute(ATRIBUTO, t);
        }
        return t;
    }

    /** true si el token enviado coincide con el de la sesión. */
    public static boolean valido(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        if (sesion == null) {
            return false;
        }
        String esperado = (String) sesion.getAttribute(ATRIBUTO);
        String recibido = request.getParameter("csrf");
        return esperado != null && recibido != null
                && java.security.MessageDigest.isEqual(esperado.getBytes(), recibido.getBytes());
    }
}
