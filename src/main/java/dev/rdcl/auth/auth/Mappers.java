package dev.rdcl.auth.auth;

import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.UserIdentity;
import dev.rdcl.auth.auth.entities.AuthenticatorEntity;
import dev.rdcl.auth.auth.entities.UserEntity;
import lombok.experimental.UtilityClass;

import java.nio.ByteBuffer;
import java.util.UUID;

@UtilityClass
public class Mappers {

    public static UserIdentity userEntityToIdentity(UserEntity entity) {
        return UserIdentity.builder()
            .name(entity.getEmail())
            .displayName(entity.getName())
            .id(uuidToByteArray(entity.getId()))
            .build();
    }

    public static UserEntity userIdentityToEntity(UserIdentity identity) {
        return UserEntity.builder()
            .email(identity.getName())
            .name(identity.getDisplayName())
            .id(byteArrayToUuid(identity.getId()))
            .build();
    }

    public static UUID byteArrayToUuid(ByteArray ba) {
        byte[] bytes = ba.getBytes();
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        long high = byteBuffer.getLong();
        long low = byteBuffer.getLong();

        return new UUID(high, low);
    }

    public static ByteArray uuidToByteArray(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());

        return new ByteArray(bb.array());
    }

    public static RegisteredCredential authenticatorToRegisteredCredential(AuthenticatorEntity authenticator) {
        return RegisteredCredential.builder()
            .credentialId(new ByteArray(authenticator.getKeyId()))
            .userHandle(uuidToByteArray(authenticator.getUser().getId()))
            .publicKeyCose(new ByteArray(authenticator.getCose()))
            .build();
    }
}
