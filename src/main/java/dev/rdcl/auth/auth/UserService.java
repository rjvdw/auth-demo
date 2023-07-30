package dev.rdcl.auth.auth;

import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.UserIdentity;
import dev.rdcl.auth.auth.entities.UserEntity;
import dev.rdcl.auth.auth.errors.UserDoesNotExist;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final EntityManager em;

    public Optional<UserIdentity> findUser(String email) {
        return em
            .createNamedQuery("User.findByEmail", UserEntity.class)
            .setParameter("email", email)
            .getResultStream()
            .findAny()
            .map(this::map);
    }

    public UserIdentity getUser(String email) {
        return findUser(email)
            .orElseThrow(() -> new UserDoesNotExist(email));
    }

    @Transactional
    public UserIdentity createUser(String email, String name) {
        var entity = UserEntity.builder()
            .email(email)
            .name(name)
            .build();

        em.persist(entity);
        em.flush();

        return map(entity);
    }

    private UserIdentity map(UserEntity entity) {
        return UserIdentity.builder()
            .name(entity.getEmail())
            .displayName(entity.getName())
            .id(map(entity.getId()))
            .build();
    }

    private UserEntity map(UserIdentity identity) {
        return UserEntity.builder()
            .email(identity.getName())
            .name(identity.getDisplayName())
            .id(map(identity.getId()))
            .build();
    }

    private UUID map(ByteArray ba) {
        byte[] bytes = ba.getBytes();
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        long high = byteBuffer.getLong();
        long low = byteBuffer.getLong();

        return new UUID(high, low);
    }

    private ByteArray map(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());

        return new ByteArray(bb.array());
    }
}
