package dev.rdcl.auth.auth;

import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import dev.rdcl.auth.auth.entities.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
public class CredentialService implements CredentialRepository {

    private final UserService userService;

    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
        return Stream.of(username)
            .map(userService::findUser)
            .flatMap(Optional::stream)
            .flatMap(userService::getAuthenticators)
            .map(a -> PublicKeyCredentialDescriptor.builder()
                .id(new ByteArray(a.getKeyId()))
                .build())
            .collect(Collectors.toSet());
    }

    @Override
    public Optional<ByteArray> getUserHandleForUsername(String username) {
        return userService.findUser(username)
            .map(UserEntity::getId)
            .map(Mappers::uuidToByteArray);
    }

    @Override
    public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
        return Optional.of(userHandle)
            .map(Mappers::byteArrayToUuid)
            .flatMap(userService::findUser)
            .map(UserEntity::getEmail);
    }

    @Override
    public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
        return Stream.of(userHandle)
            .map(Mappers::byteArrayToUuid)
            .map(userService::findUser)
            .flatMap(Optional::stream)
            .flatMap(userService::getAuthenticators)
            .filter(a -> Arrays.equals(a.getKeyId(), credentialId.getBytes()))
            .map(Mappers::authenticatorToRegisteredCredential)
            .findAny();
    }

    @Override
    public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
        return userService.getAuthenticators(credentialId)
            .map(Mappers::authenticatorToRegisteredCredential)
            .collect(Collectors.toSet());
    }
}
