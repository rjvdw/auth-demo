package dev.rdcl.auth.auth;

import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import com.yubico.webauthn.data.UserIdentity;
import dev.rdcl.auth.auth.errors.UserDoesNotExist;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    private static final Map<String, UserEntry> users = new HashMap<>();

    public Optional<UserIdentity> findUser(String username) {
        return Optional.ofNullable(users.get(username)).map(UserEntry::identity);
    }

    public UserIdentity getUser(String username) {
        return findUser(username).orElseThrow(() -> new UserDoesNotExist(username));
    }

    public void saveUser(UserIdentity user) {
        users.put(
            user.getName(),
            new UserEntry(user, null)
        );
    }

    public void setActiveRequest(UserIdentity user, PublicKeyCredentialCreationOptions request) {
        var entry = getUserEntry(user);
        users.put(
            user.getName(),
            new UserEntry(entry.identity(), request)
        );
    }

    public Optional<PublicKeyCredentialCreationOptions> getActiveRequest(UserIdentity user) {
        var entry = getUserEntry(user);
        return Optional.ofNullable(entry.request());
    }

    public void clearActiveRequest(UserIdentity user) {
        var entry = getUserEntry(user);
        users.put(
            user.getName(),
            new UserEntry(entry.identity(), null)
        );
    }

    private UserEntry getUserEntry(UserIdentity user) {
        return Optional.ofNullable(users.get(user.getName()))
            .orElseThrow(() -> new UserDoesNotExist(user.getName()));
    }
}

record UserEntry(UserIdentity identity, PublicKeyCredentialCreationOptions request) {
}
