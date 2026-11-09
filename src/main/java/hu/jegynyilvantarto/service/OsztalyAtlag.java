package hu.jegynyilvantarto.service;

import hu.jegynyilvantarto.model.Osztaly;
import java.util.OptionalDouble;

public record OsztalyAtlag(Osztaly osztaly, OptionalDouble atlag, int jegyekSzama) {}
