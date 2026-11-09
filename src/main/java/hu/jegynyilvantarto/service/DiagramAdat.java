package hu.jegynyilvantarto.service;

import java.util.List;
import java.util.OptionalDouble;

public record DiagramAdat(List<Oszlop> oszlopok, double maxErtek, OptionalDouble referencia,
                           String referenciaCimke, String alsoSzoveg, String uresSzoveg) {}
