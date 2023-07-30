package dev.rdcl.auth.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

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
        var request = authService.startRegistration(body.user(), body.name());

        try {
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(request.toCredentialsCreateJson());
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex); // FIXME
        }
    }

    public record RegistrationRequestBody(String user, String name) {
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
    public @ResponseBody ValidateRegistrationResponseBody validateRegistration(
        @RequestBody ValidateRegistrationRequestBody body
    ) {
        var success = authService.completeRegistration(body.user(), body.credentialJson());

        return new ValidateRegistrationResponseBody(success);
    }

    public record ValidateRegistrationRequestBody(String user, String credentialJson) {
    }

    public record ValidateRegistrationResponseBody(boolean success) {
    }

    @PostMapping(
        path = "/login",
        consumes = {
            MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            MediaType.MULTIPART_FORM_DATA_VALUE,
        },
        produces = {
            MediaType.APPLICATION_JSON_VALUE,
        }
    )
    public ResponseEntity<String> login(LoginRequestBody body) {
        var request = authService.login(body.user());

        try {
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(request.toCredentialsGetJson());
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex); // FIXME
        }

    }

    public record LoginRequestBody(String user) {
    }

    @PostMapping(
        path = "/login/validate",
        consumes = {
            MediaType.APPLICATION_JSON_VALUE,
        },
        produces = {
            MediaType.APPLICATION_JSON_VALUE,
        }
    )
    public @ResponseBody ValidateLoginResponseBody validateLogin(
        @RequestBody ValidateLoginRequestBody body
    ) {
        var success = authService.completeLogin(body.user(), body.credentialJson());

        return new ValidateLoginResponseBody(success);
    }

    public record ValidateLoginRequestBody(String user, String credentialJson) {
    }

    public record ValidateLoginResponseBody(boolean success) {
    }

}
