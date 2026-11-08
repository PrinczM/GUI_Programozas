package hu.jegynyilvantarto.service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class Validator {

    private Validator() {}

    /** V1 */
    public static String nev(String s) {
        if (s == null || s.trim().isEmpty()) throw new ValidaciosHiba("A név nem lehet üres.");
        s = s.trim();
        if (s.length() > 60) throw new ValidaciosHiba("A név legfeljebb 60 karakter lehet.");
        for (char c : s.toCharArray()) {
            if (!Character.isLetter(c) && c != ' ' && c != '-' && c != '.') {
                throw new ValidaciosHiba("A név csak betűt, szóközt, kötőjelet és pontot tartalmazhat.");
            }
        }
        return s;
    }

    /** V2 */
    public static String osztalyNev(String s) {
        if (s == null) throw new ValidaciosHiba("Az osztály neve pl. 10.B formátumú legyen.");
        s = s.trim();
        if (!s.matches("\\d{1,2}\\.[A-Z]")) {
            throw new ValidaciosHiba("Az osztály neve pl. 10.B formátumú legyen.");
        }
        return s;
    }

    /** V3 */
    public static void jegy(int ertek) {
        if (ertek < 1 || ertek > 5) {
            throw new ValidaciosHiba("A jegy 1 és 5 közötti egész szám.");
        }
    }

    /** V4 */
    public static void datum(String s) {
        datum(s, LocalDate.now());
    }

    /** V4 */
    public static void datum(String s, LocalDate ma) {
        if (s == null || s.trim().isEmpty()) throw new ValidaciosHiba("A dátum formátuma ÉÉÉÉ-HH-NN.");
        LocalDate d;
        try {
            d = LocalDate.parse(s.trim());
        } catch (DateTimeParseException e) {
            throw new ValidaciosHiba("A dátum formátuma ÉÉÉÉ-HH-NN.");
        }
        if (d.isAfter(ma)) {
            throw new ValidaciosHiba("A dátum nem lehet jövőbeli.");
        }
    }

    /** V5 */
    public static String tantargyNev(String s) {
        if (s == null || s.trim().isEmpty()) throw new ValidaciosHiba("A tantárgy neve nem lehet üres.");
        s = s.trim();
        if (s.length() > 40) throw new ValidaciosHiba("A tantárgy neve legfeljebb 40 karakter lehet.");
        return s;
    }

    /** V6 */
    public static String megjegyzes(String s) {
        if (s == null) return "";
        s = s.trim();
        if (s.length() > 100) throw new ValidaciosHiba("A megjegyzés legfeljebb 100 karakter lehet.");
        return s;
    }
}
