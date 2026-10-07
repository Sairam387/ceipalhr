package com.smarthr.service;

import com.smarthr.entity.Employee;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
public class AnniversaryCardService {

    private static final ZoneId INDIA_ZONE =
            ZoneId.of("Asia/Kolkata");

    public Path generateCard(Employee employee) {

        LocalDate today = LocalDate.now(INDIA_ZONE);

        int completedYears = 0;

        if (employee.getJoiningDate() != null) {
            completedYears =
                    employee.getJoiningDate()
                            .until(today)
                            .getYears();
        }

        String employeeName = safe(employee.getName());
        String designation = safe(employee.getDesignation());
        String department = safe(employee.getDepartment());

        String joiningDate =
                employee.getJoiningDate() != null
                        ? employee.getJoiningDate().toString()
                        : "N/A";

        String yearText =
                completedYears == 1
                        ? "year"
                        : "years";

        String html = """
                <!DOCTYPE html>
                <html>
                <head>

                    <meta charset="UTF-8">

                    <title>Work Anniversary - EMPLOYEE_NAME</title>

                    <style>

                        body {
                            margin: 0;
                            padding: 0;
                            font-family: Arial, Helvetica, sans-serif;
                            background: #f4f6f8;
                        }

                        .card {
                            width: 800px;
                            margin: 40px auto;
                            background: white;
                            border: 3px solid #005499;
                            text-align: center;
                        }

                        .header {
                            background: #005499;
                            color: white;
                            padding: 30px;
                        }

                        .company {
                            font-size: 34px;
                            font-weight: bold;
                        }

                        .subtitle {
                            font-size: 18px;
                            margin-top: 8px;
                        }

                        .content {
                            padding: 45px 40px;
                        }

                        .celebration {
                            font-size: 55px;
                            margin-bottom: 15px;
                        }

                        .title {
                            color: #005499;
                            font-size: 38px;
                            font-weight: bold;
                        }

                        .name {
                            color: #F46933;
                            font-size: 42px;
                            font-weight: bold;
                            margin: 15px 0;
                        }

                        .years {
                            font-size: 30px;
                            font-weight: bold;
                            margin: 20px 0;
                        }

                        .message {
                            font-size: 20px;
                            line-height: 1.6;
                            color: #333333;
                        }

                        .details {
                            margin: 30px auto;
                            width: 80%;
                            border-collapse: collapse;
                            font-size: 18px;
                        }

                        .details td {
                            padding: 12px;
                            border-bottom: 1px solid #dddddd;
                            text-align: left;
                        }

                        .label {
                            font-weight: bold;
                            color: #005499;
                            width: 40%;
                        }

                        .footer {
                            background: #f4f6f8;
                            padding: 20px;
                            color: #555555;
                            font-size: 14px;
                        }

                    </style>

                </head>

                <body>

                    <div class="card">

                        <div class="header">

                            <div class="company">
                                CeipalHR
                            </div>

                            <div class="subtitle">
                                Employee Milestone Recognition
                            </div>

                        </div>

                        <div class="content">

                            <div class="celebration">
                                🎉
                            </div>

                            <div class="title">
                                Happy Work Anniversary
                            </div>

                            <div class="name">
                                EMPLOYEE_NAME
                            </div>

                            <div class="years">
                                Congratulations on completing
                                COMPLETED_YEARS YEAR_TEXT!
                            </div>

                            <div class="message">

                                Thank you for your valuable contribution,
                                dedication and commitment.

                                <br><br>

                                Wishing you continued success and many more
                                wonderful milestones ahead.

                            </div>

                            <table class="details">

                                <tr>
                                    <td class="label">
                                        Employee Name
                                    </td>

                                    <td>
                                        EMPLOYEE_NAME
                                    </td>
                                </tr>

                                <tr>
                                    <td class="label">
                                        Designation
                                    </td>

                                    <td>
                                        DESIGNATION
                                    </td>
                                </tr>

                                <tr>
                                    <td class="label">
                                        Department
                                    </td>

                                    <td>
                                        DEPARTMENT
                                    </td>
                                </tr>

                                <tr>
                                    <td class="label">
                                        Joining Date
                                    </td>

                                    <td>
                                        JOINING_DATE
                                    </td>
                                </tr>

                                <tr>
                                    <td class="label">
                                        Completed Service
                                    </td>

                                    <td>
                                        COMPLETED_YEARS YEAR_TEXT
                                    </td>
                                </tr>

                            </table>

                        </div>

                        <div class="footer">

                            <strong>
                                HR DEPARTMENT
                            </strong>

                            <br><br>

                            CeipalHR Employee Milestone

                            <br>

                            This is an automated HR notification.

                        </div>

                    </div>

                </body>
                </html>
                """;

        /*
         * Replace placeholders.
         */

        html = html.replace(
                "EMPLOYEE_NAME",
                employeeName
        );

        html = html.replace(
                "DESIGNATION",
                designation
        );

        html = html.replace(
                "DEPARTMENT",
                department
        );

        html = html.replace(
                "JOINING_DATE",
                joiningDate
        );

        html = html.replace(
                "COMPLETED_YEARS",
                String.valueOf(completedYears)
        );

        html = html.replace(
                "YEAR_TEXT",
                yearText
        );

        try {

            Path directory =
                    Paths.get("generated-cards");

            Files.createDirectories(directory);

            String fileName =
                    "anniversary-"
                            + employee.getId()
                            + "-"
                            + today
                            + ".html";

            Path file =
                    directory.resolve(fileName);

            Files.writeString(
                    file,
                    html,
                    StandardCharsets.UTF_8
            );

            return file;

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to generate anniversary card",
                    e
            );
        }
    }

    private String safe(String value) {

        if (value == null || value.isBlank()) {
            return "N/A";
        }

        return escapeHtml(value);
    }

    private String escapeHtml(String value) {

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}