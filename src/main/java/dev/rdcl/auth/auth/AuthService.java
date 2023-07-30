package dev.rdcl.auth.auth;

import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredential;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import com.yubico.webauthn.data.UserIdentity;
import com.yubico.webauthn.exception.RegistrationFailedException;
import dev.rdcl.auth.auth.entities.UserEntity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final RelyingPartyService relyingPartyService;
    private final UserService userService;
    private final Map<String, PublicKeyCredentialCreationOptions> activeRequests = new HashMap<>();

    @Transactional
    public PublicKeyCredentialCreationOptions startRegistration(String email, String name) {
        if (userService.findUser(email).isPresent()) {
            throw new RuntimeException("User already exists"); // FIXME
        }

        var user = userService.createUser(email, name);

        var request = relyingPartyService.getRelyingParty().startRegistration(
            StartRegistrationOptions.builder()
                .user(map(user))
                .build()
        );

        activeRequests.put(user.getEmail(), request);

        return request;
    }

    @Transactional
    public boolean completeRegistration(String email, String credentialJson) {
        try {
            var user = userService.getUser(email);
            var request = Optional.ofNullable(activeRequests.get(user.getEmail()))
                .orElseThrow(() -> new RuntimeException("no active request")); // FIXME
            var response = PublicKeyCredential.parseRegistrationResponseJson(credentialJson);

            var result = relyingPartyService.getRelyingParty().finishRegistration(
                FinishRegistrationOptions.builder()
                    .request(request)
                    .response(response)
                    .build()
            );

            userService.registerAuthenticator(user, result);

            return true;
        } catch (RegistrationFailedException e) {
            System.err.println("Registration failed: " + e.getMessage());
            return false;
        } catch (IOException e) {
            throw new RuntimeException(e); // FIXME
        }
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
