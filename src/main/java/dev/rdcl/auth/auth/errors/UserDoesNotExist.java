package dev.rdcl.auth.auth.errors;

public class UserDoesNotExist extends RuntimeException {
    public UserDoesNotExist(String username) {
        super("user '%s' does not exist".formatted(username));
    }
}
