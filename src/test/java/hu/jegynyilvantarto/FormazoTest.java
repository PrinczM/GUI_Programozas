package hu.jegynyilvantarto;

import hu.jegynyilvantarto.service.Formazo;
import org.junit.jupiter.api.Test;

import java.util.OptionalDouble;

import static org.junit.jupiter.api.Assertions.*;

class FormazoTest {

    @Test
    void atlagUresOptionalbolGondolatjelet() {
        assertEquals("–", Formazo.atlag(OptionalDouble.empty()));
    }

    @Test
    void atlagToltottOptionalbolErteket() {
        String eredmeny = Formazo.atlag(OptionalDouble.of(4.0));
        assertEquals("4,00", eredmeny);
    }

    @Test
    void atlagCsonkitNemKerekitLefele() {
        // 4.256 -> floor(4.256*100)/100 = floor(425.6)/100 = 425/100 = 4.25
        assertEquals("4,25", Formazo.atlag(4.256));
    }

    @Test
    void atlagCsonkitNemKerekitFelfele() {
        // 4.259 -> floor(4.259*100)/100 = floor(425.9)/100 = 425/100 = 4.25
        assertEquals("4,25", Formazo.atlag(4.259));
    }

    @Test
    void atlagEgeszSzamot() {
        assertEquals("3,00", Formazo.atlag(3.0));
    }

    @Test
    void atlagEgyest() {
        assertEquals("1,00", Formazo.atlag(1.0));
    }

    @Test
    void atlagOtost() {
        assertEquals("5,00", Formazo.atlag(5.0));
    }

    @Test
    void atlagKetTizedesJegyet() {
        assertEquals("3,33", Formazo.atlag(3.333));
    }
}
