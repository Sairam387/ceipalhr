package com.smarthr.service;

import com.smarthr.entity.Employee;
import com.smarthr.repository.EmailHistoryRepository;
import com.smarthr.repository.EmployeeRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
public class MilestoneScheduler {

    private static final Logger logger =
            LoggerFactory.getLogger(MilestoneScheduler.class);

    private static final ZoneId INDIA_ZONE =
            ZoneId.of("Asia/Kolkata");

    private final EmployeeRepository employeeRepository;

    private final EmailHistoryRepository emailHistoryRepository;

    private final EmailService emailService;

    public MilestoneScheduler(
            EmployeeRepository employeeRepository,
            EmailHistoryRepository emailHistoryRepository,
            EmailService emailService) {

        this.employeeRepository = employeeRepository;

        this.emailHistoryRepository = emailHistoryRepository;

        this.emailService = emailService;
    }

    /*
     * ============================================================
     * CEIPALHR AUTOMATIC MILESTONE SCHEDULER
     * ============================================================
     *
     * Runs every day at 9:00 AM Asia/Kolkata.
     *
     * Birthday and anniversary emails are protected against
     * duplicate successful sends on the same India calendar day.
     *
     * ============================================================
     */

    @Scheduled(
            cron = "0 0 9 * * *",
            zone = "Asia/Kolkata"
    )
    public void processDailyMilestones() {

        LocalDate today =
                LocalDate.now(INDIA_ZONE);

        LocalDateTime now =
                LocalDateTime.now(INDIA_ZONE);

        logger.info(
                "CeipalHR milestone automation started. date={}, time={}",
                today,
                now
        );

        try {

            processBirthdays(today);

        } catch (Exception e) {

            logger.error(
                    "Unexpected error during birthday automation. date={}",
                    today,
                    e
            );
        }

        try {

            processAnniversaries(today);

        } catch (Exception e) {

            logger.error(
                    "Unexpected error during anniversary automation. date={}",
                    today,
                    e
            );
        }

        logger.info(
                "CeipalHR milestone automation completed. date={}",
                today
        );
    }

    /*
     * ============================================================
     * BIRTHDAY AUTOMATION
     * ============================================================
     */

    private void processBirthdays(
            LocalDate today) {

        logger.info(
                "Starting birthday milestone check. date={}",
                today
        );

        List<Employee> employees =
                employeeRepository.findAll();

        LocalDateTime startOfDay =
                today.atStartOfDay();

        LocalDateTime endOfDay =
                today.plusDays(1)
                        .atStartOfDay()
                        .minusNanos(1);

        int found = 0;

        int sent = 0;

        int skipped = 0;

        logger.debug(
                "Loaded {} employees for birthday check.",
                employees.size()
        );

        for (Employee employee :
                employees) {

            if (employee.getDob() == null) {

                logger.debug(
                        "Skipping employee id={} because DOB is null.",
                        employee.getId()
                );

                continue;
            }

            boolean birthdayToday =
                    employee.getDob().getMonthValue()
                            == today.getMonthValue()
                    &&
                    employee.getDob().getDayOfMonth()
                            == today.getDayOfMonth();

            if (!birthdayToday) {

                continue;
            }

            found++;

            logger.info(
                    "Birthday milestone found. employeeId={}, employeeName={}",
                    employee.getId(),
                    employee.getName()
            );

            /*
             * ====================================================
             * DUPLICATE PROTECTION
             * ====================================================
             *
             * Only a successfully SENT email blocks another
             * birthday email on the same India calendar day.
             *
             * FAILED emails do not block another attempt.
             *
             * ====================================================
             */

            boolean alreadySent =
                    emailHistoryRepository
                            .existsByEmployeeIdAndEmailTypeAndStatusAndSentAtBetween(
                                    employee.getId(),
                                    "BIRTHDAY",
                                    "SENT",
                                    startOfDay,
                                    endOfDay
                            );

            if (alreadySent) {

                skipped++;

                logger.warn(
                        "Birthday email skipped because it was already "
                                + "successfully sent today. "
                                + "employeeId={}, employeeName={}",
                        employee.getId(),
                        employee.getName()
                );

                continue;
            }

            logger.info(
                    "Sending birthday email. "
                            + "employeeId={}, employeeName={}, recipient={}",
                    employee.getId(),
                    employee.getName(),
                    employee.getEmail()
            );

            try {

                emailService.sendBirthdayEmail(
                        employee
                );

                sent++;

                logger.info(
                        "Birthday email processing completed. "
                                + "employeeId={}, employeeName={}",
                        employee.getId(),
                        employee.getName()
                );

            } catch (Exception e) {

                logger.error(
                        "Birthday email failed. "
                                + "employeeId={}, employeeName={}, recipient={}",
                        employee.getId(),
                        employee.getName(),
                        employee.getEmail(),
                        e
                );
            }
        }

        logger.info(
                "Birthday milestone check completed. "
                        + "employeesChecked={}, birthdaysFound={}, "
                        + "emailsProcessed={}, alreadySent={}",
                employees.size(),
                found,
                sent,
                skipped
        );
    }

    /*
     * ============================================================
     * WORK ANNIVERSARY AUTOMATION
     * ============================================================
     */

    private void processAnniversaries(
            LocalDate today) {

        logger.info(
                "Starting work anniversary milestone check. date={}",
                today
        );

        List<Employee> employees =
                employeeRepository.findAll();

        LocalDateTime startOfDay =
                today.atStartOfDay();

        LocalDateTime endOfDay =
                today.plusDays(1)
                        .atStartOfDay()
                        .minusNanos(1);

        int found = 0;

        int sent = 0;

        int skipped = 0;

        logger.debug(
                "Loaded {} employees for anniversary check.",
                employees.size()
        );

        for (Employee employee :
                employees) {

            if (employee.getJoiningDate() == null) {

                logger.debug(
                        "Skipping employee id={} because joining date is null.",
                        employee.getId()
                );

                continue;
            }

            boolean anniversaryToday =
                    employee.getJoiningDate().getMonthValue()
                            == today.getMonthValue()
                    &&
                    employee.getJoiningDate().getDayOfMonth()
                            == today.getDayOfMonth();

            if (!anniversaryToday) {

                continue;
            }

            int years =
                    employee.getJoiningDate()
                            .until(today)
                            .getYears();

            /*
             * Do not send an anniversary email on the employee's
             * original joining date.
             */

            if (years <= 0) {

                logger.warn(
                        "Anniversary email skipped because employee has not "
                                + "completed one year. "
                                + "employeeId={}, employeeName={}, joiningDate={}",
                        employee.getId(),
                        employee.getName(),
                        employee.getJoiningDate()
                );

                continue;
            }

            found++;

            logger.info(
                    "Work anniversary milestone found. "
                            + "employeeId={}, employeeName={}, completedYears={}",
                    employee.getId(),
                    employee.getName(),
                    years
            );

            /*
             * ====================================================
             * DUPLICATE PROTECTION
             * ====================================================
             *
             * Only a successfully SENT email blocks another
             * anniversary email on the same India calendar day.
             *
             * FAILED emails do not block another attempt.
             *
             * ====================================================
             */

            boolean alreadySent =
                    emailHistoryRepository
                            .existsByEmployeeIdAndEmailTypeAndStatusAndSentAtBetween(
                                    employee.getId(),
                                    "ANNIVERSARY",
                                    "SENT",
                                    startOfDay,
                                    endOfDay
                            );

            if (alreadySent) {

                skipped++;

                logger.warn(
                        "Anniversary email skipped because it was already "
                                + "successfully sent today. "
                                + "employeeId={}, employeeName={}",
                        employee.getId(),
                        employee.getName()
                );

                continue;
            }

            logger.info(
                    "Sending anniversary email. "
                            + "employeeId={}, employeeName={}, recipient={}, years={}",
                    employee.getId(),
                    employee.getName(),
                    employee.getEmail(),
                    years
            );

            try {

                emailService.sendAnniversaryEmail(
                        employee
                );

                sent++;

                logger.info(
                        "Anniversary email processing completed. "
                                + "employeeId={}, employeeName={}",
                        employee.getId(),
                        employee.getName()
                );

            } catch (Exception e) {

                logger.error(
                        "Anniversary email failed. "
                                + "employeeId={}, employeeName={}, recipient={}",
                        employee.getId(),
                        employee.getName(),
                        employee.getEmail(),
                        e
                );
            }
        }

        logger.info(
                "Work anniversary milestone check completed. "
                        + "employeesChecked={}, anniversariesFound={}, "
                        + "emailsProcessed={}, alreadySent={}",
                employees.size(),
                found,
                sent,
                skipped
        );
    }
}