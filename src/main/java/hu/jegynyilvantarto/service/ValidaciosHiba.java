package hu.jegynyilvantarto.service;

public class ValidaciosHiba extends RuntimeException {
    public ValidaciosHiba(String uzenet) {
        super(uzenet);
    }
}
