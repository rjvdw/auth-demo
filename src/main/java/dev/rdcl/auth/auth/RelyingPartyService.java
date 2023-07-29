package dev.rdcl.auth.auth;

import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class RelyingPartyService {

    private final AuthRepository authRepository;

    public RelyingParty getRelyingParty() {
        var identity = RelyingPartyIdentity.builder()
            .id("localhost")  // FIXME
            .name("auth demo")
            .build();

        return RelyingParty.builder()
            .identity(identity)
            .credentialRepository(authRepository)
            .origins(Set.of("http://localhost:8080"))
            .build();
    }

}
