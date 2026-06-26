package com.djeffing.SpecZeta.domain.user.service;

import com.djeffing.SpecZeta.domain.user.dto.*;
import com.djeffing.SpecZeta.domain.user.entity.OTPVerification;
import com.djeffing.SpecZeta.domain.user.entity.OtpVerificationResult;
import com.djeffing.SpecZeta.domain.user.entity.User;
import com.djeffing.SpecZeta.domain.user.repository.OTPVerificationRepository;
import com.djeffing.SpecZeta.domain.user.repository.UserRepository;
import com.djeffing.SpecZeta.security.JwtTokenProvider;
import com.djeffing.SpecZeta.shared.exception.ResourceNotFoundException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class OTPVerificationService {
    private static final int OTP_VALIDITY_MINUTES = 10; // ex.
    private static final SecureRandom RANDOM = new SecureRandom();

    private final OTPVerificationRepository otpRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine; // Thymeleaf
    @Value("${app.mail.from}")
    private String defaultFromEmail;



    /**
     * Génère un code aléatoire de 6 chiffres.
     */
    private String generatePlainCode() {
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * Invalide tous les OTPs actifs existants pour cet utilisateur,
     * crée un nouveau code, le hache et le stocke.
     * Retourne le code en clair (à envoyer par email).
     */
    @Transactional
    public String createOtp(User user) {
        otpRepository.invalidateActiveCodes(user);

        String plainCode = generatePlainCode();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(OTP_VALIDITY_MINUTES);

        OTPVerification otp = new OTPVerification();
        otp.setUser(user);
        otp.setCode(passwordEncoder.encode(plainCode));
        otp.setExpiresAt(expiresAt);

        otpRepository.save(otp);

        return plainCode;
    }

    public OtpVerificationResult resendOTP(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(()-> new ResourceNotFoundException("Utilisateur introuvable."));
        if(user.getEmailVerified())
            return OtpVerificationResult.failure("Ce compte est déjà vérifié.");

        String plainCode = createOtp(user);
        sendOtpEmail(user, plainCode);

        AuthResponse response = authUser(user);

        return OtpVerificationResult.success(response);
    }

    /**
     * Vérifie le code OTP soumis par l'utilisateur.
     * Retourne un résultat de vérification (succès/échec + message).
     * En cas de succès, marque le code comme utilisé et passe isEmailVerified à true.
     */
    @Transactional
    public OtpVerificationResult verifyOtp(OTPrequest otpRequest) {

        User user = userRepository.findByEmail(otpRequest.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable!"));

        if (user.getEmailVerified()) {
            return OtpVerificationResult.failure("Ce compte est déjà vérifié.");
        }

        Optional<OTPVerification> otpOpt = otpRepository
                .findLatestActiveOtp(user);

        if (otpOpt.isEmpty()) {
            return OtpVerificationResult.failure("Code invalide.");
        }

        OTPVerification otp = otpOpt.get();

        if (otp.isExpired()) {
            return OtpVerificationResult.failure("Code expiré, veuillez en demander un nouveau.");
        }

        if (!passwordEncoder.matches(otpRequest.getPlainCode(), otp.getCode())) {
            return OtpVerificationResult.failure("Code invalide.");
        }

        otp.setIsUsed(true);
        otp.setIsActive(false);
        otpRepository.save(otp);

        user.setEmailVerified(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Authentifier l'utilisateur
        AuthResponse authResponse = authUser(user);
        return OtpVerificationResult.success(authResponse);
    }

    /**
     * Envoie le code OTP par email à l'utilisateur.
     */
    public void sendOtpEmail(User user, String plainCode) {
        String formattedCode = plainCode.substring(0, 3) + " " + plainCode.substring(3);

        Context context = new Context();
        context.setVariable("user", user);
        context.setVariable("otpCode", formattedCode);
        context.setVariable("validityMinutes", OTP_VALIDITY_MINUTES);
        context.setVariable("year", Year.now().getValue());

        String htmlContent = templateEngine.process("emails/otp_verification", context);

        String textContent = String.format(
                "Bonjour %s,%n%n" +
                        "Votre code de vérification : %s%n%n" +
                        "Ce code est valable pendant %d minutes.%n" +
                        "Si vous n'avez pas créé de compte, ignorez cet email.",
                user.getPseudo(), formattedCode, OTP_VALIDITY_MINUTES
        );

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setSubject("Votre code de vérification — Abonment");
            helper.setFrom(defaultFromEmail);
            helper.setTo(user.getEmail());
            helper.setText(textContent, htmlContent);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email OTP", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private AuthResponse authUser(User user){
        Token token =
                jwtTokenProvider.generateToken(user.getId(),user.getEmail(),true);

        return AuthResponse.builder()
                .accessToken(token.getAccessToken())
                .refreshToken(token.getRefreshToken())
                .user(UserSummaryResponse.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .pseudo(user.getPseudo())
                        .photoUrl(user.getPhotoUrl())
                        .ville(user.getVille())
                        .build())
                .build();
    }
}
