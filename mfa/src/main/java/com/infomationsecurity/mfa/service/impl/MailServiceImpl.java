package com.infomationsecurity.mfa.service.impl;

import com.infomationsecurity.mfa.dto.other.RequestInfo;
import com.infomationsecurity.mfa.dto.request.accountDTO.FormVerify;
import com.infomationsecurity.mfa.dto.request.emailOTP.EmailResendOTP;
import com.infomationsecurity.mfa.dto.request.emailOTP.EmailVerificationDTO;
import com.infomationsecurity.mfa.dto.request.emailOTP.EmailVerificationDevice;
import com.infomationsecurity.mfa.dto.response.VerificationResult;
import com.infomationsecurity.mfa.dto.response.accountDTO.AccountDTO;
import com.infomationsecurity.mfa.entity.Account;
import com.infomationsecurity.mfa.entity.MfaSettings;
import com.infomationsecurity.mfa.entity.TrustDevice;
import com.infomationsecurity.mfa.repository.AccountRepository;
import com.infomationsecurity.mfa.service.MailService;
import com.infomationsecurity.mfa.service.MfaSettingsService;
import com.infomationsecurity.mfa.service.TrustDeviceService;
import com.infomationsecurity.mfa.util.OtpService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final OtpService otpService;
    private final JavaMailSender mailSender;
    private final Configuration freemarkerConfig;
    private final AccountRepository accountRepository;
    private final MfaSettingsService mfaSettingsService;
    private final TrustDeviceService trustDeviceService;


    @Value("${spring.mail.username}")
    private String fromEmail;

    // Rate limiting với Bucket4j
    private final Map<String, Bucket> rateLimitBuckets = new HashMap<>();

    private Bucket getRateLimitBucket(String email) {
        return rateLimitBuckets.computeIfAbsent(email, k -> Bucket.builder()
                .addLimit(Bandwidth.simple(5, Duration.ofHours(1))) // 5 lần/giờ
                .build());
    }

    @Override
    public void sendVerificationOTPEmail(EmailResendOTP emailResendOTP) {
        log.info("Preparing to send verification OTP email to: {}", emailResendOTP.getEmail());
        Optional<Account> optionalAccount = getAccountByEmail(emailResendOTP.getEmail());
        Account account = optionalAccount.orElse(null);
        if (account == null) {
            log.warn("No account found for email: {}", emailResendOTP.getEmail());
            throw new RuntimeException("Không tìm thấy tài khoản với email: " + emailResendOTP.getEmail());
        }
        String OTP = otpService.generateOtp(emailResendOTP.getEmail());

        sendVerificationEmail(account, OTP);

    }

    /**
     * @param formVerify
     */
    @Override
    public void sendEmailVerifyDevice(FormVerify formVerify) {
        Account account = accountRepository.findByAccountUsername(formVerify.getUsername())
                .orElseThrow();

        String OTP = otpService.generateOtp(account.getAccountEmail());

        sendVerificationEmail(account, OTP);

    }

    @Override
    public VerificationResult verifyEmail(EmailVerificationDTO emailVerificationDTO) {
        try {
            boolean isValid = otpService.validateOtp(
                    emailVerificationDTO.getEmail(),
                    emailVerificationDTO.getOtp()
            );
            return VerificationResult.builder()
                    .success(true)
                    .message(null) // Hoặc một thông điệp thành công nếu cần
                    .build();
        } catch (RuntimeException e) {
            log.warn("Email verification failed: {}", e.getMessage());
            return VerificationResult.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
        }
    }

    @Override
    public VerificationResult verifiedSignUp(EmailVerificationDTO emailVerificationDTO) {

        VerificationResult result = verifyEmail(emailVerificationDTO);
        if (result.getSuccess()) {
            //MFA setting
            Optional<Account> optionalAccount = getAccountByEmail(emailVerificationDTO.getEmail());
            Account account = optionalAccount.orElse(null);
            createMFASetting(account);
            //TrustDevice
            RequestInfo requestInfo = mfaSettingsService.extractRequestInfo();
            TrustDevice trustDevice = trustDeviceService.createOrGetTrustDevice(account, requestInfo, true);
            sendWelcomeEmail(account, trustDevice.getDeviceName());
            return result;
        }
        return result;
    }

    /**
     * @param emailVerificationDevice
     * @return
     */
    @Override
    public Boolean verifyEmailDevice(EmailVerificationDevice emailVerificationDevice) {

        Optional<Account> account = accountRepository.findByAccountUsername(emailVerificationDevice.getUsername());

        if(account.isPresent()){
            Boolean isVerify = otpService.validateOtp(account.get().getAccountEmail(), emailVerificationDevice.getOtp());
            trustDeviceService.updateDeviceVerify(account.get());
            return isVerify;
        }

        return false;
    }

    public CompletableFuture<Void> sendVerificationEmail(Account account, String OTP) {
        return CompletableFuture.runAsync(() -> {
            try {
                // Kiểm tra rate limit
                Bucket bucket = getRateLimitBucket(account.getAccountEmail());
                if (!bucket.tryConsume(1)) {
                    log.warn("Rate limit exceeded for email: {}", account.getAccountEmail());
                    throw new RuntimeException("Vượt quá giới hạn gửi OTP. Vui lòng thử lại sau!");
                }

                // Sử dụng OTP được cung cấp (từ OtpService)
                // Lưu ý: OtpService đã lưu OTP vào Redis, nên không cần lưu lại ở đây
                log.info("Sending verification email with OTP: {} to email: {}", OTP, account.getAccountEmail());

                // Tạo model cho FreeMarker template
                Map<String, Object> model = new HashMap<>();
                model.put("userName", account.getUser().getUserName());
                model.put("otp", OTP);
                model.put("expiryMinutes", otpService.getOtpExpiryMinutes());

                // Render email template
                Template template = freemarkerConfig.getTemplate("email/verify-otp.ftl");
                String htmlBody = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);

                // Tạo và gửi email
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                helper.setTo(account.getAccountEmail());
                helper.setFrom(fromEmail);
                helper.setSubject("Xác nhận OTP - Hệ thống MFA");
                helper.setText(htmlBody, true);

                mailSender.send(mimeMessage);
                log.info("Verification email sent successfully to: {}", account.getAccountEmail());

            } catch (MessagingException e) {
                log.error("Failed to send email to {}: {}", account.getAccountEmail(), e.getMessage());
                throw new RuntimeException("Lỗi khi gửi email OTP: " + e.getMessage(), e);
            } catch (Exception e) {
                log.error("Error processing email template for {}: {}", account.getAccountEmail(), e.getMessage());
                throw new RuntimeException("Lỗi xử lý template email: " + e.getMessage(), e);
            }
        });
    }

    public void sendWelcomeEmail(Account account, String deviceName) {
        CompletableFuture.runAsync(() -> {
            try {

                log.info("Sending welcome email to: {}, device: {}", account.getAccountEmail(), deviceName);

                // Tạo model cho FreeMarker template
                Map<String, Object> model = new HashMap<>();
                model.put("userName", account.getUser().getUserName());
                model.put("deviceInfo", deviceName);
                model.put("currentDateTime", java.time.LocalDateTime.now().toString());

                // Render email template
                Template template = freemarkerConfig.getTemplate("email/welcome-email.ftl");
                String htmlBody = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);

                // Tạo và gửi email
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                helper.setTo(account.getAccountEmail());
                helper.setFrom(fromEmail);
                helper.setSubject("Chào mừng đến với Hệ thống MFA!");
                helper.setText(htmlBody, true);

                mailSender.send(mimeMessage);
                log.info("Welcome email sent successfully to: {}", account.getAccountEmail());

            } catch (MessagingException e) {
                log.error("Failed to send welcome email to {}: {}", account.getAccountEmail(), e.getMessage());
                throw new RuntimeException("Lỗi khi gửi email chào mừng: " + e.getMessage(), e);
            } catch (Exception e) {
                log.error("Error processing welcome email template for {}: {}", account.getAccountEmail(), e.getMessage());
                throw new RuntimeException("Lỗi xử lý template email chào mừng: " + e.getMessage(), e);
            }
        });
    }


    private void createMFASetting(Account account) {
        MfaSettings mfaSettings = new MfaSettings();
        mfaSettings.setAccount(account);
        mfaSettingsService.create(mfaSettings);
    }

    private Optional<Account> getAccountByEmail(String email) {
        log.info("Get account by email: {}", email);
        return accountRepository.getAccountByEmail(email);
    }


}