package hu.jegynyilvantarto.model;

public record Jegy(int id, int diakId, int tantargyId, int ertek, String datum, String megjegyzes) implements Azonosithato {}
