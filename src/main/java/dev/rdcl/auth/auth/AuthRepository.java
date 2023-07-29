package dev.rdcl.auth.auth;

import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public class AuthRepository implements CredentialRepository {

    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
        System.out.printf("getCredentialIdsForUsername(%s)%n", username);
        return Set.of();
    }

    @Override
    public Optional<ByteArray> getUserHandleForUsername(String username) {
        System.out.printf("getUserHandleForUsername(%s)%n", username);
        return Optional.empty();
    }

    @Override
    public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
        System.out.printf("getUsernameForUserHandle(%s)%n", userHandle);
        return Optional.empty();
    }

    @Override
    public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
        System.out.printf("lookup(%s, %s)%n", credentialId, userHandle);
        return Optional.empty();
    }

    @Override
    public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
        System.out.printf("lookupAll(%s)%n", credentialId);
        return Set.of();
    }
}
