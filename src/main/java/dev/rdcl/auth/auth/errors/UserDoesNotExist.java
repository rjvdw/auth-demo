package dev.rdcl.auth.auth.errors;

public class UserDoesNotExist extends RuntimeException {
    public UserDoesNotExist(String email) {
        super("user '%s' does not exist".formatted(email));
    }
}
