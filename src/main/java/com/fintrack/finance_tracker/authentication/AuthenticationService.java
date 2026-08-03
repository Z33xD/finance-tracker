package com.fintrack.finance_tracker.authentication;

import com.fintrack.finance_tracker.email.EmailService;
import com.fintrack.finance_tracker.users.User;
import com.fintrack.finance_tracker.users.UserRepository;
import com.fintrack.finance_tracker.dto.LoginUserDto;
import com.fintrack.finance_tracker.dto.RegisterUserDto;
import com.fintrack.finance_tracker.dto.VerifyUserDto;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    private AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
    }

    public User signup(RegisterUserDto input) {
        Optional<User> existingUser = userRepository.findByEmail(input.getEmail());

        if (existingUser.isPresent()) {
            User user = existingUser.get();

            if (user.isEnabled()) {
                throw new RuntimeException("Email already in use!");
            }

            user.setVerificationCode(generateVerificationCode());
            user.setVerificationExpiration(LocalDateTime.now().plusMinutes(15));

            userRepository.save(user);
            sendVerificationEmail(user);

            return user;
        }

        User user = new User(input.getUsername(), input.getEmail(), passwordEncoder.encode(input.getPassword()));

        user.setVerificationCode(generateVerificationCode());
        user.setVerificationExpiration(LocalDateTime.now().plusMinutes(15));
        user.setEnabled(false);

        System.out.println("DB code: '" + user.getVerificationCode() + "'");

        User savedUser = userRepository.save(user);
        sendVerificationEmail(savedUser);
        return savedUser;
    }

    public User authenticate(LoginUserDto input) {
        User user = userRepository.findByUsername(input.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found!"));

        if (!user.isEnabled()) {
            throw new RuntimeException("Account not verified yet! Please verify your account");
        }

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                input.getUsername(),
                input.getPassword()
        ));

        return user;
    }

    public void verifyUser(VerifyUserDto input) {
        Optional<User> optionalUser = userRepository.findByEmail(input.getEmail());
        if (optionalUser.isEmpty()) {
            throw new RuntimeException("User not found!");
        }

        User user = optionalUser.get();

        String codeFromUser = input.getVerificationCode();
        if (codeFromUser == null) {
            throw new RuntimeException("Verification code cannot be null!");
        }

        String codeFromDB = user.getVerificationCode();
        if (codeFromDB == null) {
            throw new RuntimeException("User does not have a verification code!");
        }

        if (user.getVerificationExpiration().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Verification code has expired!");
        }

        if (codeFromDB.trim().equals(codeFromUser.trim())) {
            user.setEnabled(true);
            user.setVerificationCode(null);
            user.setVerificationExpiration(null);
            userRepository.save(user);
        } else {
            throw new RuntimeException("Invalid verification code!");
        }
    }

    public void resendVerificationCode(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            if (user.isEnabled()) {
                throw new RuntimeException("Account is already verified!");
            }

            if (user.getVerificationCode() == null ||
                    user.getVerificationExpiration() == null ||
                    user.getVerificationExpiration().isBefore(LocalDateTime.now())) {
                user.setVerificationCode(generateVerificationCode());
                user.setVerificationExpiration(LocalDateTime.now().plusMinutes(15));
            }

            sendVerificationEmail(user);
            userRepository.save(user);
        }

        else {
            throw new RuntimeException("User not found!");
        }
    }

    public void sendVerificationEmail(User user) {
        String subject = "Account verification";
        String verificationCode = user.getVerificationCode();

        String htmlMessage = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "  <meta charset=\"UTF-8\">\n" +
                "  <title>Verify your Finance Tracker account</title>\n" +
                "</head>\n" +
                "<body style=\"margin:0; padding:0; font-family: Arial, Helvetica, sans-serif; background-color:#f6f3ec;\">\n" +
                "  <div style=\"width:100%; padding:24px 0; background-color:#f6f3ec;\">\n" +
                "    \n" +
                "    <div style=\"max-width:600px; margin:0 auto; background-color:#fbfaf6; border-radius:12px; overflow:hidden; box-shadow:0 8px 24px rgba(20, 70, 48, 0.08); border:1px solid #ede7d9;\">\n" +
                "      <div style=\"background-color:#1f8455; color:#ffffff; padding:24px; text-align:center;\">\n" +
                "        <h2 style=\"margin:0; font-family: Georgia, 'Times New Roman', serif; letter-spacing:0.5px;\">Finance Tracker</h2>\n" +
                "      </div>\n" +
                "      \n" +
                "      <div style=\"padding:32px; color:#334155;\">\n" +
                "        <p style=\"font-size:16px; margin:0 0 15px;\">\n" +
                "          Hello!\n" +
                "        </p>\n" +
                "\n" +
                "        <p style=\"font-size:16px; line-height:1.5; margin:0 0 20px;\">\n" +
                "          Please use the verification code below to complete your sign-up process:\n" +
                "        </p>\n" +
                "        \n" +
                "        <div style=\"background-color:#f0f9f4; padding:24px; text-align:center; border-radius:8px; border:1px solid #b8e3c9;\">\n" +
                "          <p style=\"margin:0; font-size:13px; letter-spacing:1px; text-transform:uppercase; color:#64748b;\">Your Verification Code</p>\n" +
                "          <p style=\"margin:10px 0 0; font-size:28px; font-weight:bold; letter-spacing:4px; color:#1a6b47;\">\n" +
                               verificationCode +
                "          </p>\n" +
                "        </div>\n" +
                "\n" +
                "        <p style=\"font-size:14px; line-height:1.5; margin:20px 0 0; color:#64748b;\">\n" +
                "          This code will expire in 15 minutes. If you did not request this, please ignore this email.\n" +
                "        </p>\n" +
                "      </div>\n" +
                "\n" +
                "    </div>\n" +
                "\n" +
                "  </div>\n" +
                "</body>\n" +
                "</html>";

        try {
            emailService.sendVerificationEmail(user.getEmail(), subject, htmlMessage);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }
}
