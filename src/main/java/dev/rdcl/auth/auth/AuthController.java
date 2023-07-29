package dev.rdcl.auth.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredential;
import com.yubico.webauthn.data.UserIdentity;
import com.yubico.webauthn.exception.RegistrationFailedException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.UUID;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RelyingPartyService relyingPartyService;
    private final UserService userService;

    @PostMapping(
        path = "/register",
        consumes = {
            MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            MediaType.MULTIPART_FORM_DATA_VALUE,
        },
        produces = {
            MediaType.APPLICATION_JSON_VALUE,
        }
    )
    public ResponseEntity<String> startRegistration(RegistrationRequestBody body) {
        if (userService.findUser(body.user()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        var user = UserIdentity.builder()
            .name(body.user())
            .displayName(body.name())
            .id(generateNewUserHandle())
            .build();

        userService.saveUser(user);

        var request = relyingPartyService.getRelyingParty().startRegistration(
            StartRegistrationOptions.builder()
                .user(user)
                .build()
        );

        userService.setActiveRequest(user, request);

        try {
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(request.toCredentialsCreateJson());
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex); // FIXME
        }
    }

    @PostMapping(
        path = "/register/validate",
        consumes = {
            MediaType.APPLICATION_JSON_VALUE,
        },
        produces = {
            MediaType.APPLICATION_JSON_VALUE,
        }
    )
    public @ResponseBody ValidateResponseBody validateRegistration(@RequestBody ValidateRequestBody body) {
        try {
            var user = userService.getUser(body.user());
            var request = userService.getActiveRequest(user)
                .orElseThrow(() -> new RuntimeException("no active request"));
            var response = PublicKeyCredential.parseRegistrationResponseJson(body.credentialJson());

            var result = relyingPartyService.getRelyingParty().finishRegistration(
                FinishRegistrationOptions.builder()
                    .request(request)
                    .response(response)
                    .build()
            );

            System.out.print("result: ");
            System.out.println(result);

            return new ValidateResponseBody(true);
        } catch (RegistrationFailedException e) {
            System.err.println("Registration failed: " + e.getMessage());
            return new ValidateResponseBody(false);
        } catch (IOException e) {
            throw new RuntimeException(e); // FIXME
        }
    }

    private ByteArray generateNewUserHandle() {
        return new ByteArray(UUID.randomUUID().toString().getBytes());
    }

    public record RegistrationRequestBody(String user, String name) {
    }

    public record ValidateRequestBody(String user, String credentialJson) {
    }

    public record ValidateResponseBody(boolean success) {
    }

}
