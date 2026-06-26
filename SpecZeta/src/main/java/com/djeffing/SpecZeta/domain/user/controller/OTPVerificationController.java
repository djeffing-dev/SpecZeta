package com.djeffing.SpecZeta.domain.user.controller;

import com.djeffing.SpecZeta.domain.user.dto.OTPrequest;
import com.djeffing.SpecZeta.domain.user.entity.OtpVerificationResult;
import com.djeffing.SpecZeta.domain.user.service.AuthService;
import com.djeffing.SpecZeta.domain.user.service.OTPVerificationService;
import com.djeffing.SpecZeta.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/otp")
@Tag(name = "Vérification OTP",
        description = "Endpoints publics de vérification de l'adresse email par code à usage unique (OTP) "
                + "envoyé après l'inscription, et de renvoi d'un nouveau code.")
public class OTPVerificationController {
    private final OTPVerificationService otpService;

    @Operation(
            summary = "Vérifier un code OTP et valider l'adresse email",
            description = """
                    Vérifie le code OTP à 6 chiffres soumis par l'utilisateur pour l'adresse email fournie.

                    Le code est comparé au hash stocké en base. En cas de succès, le code est marqué
                    comme utilisé et invalidé, et le compte passe à l'état `emailVerified = true`.

                    **Cas d'erreur fréquents :**
                    - `400` : code invalide, expiré, ou compte déjà vérifié.
                    - `404` : aucun utilisateur ne correspond à l'email fourni.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Code valide. L'adresse email est désormais vérifiée.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "Vérification réussie",
                                    value = """
                                            {
                                              "status": "success",
                                              "message": "Adresse email vérifiée avec succès."
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Code invalide, expiré, ou compte déjà vérifié.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "Code invalide",
                                    value = """
                                            {
                                              "status": "error",
                                              "message": "Code invalide."
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Aucun utilisateur ne correspond à l'adresse email fournie.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody OTPrequest otPrequest){
        OtpVerificationResult result= otpService.verifyOtp(otPrequest);
        Map<String, Object> response = new HashMap<>();

        if(!result.isSuccess()){
            response.put("status", "error");
            response.put("message", result.getErrorMessage());
            return ResponseEntity.badRequest().body(response);
        }

        response.put("status", "success");
        response.put("detail", "Adresse email vérifiée avec succès.");
        response.put("accessToken", result.getAuthResponse().getAccessToken());
        response.put("refreshToken", result.getAuthResponse().getRefreshToken());
        response.put("user", result.getAuthResponse().getUser());
        response.put("data", result.getAuthResponse());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Renvoyer un nouveau code OTP",
            description = """
                    Génère et envoie par email un nouveau code OTP à l'utilisateur identifié par l'adresse
                    email fournie. Tout code actif précédent est invalidé.

                    **Cas d'erreur fréquents :**
                    - `400` : le compte est déjà vérifié (aucun code n'est alors nécessaire).
                    - `404` : aucun utilisateur ne correspond à l'email fourni.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Un nouveau code a été envoyé à l'adresse email.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "Renvoi réussi",
                                    value = """
                                            {
                                              "status": "success",
                                              "message": "Un nouveau code de vérification a été envoyé à votre adresse email."
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Le compte est déjà vérifié.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "Compte déjà vérifié",
                                    value = """
                                            {
                                              "status": "error",
                                              "message": "Ce compte est déjà vérifié."
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Aucun utilisateur ne correspond à l'adresse email fournie.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOTP(
            @Parameter(description = "Adresse email du compte à re-vérifier.",
                    example = "jane.doe@example.com", required = true)
            @RequestBody @NotBlank @Email String email){
        OtpVerificationResult result = otpService.resendOTP(email);
        Map<String, Object> response = new HashMap<>();

        if(!result.isSuccess()){
            response.put("status", "error");
            response.put("message", result.getErrorMessage());
            return ResponseEntity.badRequest().body(response);
        }

        response.put("status", "success");
        response.put("detail", "Adresse email vérifiée avec succès.");
        response.put("accessToken", result.getAuthResponse().getAccessToken());
        response.put("refreshToken", result.getAuthResponse().getRefreshToken());
        response.put("user", result.getAuthResponse().getUser());
        response.put("data", result.getAuthResponse());
        return ResponseEntity.ok(response);
    }

}
