package dev.rdcl.auth.auth;

import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.PublicKeyCredential;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import com.yubico.webauthn.exception.RegistrationFailedException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
                .user(user)
                .build()
        );

        activeRequests.put(user.getName(), request);

        return request;
    }

    @Transactional
    public boolean completeRegistration(String email, String credentialJson) {
        try {
            var user = userService.getUser(email);
            var request = Optional.ofNullable(activeRequests.get(user.getName()))
                .orElseThrow(() -> new RuntimeException("no active request")); // FIXME
            var response = PublicKeyCredential.parseRegistrationResponseJson(credentialJson);

            var result = relyingPartyService.getRelyingParty().finishRegistration(
                FinishRegistrationOptions.builder()
                    .request(request)
                    .response(response)
                    .build()
            );

            System.out.print("result: ");
            System.out.println(result);

            return true;
        } catch (RegistrationFailedException e) {
            System.err.println("Registration failed: " + e.getMessage());
            return false;
        } catch (IOException e) {
            throw new RuntimeException(e); // FIXME
        }
    }

}
