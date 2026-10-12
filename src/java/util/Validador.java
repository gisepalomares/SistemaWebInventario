package util;

import java.util.regex.Pattern;

/** Validaciones comunes para los formularios (registro de clientes y usuarios). */
public final class Validador {

    private static final Pattern CORREO = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern DNI = Pattern.compile("^\\d{8}$");
    private static final Pattern TELEFONO = Pattern.compile("^\\d{9}$");

    private Validador() {
    }

    public static boolean correoValido(String correo) {
        return correo != null && correo.length() <= 120 && CORREO.matcher(correo).matches();
    }

    public static boolean dniValido(String dni) {
        return dni != null && DNI.matcher(dni).matches();
    }

    public static boolean telefonoValido(String telefono) {
        return telefono != null && TELEFONO.matcher(telefono).matches();
    }

    public static boolean nombreValido(String nombre) {
        return nombre != null && nombre.length() >= 3 && nombre.length() <= 100;
    }

    /** Mínimo 8 caracteres, con al menos una letra y un número. */
    public static boolean claveValida(String clave) {
        if (clave == null || clave.length() < 8 || clave.length() > 100) {
            return false;
        }
        boolean letra = false;
        boolean numero = false;
        for (char c : clave.toCharArray()) {
            if (Character.isLetter(c)) {
                letra = true;
            } else if (Character.isDigit(c)) {
                numero = true;
            }
        }
        return letra && numero;
    }

    public static final String MENSAJE_CLAVE
            = "La contraseña debe tener al menos 8 caracteres, con letras y números.";
}
