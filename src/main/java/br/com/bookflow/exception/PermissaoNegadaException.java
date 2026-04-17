package br.com.bookflow.exception;

public class PermissaoNegadaException extends RuntimeException {

    public PermissaoNegadaException(String message) {
        super(message);
    }
}