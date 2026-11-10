package hu.jegynyilvantarto.service;

import java.util.Locale;
import java.util.OptionalDouble;

public class Formazo {

    private Formazo() {}

    /** F14 */
    public static String atlag(OptionalDouble a) {
        return a.isPresent() ? atlag(a.getAsDouble()) : "–";
    }

    /** F14 */
    public static String atlag(double a) {
        double csonkolt = Math.floor(a * 100) / 100.0;
        return String.format(Locale.of("hu"), "%.2f", csonkolt);
    }
}
