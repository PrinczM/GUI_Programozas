package hu.jegynyilvantarto;

import hu.jegynyilvantarto.service.ValidaciosHiba;
import hu.jegynyilvantarto.service.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ValidatorTest {

    // --- nev ---

    @Test
    void nevElfogadErvenyesNevet() {
        assertEquals("Kiss János", Validator.nev("Kiss János"));
    }

    @Test
    void nevTrimmeli() {
        assertEquals("Kiss János", Validator.nev("  Kiss János  "));
    }

    @Test
    void nevElfogadKotojeleset() {
        assertEquals("Kis-Nagy Anna", Validator.nev("Kis-Nagy Anna"));
    }

    @Test
    void nevElfogadPontot() {
        assertEquals("Dr. Kiss", Validator.nev("Dr. Kiss"));
    }

    @Test
    void nevUresNevreHibatDob() {
        assertThrows(ValidaciosHiba.class, () -> Validator.nev(""));
    }

    @Test
    void nevNullNevreHibatDob() {
        assertThrows(ValidaciosHiba.class, () -> Validator.nev(null));
    }

    @Test
    void nevCsakSzokozNevreHibatDob() {
        assertThrows(ValidaciosHiba.class, () -> Validator.nev("   "));
    }

    @Test
    void nevTulHosszuNevreHibatDob() {
        String hosszu = "A".repeat(61);
        assertThrows(ValidaciosHiba.class, () -> Validator.nev(hosszu));
    }

    @Test
    void nevSzamotTartalmazoNevreHibatDob() {
        assertThrows(ValidaciosHiba.class, () -> Validator.nev("Kiss 123"));
    }

    @Test
    void nevSpecialisKarakterreHibatDob() {
        assertThrows(ValidaciosHiba.class, () -> Validator.nev("Kiss@János"));
    }

    // --- osztalyNev ---

    @Test
    void osztalyNevElfogadErvenyeset() {
        assertEquals("10.A", Validator.osztalyNev("10.A"));
    }

    @Test
    void osztalyNevElfogadEgyJegyut() {
        assertEquals("1.B", Validator.osztalyNev("1.B"));
    }

    @Test
    void osztalyNevTrimmeli() {
        assertEquals("10.A", Validator.osztalyNev(" 10.A "));
    }

    @Test
    void osztalyNevKisbetuvelHibatDob() {
        assertThrows(ValidaciosHiba.class, () -> Validator.osztalyNev("10.a"));
    }

    @Test
    void osztalyNevBetuvelHibatDob() {
        assertThrows(ValidaciosHiba.class, () -> Validator.osztalyNev("abc"));
    }

    @Test
    void osztalyNevHaromJegyuSzammalHibatDob() {
        assertThrows(ValidaciosHiba.class, () -> Validator.osztalyNev("100.A"));
    }

    @Test
    void osztalyNevNullraHibatDob() {
        assertThrows(ValidaciosHiba.class, () -> Validator.osztalyNev(null));
    }

    // --- jegy ---

    @Test
    void jegyElfogadEgyest() {
        assertDoesNotThrow(() -> Validator.jegy(1));
    }

    @Test
    void jegyElfogadOtost() {
        assertDoesNotThrow(() -> Validator.jegy(5));
    }

    @Test
    void jegyElfogadHarmast() {
        assertDoesNotThrow(() -> Validator.jegy(3));
    }

    @Test
    void jegyNullasraHibatDob() {
        assertThrows(ValidaciosHiba.class, () -> Validator.jegy(0));
    }

    @Test
    void jegyHatosraHibatDob() {
        assertThrows(ValidaciosHiba.class, () -> Validator.jegy(6));
    }

    @Test
    void jegyNegativraHibatDob() {
        assertThrows(ValidaciosHiba.class, () -> Validator.jegy(-1));
    }

    // --- datum ---

    @Test
    void datumElfogadErvenyeset() {
        LocalDate ma = LocalDate.of(2024, 12, 31);
        assertDoesNotThrow(() -> Validator.datum("2024-09-15", ma));
    }

    @Test
    void datumElfogadMaiNapot() {
        LocalDate ma = LocalDate.of(2024, 10, 1);
        assertDoesNotThrow(() -> Validator.datum("2024-10-01", ma));
    }

    @Test
    void datumJovobeliDatumraHibatDob() {
        LocalDate ma = LocalDate.of(2024, 10, 1);
        assertThrows(ValidaciosHiba.class, () -> Validator.datum("2024-10-02", ma));
    }

    @Test
    void datumErvenytelenFormatumraHibatDob() {
        LocalDate ma = LocalDate.of(2024, 12, 31);
        assertThrows(ValidaciosHiba.class, () -> Validator.datum("2024/09/15", ma));
    }

    @Test
    void datumUresreHibatDob() {
        assertThrows(ValidaciosHiba.class, () -> Validator.datum("", LocalDate.now()));
    }

    @Test
    void datumNullraHibatDob() {
        assertThrows(ValidaciosHiba.class, () -> Validator.datum(null, LocalDate.now()));
    }

    @Test
    void datumNemDatumSzovegHibatDob() {
        assertThrows(ValidaciosHiba.class, () -> Validator.datum("nem-datum", LocalDate.now()));
    }

    // --- tantargyNev ---

    @Test
    void tantargyNevElfogadErvenyeset() {
        assertEquals("Matematika", Validator.tantargyNev("Matematika"));
    }

    @Test
    void tantargyNevTrimmeli() {
        assertEquals("Fizika", Validator.tantargyNev("  Fizika  "));
    }

    @Test
    void tantargyNevUresreHibatDob() {
        assertThrows(ValidaciosHiba.class, () -> Validator.tantargyNev(""));
    }

    @Test
    void tantargyNevNullraHibatDob() {
        assertThrows(ValidaciosHiba.class, () -> Validator.tantargyNev(null));
    }

    @Test
    void tantargyNevTulHosszuraHibatDob() {
        String hosszu = "A".repeat(41);
        assertThrows(ValidaciosHiba.class, () -> Validator.tantargyNev(hosszu));
    }

    @Test
    void tantargyNevPontosanNegyvenKarakter() {
        String nev = "A".repeat(40);
        assertEquals(nev, Validator.tantargyNev(nev));
    }

    // --- megjegyzes ---

    @Test
    void megjegyzesNullbolUreset() {
        assertEquals("", Validator.megjegyzes(null));
    }

    @Test
    void megjegyzesErvenyeset() {
        assertEquals("Dolgozat", Validator.megjegyzes("Dolgozat"));
    }

    @Test
    void megjegyzesTrimmeli() {
        assertEquals("Felelet", Validator.megjegyzes("  Felelet  "));
    }

    @Test
    void megjegyzesTulHosszuraHibatDob() {
        String hosszu = "A".repeat(101);
        assertThrows(ValidaciosHiba.class, () -> Validator.megjegyzes(hosszu));
    }

    @Test
    void megjegyzesPontosanSzazKarakter() {
        String megj = "A".repeat(100);
        assertEquals(megj, Validator.megjegyzes(megj));
    }
}
