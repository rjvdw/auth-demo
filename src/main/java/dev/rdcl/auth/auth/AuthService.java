package dev.rdcl.auth.auth;

import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.FinishAssertionOptions;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.StartAssertionOptions;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.PublicKeyCredential;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import com.yubico.webauthn.exception.AssertionFailedException;
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
    private final Map<String, PublicKeyCredentialCreationOptions> activeRegisterRequests = new HashMap<>();
    private final Map<String, AssertionRequest> activeLoginRequests = new HashMap<>();

    @Transactional
    public PublicKeyCredentialCreationOptions startRegistration(String email, String name) {
        if (userService.findUser(email).isPresent()) {
            throw new RuntimeException("User already exists"); // FIXME
        }

        var user = userService.createUser(email, name);

        var request = relyingPartyService.getRelyingParty().startRegistration(
            StartRegistrationOptions.builder()
                .user(Mappers.userEntityToIdentity(user))
                .build()
        );

        activeRegisterRequests.put(user.getEmail(), request);

        return request;
    }

    @Transactional
    public boolean completeRegistration(String email, String credentialJson) {
        try {
            var user = userService.getUser(email);
            var request = Optional.ofNullable(activeRegisterRequests.get(user.getEmail()))
                .orElseThrow(() -> new RuntimeException("no active request")); // FIXME
            var response = PublicKeyCredential.parseRegistrationResponseJson(credentialJson);

            var result = relyingPartyService.getRelyingParty().finishRegistration(
                FinishRegistrationOptions.builder()
                    .request(request)
                    .response(response)
                    .build()
            );

            userService.registerAuthenticator(user, result);
            activeRegisterRequests.remove(user.getEmail());

            return true;
        } catch (RegistrationFailedException e) {
            System.err.println("Registration failed: " + e.getMessage());
            return false;
        } catch (IOException e) {
            throw new RuntimeException(e); // FIXME
        }
    }

    @Transactional
    public AssertionRequest login(String email) {
        var user = userService.getUser(email);

        var request = relyingPartyService.getRelyingParty().startAssertion(
            StartAssertionOptions.builder()
                .username(user.getEmail())
                .build()
        );

        activeLoginRequests.put(user.getEmail(), request);

        return request;
    }

    @Transactional
    public boolean completeLogin(String email, String credentialJson) {
        try {
            var user = userService.getUser(email);
            var request = Optional.ofNullable(activeLoginRequests.get(user.getEmail()))
                .orElseThrow(() -> new RuntimeException("no active request")); // FIXME
            var response = PublicKeyCredential.parseAssertionResponseJson(credentialJson);

            var result = relyingPartyService.getRelyingParty().finishAssertion(
                FinishAssertionOptions.builder()
                    .request(request)
                    .response(response)
                    .build()
            );

            activeLoginRequests.remove(user.getEmail());

            return result.isSuccess();
        } catch (AssertionFailedException e) {
            System.err.println("Login failed: " + e.getMessage());
            return false;
        } catch (IOException e) {
            throw new RuntimeException(e); // FIXME
        }
    }

}
