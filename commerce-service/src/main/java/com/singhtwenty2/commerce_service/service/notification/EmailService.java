/**
 * Copyright 2025 Aryan Singh
 * Developer: Aryan Singh (@singhtwenty2)
 * Portfolio: https://singhtwenty2.pages.dev/
 * This file is part of SSEW E-commerce Backend System
 * Licensed under MIT License
 * For commercial use and inquiries: aryansingh.corp@gmail.com
 * @author Aryan Singh (@singhtwenty2)
 * @project SSEW E-commerce Backend System
 * @since 2025
 */
package com.singhtwenty2.commerce_service.service.notification;

import com.singhtwenty2.commerce_service.data.dto.notification.EmailEvent;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${email.from:srishastaengineeringworks1991@gmail.com}")
    private String fromEmail;

    @Value("${email.admin:mrysg.test@gmail.com}")
    private String adminEmail;

    @Value("${company.name:Sri Shasta Engineering Works}")
    private String companyName;

    @Value("${company.tagline:Engineering Excellence in Every Product}")
    private String companyTagline;

    @Value("${company.address:123 Industrial Area, Mumbai, Maharashtra 400001}")
    private String companyAddress;

    @Value("${company.phone:+91 98765 43210}")
    private String companyPhone;

    @Value("${company.email:contact@srishasta.com}")
    private String companyEmail;

    @Value("${company.website:https://srishastabangalore.in}")
    private String companyWebsite;

    public void sendNewOrderNotification(EmailEvent event) throws MessagingException {
        EmailEvent.EmailMetadata metadata = event.getMetadata();

        Context context = new Context();
        context.setVariable("orderId", metadata.getOrderId());
        context.setVariable("customerName", metadata.getCustomerName());
        context.setVariable("phoneNumber", metadata.getPhoneNumber());
        context.setVariable("fullAddress", metadata.getFullAddress());
        context.setVariable("totalAmount", formatCurrency(metadata.getTotalAmount()));
        context.setVariable("totalItems", metadata.getTotalItems());
        context.setVariable("orderItems", metadata.getOrderItems());
        context.setVariable("orderPlacedAt", metadata.getOrderPlacedAt());
        context.setVariable("companyName", companyName);
        context.setVariable("companyTagline", companyTagline);
        context.setVariable("companyAddress", companyAddress);
        context.setVariable("companyPhone", companyPhone);
        context.setVariable("companyEmail", companyEmail);
        context.setVariable("companyWebsite", companyWebsite);

        String htmlContent = templateEngine.process("new-order-email", context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(event.getRecipientEmail());
        helper.setSubject("New Order Received - Order #" + metadata.getOrderId().substring(0, 8));
        helper.setText(htmlContent, true);

        mailSender.send(message);
        log.info("New order notification email sent successfully for order: {}", metadata.getOrderId());
    }

    private String formatCurrency(BigDecimal amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        return formatter.format(amount);
    }

    public String getAdminEmail() {
        return adminEmail;
    }
}