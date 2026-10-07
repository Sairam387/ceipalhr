package com.smarthr.service;

import com.smarthr.entity.Employee;
import com.smarthr.repository.EmailHistoryRepository;
import com.smarthr.entity.EmailHistory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;

@Service
public class EmailService {

private static final Logger logger =
        LoggerFactory.getLogger(EmailService.class);

private static final ZoneId INDIA_ZONE =
        ZoneId.of("Asia/Kolkata");

private final JavaMailSender mailSender;

private final EmailHistoryRepository emailHistoryRepository;

private final AnniversaryCardService anniversaryCardService;

private final BirthdayCardService birthdayCardService;

@Value("${spring.mail.username}")
private String senderEmail;

@Value("${smarthr.mail.test-mode:true}")
private boolean testMode;

@Value("${smarthr.mail.test-recipient:}")
private String testRecipient;

public EmailService(
        JavaMailSender mailSender,
        EmailHistoryRepository emailHistoryRepository,
        AnniversaryCardService anniversaryCardService,
        BirthdayCardService birthdayCardService) {

    this.mailSender = mailSender;
    this.emailHistoryRepository = emailHistoryRepository;
    this.anniversaryCardService = anniversaryCardService;
    this.birthdayCardService = birthdayCardService;
}

// =========================================================
// BIRTHDAY EMAIL
// =========================================================

public void sendBirthdayEmail(Employee employee) {

    if (employee == null) {

        throw new IllegalArgumentException(
                "Employee cannot be null."
        );
    }

    String subject =
            "Happy Birthday "
                    + safe(employee.getName())
                    + " !!!";

    logger.info(
            "Preparing birthday email. employeeId={}, employeeName={}",
            employee.getId(),
            employee.getName()
    );

    /*
     * =====================================================
     * GENERATE BIRTHDAY CARD
     * =====================================================
     */

    Path birthdayCard =
            birthdayCardService.generateCard(employee);

    logger.info(
            "Birthday card generated. employeeId={}, file={}",
            employee.getId(),
            birthdayCard
    );

    /*
     * =====================================================
     * SEND BIRTHDAY EMAIL WITH CARD ATTACHMENT
     * =====================================================
     */

    sendEmail(
            employee,
            "BIRTHDAY",
            subject,
            buildBirthdayHtml(employee),
            birthdayCard
    );
}

// =========================================================
// WORK ANNIVERSARY EMAIL
// =========================================================

public void sendAnniversaryEmail(Employee employee) {

    if (employee == null) {

        throw new IllegalArgumentException(
                "Employee cannot be null."
        );
    }

    if (employee.getJoiningDate() == null) {

        throw new IllegalArgumentException(
                "Joining date is missing for employee: "
                        + safe(employee.getName())
        );
    }

    /*
     * Always calculate anniversary using
     * Asia/Kolkata timezone.
     */

    LocalDate today =
            LocalDate.now(INDIA_ZONE);

    int completedYears =
            Period.between(
                    employee.getJoiningDate(),
                    today
            ).getYears();

    if (completedYears <= 0) {

        throw new IllegalArgumentException(
                "Employee has not completed one year yet: "
                        + safe(employee.getName())
        );
    }

    String subject =
            "Happy Work Anniversary "
                    + safe(employee.getName())
                    + " !!!";

    logger.info(
            "Preparing work anniversary email. "
                    + "employeeId={}, employeeName={}, completedYears={}",
            employee.getId(),
            employee.getName(),
            completedYears
    );

    /*
     * =====================================================
     * GENERATE ANNIVERSARY CARD
     * =====================================================
     */

    Path anniversaryCard =
            anniversaryCardService.generateCard(employee);

    logger.info(
            "Anniversary card generated. employeeId={}, file={}",
            employee.getId(),
            anniversaryCard
    );

    /*
     * =====================================================
     * SEND ANNIVERSARY EMAIL WITH CARD ATTACHMENT
     * =====================================================
     */

    sendEmail(
            employee,
            "ANNIVERSARY",
            subject,
            buildAnniversaryHtml(
                    employee,
                    completedYears
            ),
            anniversaryCard
    );
}

// =========================================================
// COMMON EMAIL SENDER
// =========================================================

private void sendEmail(
        Employee employee,
        String emailType,
        String subject,
        String htmlContent,
        Path attachment) {

    String recipient;

    /*
     * =====================================================
     * TEST MODE
     * =====================================================
     */

    if (testMode) {

        recipient = testRecipient;

        logger.info(
                "CeipalHR email test mode is enabled. "
                        + "Employee email will not be used."
        );

    } else {

        recipient = employee.getEmail();
    }

    validateConfiguration(recipient);

    /*
     * =====================================================
     * DUPLICATE PROTECTION
     * =====================================================
     *
     * Prevent the same milestone email from being sent
     * more than once for the same employee on the same
     * India calendar day.
     *
     * Only successful SENT records are considered.
     *
     * Existing FAILED records do not block another attempt.
     */

    LocalDate today =
            LocalDate.now(INDIA_ZONE);

    LocalDateTime startOfDay =
            today.atStartOfDay();

    LocalDateTime endOfDay =
            today.plusDays(1).atStartOfDay();

    boolean alreadySent =
            emailHistoryRepository
                    .existsByEmployeeIdAndEmailTypeAndStatusAndSentAtBetween(
                            employee.getId(),
                            emailType,
                            "SENT",
                            startOfDay,
                            endOfDay
                    );

    if (alreadySent) {

        logger.warn(
                "Duplicate {} email prevented. "
                        + "employeeId={}, employeeName={}, date={}",
                emailType,
                employee.getId(),
                employee.getName(),
                today
        );

        return;
    }

    /*
     * =====================================================
     * CREATE EMAIL HISTORY
     * =====================================================
     */

    EmailHistory history =
            new EmailHistory();

    history.setEmployeeId(
            employee.getId()
    );

    history.setEmployeeName(
            employee.getName()
    );

    history.setEmail(
            recipient
    );

    history.setEmailType(
            emailType
    );

    history.setSubject(
            subject
    );

    LocalDateTime attemptTime =
            LocalDateTime.now(INDIA_ZONE);

    history.setSentAt(
            attemptTime
    );

    /*
     * =====================================================
     * SEND EMAIL
     * =====================================================
     */

    try {

        logger.info(
                "Sending {} email. employeeId={}, "
                        + "employeeName={}, recipient={}",
                emailType,
                employee.getId(),
                employee.getName(),
                recipient
        );

        MimeMessage message =
                mailSender.createMimeMessage();

        /*
         * true = multipart email.
         * Required for attachments.
         */

        MimeMessageHelper helper =
                new MimeMessageHelper(
                        message,
                        true,
                        "UTF-8"
                );

        helper.setFrom(
                senderEmail
        );

        helper.setTo(
                recipient
        );

        helper.setSubject(
                subject
        );

        helper.setText(
                htmlContent,
                true
        );

        /*
         * =================================================
         * ATTACH CARD
         * =================================================
         */

        if (attachment != null) {

            helper.addAttachment(
                    attachment.getFileName().toString(),
                    attachment.toFile()
            );

            logger.info(
                    "Milestone card attached. "
                            + "employeeId={}, file={}",
                    employee.getId(),
                    attachment
            );
        }

        /*
         * =================================================
         * SEND EMAIL
         * =================================================
         */

        mailSender.send(
                message
        );

        /*
         * =================================================
         * SUCCESS
         * =================================================
         */

        history.setStatus(
                "SENT"
        );

        history.setErrorMessage(
                null
        );

        emailHistoryRepository.save(
                history
        );

        logger.info(
                "Email sent successfully. "
                        + "type={}, employeeId={}, employeeName={}, "
                        + "recipient={}, subject={}",
                emailType,
                employee.getId(),
                employee.getName(),
                recipient,
                subject
        );

    } catch (
            MailException |
            MessagingException e) {

        /*
         * =================================================
         * FAILURE
         * =================================================
         */

        history.setStatus(
                "FAILED"
        );

        history.setErrorMessage(
                getErrorMessage(e)
        );

        emailHistoryRepository.save(
                history
        );

        logger.error(
                "Email sending failed. "
                        + "type={}, employeeId={}, employeeName={}, "
                        + "recipient={}, error={}",
                emailType,
                employee.getId(),
                employee.getName(),
                recipient,
                getErrorMessage(e),
                e
        );

        throw new RuntimeException(
                "Email sending failed: "
                        + history.getErrorMessage(),
                e
        );
    }
}

// =========================================================
// BIRTHDAY HTML TEMPLATE
// =========================================================

private String buildBirthdayHtml(
        Employee employee) {

    String name =
            escapeHtml(
                    safe(employee.getName())
            );

    return """
            <!DOCTYPE html>

            <html>

            <head>

                <meta charset="UTF-8">

                <meta name="viewport"
                      content="width=device-width,
                               initial-scale=1.0">

            </head>

            <body style="
                margin:0;
                padding:0;
                background:#f4f6f8;
                font-family:Arial,Helvetica,sans-serif;
                color:#333333;
            ">

                <div style="
                    max-width:700px;
                    margin:30px auto;
                    background:#ffffff;
                    border:1px solid #dddddd;
                ">

                    <!-- HEADER -->

                    <div style="
                        background:#005499;
                        padding:20px 30px;
                        color:#ffffff;
                    ">

                        <h2 style="
                            margin:0;
                            font-size:22px;
                        ">

                            HR DEPARTMENT

                        </h2>

                        <div style="
                            margin-top:5px;
                            font-size:13px;
                        ">

                            CeipalHR Employee Milestone

                        </div>

                    </div>

                    <!-- CONTENT -->

                    <div style="
                        padding:35px;
                    ">

                        <h1 style="
                            color:#005499;
                            font-size:26px;
                            margin-top:0;
                        ">

                            Happy Birthday ${name} !!!

                        </h1>

                        <p style="
                            font-size:16px;
                            line-height:1.7;
                        ">

                            Dear ${name},

                        </p>

                        <p style="
                            font-size:16px;
                            line-height:1.7;
                        ">

                            Wishing you a very
                            <strong>Happy Birthday!</strong>

                        </p>

                        <p style="
                            font-size:16px;
                            line-height:1.7;
                        ">

                            May your special day be filled with
                            happiness, success and wonderful memories.

                        </p>

                        <p style="
                            font-size:16px;
                            line-height:1.7;
                        ">

                            Best wishes for a wonderful year ahead!

                        </p>

                        <br>

                        <p style="
                            margin-bottom:5px;
                            font-weight:bold;
                        ">

                            Thanks &amp; Regards

                        </p>

                        <p style="
                            margin-top:0;
                            font-weight:bold;
                            color:#005499;
                        ">

                            HR DEPARTMENT

                        </p>

                    </div>

                    <!-- FOOTER -->

                    <div style="
                        background:#f7f7f7;
                        border-top:1px solid #dddddd;
                        padding:25px 30px;
                        font-size:12px;
                        color:#666666;
                        line-height:1.6;
                    ">

                        <strong>
                            CeipalHR
                        </strong>

                        <br>

                        Employee Milestone
                        Automation System

                        <br><br>

                        This is an automated HR notification.
                        Please do not reply to this email.

                    </div>

                </div>

            </body>

            </html>
            """
            .replace(
                    "${name}",
                    name
            );
}

// =========================================================
// ANNIVERSARY HTML TEMPLATE
// =========================================================

private String buildAnniversaryHtml(
        Employee employee,
        int completedYears) {

    String name =
            escapeHtml(
                    safe(employee.getName())
            );

    String yearText =
            completedYears == 1
                    ? "1 year"
                    : completedYears + " years";

    return """
            <!DOCTYPE html>

            <html>

            <head>

                <meta charset="UTF-8">

                <meta name="viewport"
                      content="width=device-width,
                               initial-scale=1.0">

            </head>

            <body style="
                margin:0;
                padding:0;
                background:#f4f6f8;
                font-family:Arial,Helvetica,sans-serif;
                color:#333333;
            ">

                <div style="
                    max-width:700px;
                    margin:30px auto;
                    background:#ffffff;
                    border:1px solid #dddddd;
                ">

                    <!-- HEADER -->

                    <div style="
                        background:#005499;
                        padding:20px 30px;
                        color:#ffffff;
                    ">

                        <h2 style="
                            margin:0;
                            font-size:22px;
                        ">

                            HR DEPARTMENT

                        </h2>

                        <div style="
                            margin-top:5px;
                            font-size:13px;
                        ">

                            CeipalHR Employee Milestone

                        </div>

                    </div>

                    <!-- CONTENT -->

                    <div style="
                        padding:35px;
                    ">

                        <h1 style="
                            color:#005499;
                            font-size:26px;
                            margin-top:0;
                        ">

                            Happy Work Anniversary
                            ${name} !!!

                        </h1>

                        <p style="
                            font-size:16px;
                            line-height:1.7;
                        ">

                            Dear ${name},

                        </p>

                        <p style="
                            font-size:16px;
                            line-height:1.7;
                        ">

                            Congratulations on completing
                            <strong>${yearText}</strong>
                            with the organization!

                        </p>

                        <p style="
                            font-size:16px;
                            line-height:1.7;
                        ">

                            Thank you for your valuable contribution,
                            dedication and commitment.

                        </p>

                        <p style="
                            font-size:16px;
                            line-height:1.7;
                        ">

                            Wishing you continued success and many
                            more wonderful milestones ahead.

                        </p>

                        <br>

                        <p style="
                            margin-bottom:5px;
                            font-weight:bold;
                        ">

                            Thanks &amp; Regards

                        </p>

                        <p style="
                            margin-top:0;
                            font-weight:bold;
                            color:#005499;
                        ">

                            HR DEPARTMENT

                        </p>

                    </div>

                    <!-- FOOTER -->

                    <div style="
                        background:#f7f7f7;
                        border-top:1px solid #dddddd;
                        padding:25px 30px;
                        font-size:12px;
                        color:#666666;
                        line-height:1.6;
                    ">

                        <strong>
                            CeipalHR
                        </strong>

                        <br>

                        Employee Milestone
                        Automation System

                        <br><br>

                        This is an automated HR notification.
                        Please do not reply to this email.

                    </div>

                </div>

            </body>

            </html>
            """
            .replace(
                    "${name}",
                    name
            )
            .replace(
                    "${yearText}",
                    yearText
            );
}

// =========================================================
// VALIDATION
// =========================================================

private void validateConfiguration(
        String recipient) {

    if (senderEmail == null ||
            senderEmail.isBlank()) {

        throw new IllegalStateException(
                "spring.mail.username is not configured."
        );
    }

    if (recipient == null ||
            recipient.isBlank()) {

        throw new IllegalStateException(
                "Email recipient is not configured."
        );
    }
}

// =========================================================
// ERROR MESSAGE
// =========================================================

private String getErrorMessage(
        Exception e) {

    if (e.getMessage() != null &&
            !e.getMessage().isBlank()) {

        return e.getMessage();
    }

    return e.getClass()
            .getSimpleName();
}

// =========================================================
// NULL-SAFE TEXT
// =========================================================

private String safe(
        String value) {

    if (value == null ||
            value.isBlank()) {

        return "Employee";
    }

    return value;
}

// =========================================================
// HTML ESCAPE
// =========================================================

private String escapeHtml(
        String value) {

    return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
}
}
