package dev.rdcl.auth.auth;

import com.yubico.webauthn.RegistrationResult;
import dev.rdcl.auth.auth.entities.AuthenticatorEntity;
import dev.rdcl.auth.auth.entities.UserEntity;
import dev.rdcl.auth.auth.errors.UserDoesNotExist;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
