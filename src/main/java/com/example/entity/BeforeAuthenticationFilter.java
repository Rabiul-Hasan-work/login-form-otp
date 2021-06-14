package com.example.entity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;

@Component
public class BeforeAuthenticationFilter
        extends UsernamePasswordAuthenticationFilter {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    public void setAuthenticationManager(AuthenticationManager authManager) {
        super.setAuthenticationManager(authManager);
    }

    @Autowired
    @Override
    public void setAuthenticationFailureHandler(
            AuthenticationFailureHandler failureHandler) {
        super.setAuthenticationFailureHandler(failureHandler);
    }

    @Autowired
    @Override
    public void setAuthenticationSuccessHandler(
            AuthenticationSuccessHandler successHandler) {
        super.setAuthenticationSuccessHandler(successHandler);
    }

    public BeforeAuthenticationFilter() {
        setUsernameParameter("email");
        super.setRequiresAuthenticationRequestMatcher(
                new AntPathRequestMatcher("/login", "POST"));
    }

    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        String email = request.getParameter("email");

        User user = customUserDetailsService.getCustomerByEmail(email);

        if (user != null) {
            if (user.isOTPRequired()) {
                return super.attemptAuthentication(request, response);
            }

            System.out.println("attemptAuthentication - email: " + email);
            float spamScore = getGoogleRecaptchaScore();

            if (spamScore < 0.5) {
                try {
                    CustomUserDetailsService.generateOneTimePassword(user);
                    throw new InsufficientAuthenticationException("OTP");
                } catch (MessagingException | UnsupportedEncodingException ex) {
                    throw new AuthenticationServiceException(
                            "Error while sending OTP email.");
                }

            }
        }

        return super.attemptAuthentication(request, response);
    }

    private float getGoogleRecaptchaScore() {
        return 0.43f;
    }

}