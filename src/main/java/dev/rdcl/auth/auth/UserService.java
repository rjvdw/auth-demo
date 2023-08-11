package dev.rdcl.auth.auth;

import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.data.ByteArray;
import dev.rdcl.auth.auth.entities.AuthenticatorEntity;
import dev.rdcl.auth.auth.entities.UserEntity;
import dev.rdcl.auth.auth.errors.UserDoesNotExist;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class UserService {

    private final EntityManager em;

    public Optional<UserEntity> findUser(String email) {
        return em
            .createNamedQuery("User.findByEmail", UserEntity.class)
            .setParameter("email", email)
            .getResultStream()
            .findAny();
    }

    public Optional<UserEntity> findUser(UUID id) {
        return em
            .createNamedQuery("User.findById", UserEntity.class)
            .setParameter("id", id)
            .getResultStream()
            .findAny();
    }

    public UserEntity getUser(String email) {
        return findUser(email)
            .orElseThrow(() -> new UserDoesNotExist(email));
    }

    @Transactional
    public UserEntity createUser(String email, String name) {
        var user = UserEntity.builder()
            .email(email)
            .name(name)
            .build();

        em.persist(user);
        em.flush();

        return user;
    }

    public Stream<AuthenticatorEntity> getAuthenticators(UserEntity user) {
        return em.createNamedQuery("Authenticator.findByUser", AuthenticatorEntity.class)
            .setParameter("user", user)
            .getResultStream();
    }

    public Stream<AuthenticatorEntity> getAuthenticators(ByteArray credentialId) {
        return em.createNamedQuery("Authenticator.findByCredentialId", AuthenticatorEntity.class)
            .setParameter("credentialId", credentialId.getBytes())
            .getResultStream();
    }

    @Transactional
    public void registerAuthenticator(UserEntity user, RegistrationResult result) {
        System.out.print("result: ");
        System.out.println(result);

        var authenticator = AuthenticatorEntity.builder()
            .user(user)
            .keyId(result.getKeyId().getId().getBytes())
            .cose(result.getPublicKeyCose().getBytes())
            .signatureCount(result.getSignatureCount())
            .build();

        em.persist(authenticator);
        em.flush();
    }
}
