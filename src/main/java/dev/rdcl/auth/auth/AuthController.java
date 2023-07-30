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
        var success = authService.completeRegistration(body.user(), body.credentialJson());

        return new ValidateResponseBody(success);
    }

    public record RegistrationRequestBody(String user, String name) {
    }

    public record ValidateRequestBody(String user, String credentialJson) {
    }

    public record ValidateResponseBody(boolean success) {
    }

}
