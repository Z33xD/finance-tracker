package com.fintrack.finance_tracker.email;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class EmailService {
    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.mail.dispatch-enabled:false}")
    private boolean dispatchEnabled;

    @Value("${github.dispatch.token:}")
    private String githubToken;

    @Value("${github.dispatch.repo:}")
    private String githubRepo;

    @Value("${github.dispatch.event-type:send-verification-email}")
    private String eventType;

    @Autowired
    private JavaMailSender emailSender;

    private final RestClient restClient = RestClient.create();

    public void sendVerificationEmail(String to, String subject, String text) {
        if (dispatchEnabled) {
            sendViaGitHubDispatch(to, subject, text);
        } else {
            sendViaSmtp(to, subject, text);
        }
    }

    private void sendViaSmtp(String to, String subject, String text) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, true);

            emailSender.send(message);
        } catch (Exception e) {
            System.out.println("SMTP send failed: " + e.getMessage());
        }
    }

    private void sendViaGitHubDispatch(String to, String subject, String html) {
        String[] repoParts = githubRepo.split("/", 2);
        if (repoParts.length != 2 || githubToken == null || githubToken.isBlank()) {
            System.out.println("GitHub dispatch skipped: missing repo or token");
            return;
        }

        Map<String, Object> clientPayload = Map.of("to", to, "subject", subject, "html", html);
        Map<String, Object> body = Map.of("event_type", eventType, "client_payload", clientPayload);

        try {
            restClient.post()
                    .uri("https://api.github.com/repos/{owner}/{repo}/dispatches", repoParts[0], repoParts[1])
                    .header("Authorization", "Bearer " + githubToken)
                    .header("Accept", "application/vnd.github+json")
                    .header("X-GitHub-Api-Version", "2022-11-28")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            System.out.println("GitHub dispatch failed: " + e.getMessage());
        }
    }
}
