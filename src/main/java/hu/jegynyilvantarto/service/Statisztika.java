package hu.jegynyilvantarto.service;

import hu.jegynyilvantarto.model.Diak;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

public record Statisztika(int jegyekSzama, OptionalDouble atlag, Optional<DiakAtlag> legjobb,
                           List<Integer> eloszlas, List<Diak> bukasraAllok) {}
