package com.smarthr.service;

import com.smarthr.entity.Employee;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class BirthdayCardService {

    /*
     * =========================================================
     * CARD SIZE
     * =========================================================
     */

    private static final int WIDTH = 1200;
    private static final int HEIGHT = 675;

    /*
     * =========================================================
     * CEIPAL COLORS
     * =========================================================
     */

    private static final Color CEIPAL_BLUE =
            new Color(0, 84, 153);

    private static final Color CEIPAL_ORANGE =
            new Color(244, 105, 51);

    private static final Color DARK_BLUE =
            new Color(8, 43, 91);

    private static final Color TEXT_DARK =
            new Color(45, 45, 45);

    private static final Color LIGHT_BACKGROUND =
            new Color(248, 249, 251);

    /*
     * =========================================================
     * CONFIGURATION
     * =========================================================
     *
     * Employee photos will be searched here.
     *
     * Example:
     *
     * C:/Users/sairam/Desktop/java/smarthr/smarthr/
     * employee-photos/10.jpg
     *
     */

    @Value("${ceipalhr.employee-photo-dir:employee-photos}")
    private String employeePhotoDirectory;

    /*
     * Generated birthday cards will be stored here.
     */

    @Value("${ceipalhr.birthday-card-dir:generated/birthday-cards}")
    private String birthdayCardDirectory;

    /*
     * =========================================================
     * MAIN METHOD
     * =========================================================
     */

    public Path generateCard(Employee employee) {

        if (employee == null) {

            throw new IllegalArgumentException(
                    "Employee cannot be null."
            );
        }

        try {

            /*
             * -------------------------------------------------
             * CREATE OUTPUT DIRECTORY
             * -------------------------------------------------
             */

            Path outputDirectory =
                    Paths.get(birthdayCardDirectory);

            Files.createDirectories(outputDirectory);

            /*
             * -------------------------------------------------
             * CREATE CANVAS
             * -------------------------------------------------
             */

            BufferedImage card =
                    new BufferedImage(
                            WIDTH,
                            HEIGHT,
                            BufferedImage.TYPE_INT_ARGB
                    );

            Graphics2D g =
                    card.createGraphics();

            /*
             * Better rendering quality
             */

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g.setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON
            );

            g.setRenderingHint(
                    RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY
            );

            /*
             * -------------------------------------------------
             * BACKGROUND
             * -------------------------------------------------
             */

            g.setColor(Color.WHITE);

            g.fillRect(
                    0,
                    0,
                    WIDTH,
                    HEIGHT
            );

            /*
             * -------------------------------------------------
             * TOP LEFT CEIPAL BRAND AREA
             * -------------------------------------------------
             */

            drawCeipalBrand(
                    g
            );

            /*
             * -------------------------------------------------
             * DECORATIVE BLUE SHAPE
             * -------------------------------------------------
             */

            drawDecorations(
                    g
            );

            /*
             * -------------------------------------------------
             * HAPPY BIRTHDAY TEXT
             * -------------------------------------------------
             */

            drawBirthdayHeading(
                    g
            );

            /*
             * -------------------------------------------------
             * EMPLOYEE PHOTO
             * -------------------------------------------------
             */

            BufferedImage employeePhoto =
                    loadEmployeePhoto(employee);

            drawEmployeePhoto(
                    g,
                    employeePhoto
            );

            /*
             * -------------------------------------------------
             * EMPLOYEE DETAILS
             * -------------------------------------------------
             */

            String employeeName =
                    safe(employee.getName());

            String designation =
                    safe(employee.getDesignation());

            String department =
                    safe(employee.getDepartment());

            drawEmployeeDetails(
                    g,
                    employeeName,
                    designation,
                    department
            );

            /*
             * -------------------------------------------------
             * BIRTHDAY MESSAGE
             * -------------------------------------------------
             */

            drawBirthdayMessage(
                    g
            );

            /*
             * -------------------------------------------------
             * FINISH
             * -------------------------------------------------
             */

            g.dispose();

            /*
             * -------------------------------------------------
             * OUTPUT FILE
             * -------------------------------------------------
             */

            String employeeId =
                    String.valueOf(
                            employee.getId()
                    );

            String safeName =
                    employeeName
                            .replaceAll(
                                    "[^a-zA-Z0-9_-]",
                                    "_"
                            );

            String fileName =
                    "birthday-"
                            + employeeId
                            + "-"
                            + safeName
                            + ".png";

            Path outputFile =
                    outputDirectory.resolve(
                            fileName
                    );

            /*
             * -------------------------------------------------
             * WRITE PNG
             * -------------------------------------------------
             */

            ImageIO.write(
                    card,
                    "png",
                    outputFile.toFile()
            );

            return outputFile;

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to generate birthday card for employee: "
                            + safe(employee.getName()),
                    e
            );
        }
    }

    /*
     * =========================================================
     * CEIPAL BRAND
     * =========================================================
     */

    private void drawCeipalBrand(
            Graphics2D g) {

        /*
         * Simple Ceipal-style icon.
         *
         * You can later replace this with the actual
         * Ceipal logo PNG.
         */

        g.setStroke(
                new BasicStroke(
                        7f,
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND
                )
        );

        /*
         * Blue circle
         */

        g.setColor(
                CEIPAL_BLUE
        );

        g.drawOval(
                55,
                42,
                25,
                25
        );

        /*
         * Orange circle
         */

        g.setColor(
                CEIPAL_ORANGE
        );

        g.drawOval(
                83,
                42,
                25,
                25
        );

        /*
         * Brand text
         */

        g.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        38
                )
        );

        g.setColor(
                CEIPAL_BLUE
        );

        g.drawString(
                "Ceipal",
                125,
                70
        );
    }

    /*
     * =========================================================
     * DECORATIONS
     * =========================================================
     */

    private void drawDecorations(
            Graphics2D g) {

        /*
         * Large dark-blue corner shape
         */

        g.setColor(
                DARK_BLUE
        );

        int[] xPoints = {
                850,
                1200,
                1200,
                1030
        };

        int[] yPoints = {
                0,
                0,
                675,
                675
        };

        g.fillPolygon(
                xPoints,
                yPoints,
                4
        );

        /*
         * Orange curved-looking frame around photo
         */

        g.setColor(
                CEIPAL_ORANGE
        );

        g.setStroke(
                new BasicStroke(
                        3f
                )
        );

        g.drawOval(
                770,
                75,
                350,
                350
        );

        /*
         * Decorative orange dots
         */

        g.fillOval(
                780,
                82,
                12,
                12
        );

        g.fillOval(
                1110,
                410,
                12,
                12
        );
    }

    /*
     * =========================================================
     * BIRTHDAY HEADING
     * =========================================================
     */

    private void drawBirthdayHeading(
            Graphics2D g) {

        /*
         * HAPPY
         */

        g.setFont(
                new Font(
                        "Serif",
                        Font.BOLD,
                        34
                )
        );

        g.setColor(
                DARK_BLUE
        );

        g.drawString(
                "H A P P Y",
                235,
                145
        );

        /*
         * Birthday
         */

        g.setFont(
                new Font(
                        "Serif",
                        Font.ITALIC,
                        82
                )
        );

        g.setColor(
                new Color(
                        190,
                        125,
                        20
                )
        );

        g.drawString(
                "Birthday!",
                65,
                235
        );

        /*
         * Divider
         */

        g.setColor(
                CEIPAL_ORANGE
        );

        g.setStroke(
                new BasicStroke(
                        2f
                )
        );

        g.drawLine(
                165,
                265,
                540,
                265
        );

        /*
         * Small star
         */

        g.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        28
                )
        );

        g.drawString(
                "✦",
                350,
                275
        );
    }

    /*
     * =========================================================
     * EMPLOYEE PHOTO
     * =========================================================
     */

    private void drawEmployeePhoto(
            Graphics2D g,
            BufferedImage photo) {

        int x = 780;
        int y = 82;
        int size = 330;

        /*
         * Photo circle
         */

        Ellipse2D circle =
                new Ellipse2D.Double(
                        x,
                        y,
                        size,
                        size
                );

        /*
         * Save current graphics state
         */

        java.awt.Shape oldClip =
                g.getClip();

        /*
         * Clip to circle
         */

        g.setClip(
                circle
        );

        /*
         * Draw photo
         */

        if (photo != null) {

            g.drawImage(
                    photo,
                    x,
                    y,
                    size,
                    size,
                    null
            );

        } else {

            /*
             * Placeholder if photo is not found
             */

            g.setColor(
                    new Color(
                            225,
                            230,
                            235
                    )
            );

            g.fillOval(
                    x,
                    y,
                    size,
                    size
            );

            g.setColor(
                    CEIPAL_BLUE
            );

            g.setFont(
                    new Font(
                            "Arial",
                            Font.BOLD,
                            30
                    )
            );

            String text =
                    "PHOTO";

            FontMetrics fm =
                    g.getFontMetrics();

            int textX =
                    x
                            + (
                            size
                                    - fm.stringWidth(text)
                    ) / 2;

            int textY =
                    y
                            + (
                            size
                                    - fm.getHeight()
                    ) / 2
                            + fm.getAscent();

            g.drawString(
                    text,
                    textX,
                    textY
            );
        }

        /*
         * Restore clip
         */

        g.setClip(
                oldClip
        );

        /*
         * Orange circle border
         */

        g.setColor(
                CEIPAL_ORANGE
        );

        g.setStroke(
                new BasicStroke(
                        4f
                )
        );

        g.drawOval(
                x,
                y,
                size,
                size
        );
    }

    /*
     * =========================================================
     * EMPLOYEE DETAILS
     * =========================================================
     */

    private void drawEmployeeDetails(
            Graphics2D g,
            String name,
            String designation,
            String department) {

        /*
         * Name
         */

        g.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        32
                )
        );

        g.setColor(
                CEIPAL_BLUE
        );

        drawCenteredText(
                g,
                name.toUpperCase(),
                350,
                320
        );

        /*
         * Designation
         */

        g.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        20
                )
        );

        g.setColor(
                Color.WHITE
        );

        /*
         * Blue designation ribbon
         */

        g.fill(
                new RoundRectangle2D.Double(
                        125,
                        350,
                        440,
                        50,
                        8,
                        8
                )
        );

        g.setColor(
                DARK_BLUE
        );

        g.fill(
                new RoundRectangle2D.Double(
                        125,
                        350,
                        440,
                        50,
                        8,
                        8
                )
        );

        g.setColor(
                Color.WHITE
        );

        drawCenteredText(
                g,
                designation.toUpperCase(),
                350,
                383
        );

        /*
         * Department
         */

        g.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        19
                )
        );

        g.setColor(
                CEIPAL_BLUE
        );

        drawCenteredText(
                g,
                department.toUpperCase(),
                350,
                435
        );
    }

    /*
     * =========================================================
     * BIRTHDAY MESSAGE
     * =========================================================
     */

    private void drawBirthdayMessage(
            Graphics2D g) {

        g.setFont(
                new Font(
                        "Arial",
                        Font.PLAIN,
                        21
                )
        );

        g.setColor(
                TEXT_DARK
        );

        drawCenteredText(
                g,
                "Wishing you a day filled with happiness, love,",
                350,
                485
        );

        drawCenteredText(
                g,
                "and laughter. May this year bring you success,",
                350,
                515
        );

        drawCenteredText(
                g,
                "good health, and all the happiness you deserve.",
                350,
                545
        );
    }

    /*
     * =========================================================
     * CENTERED TEXT
     * =========================================================
     */

    private void drawCenteredText(
            Graphics2D g,
            String text,
            int centerX,
            int y) {

        FontMetrics metrics =
                g.getFontMetrics();

        int textWidth =
                metrics.stringWidth(
                        text
                );

        g.drawString(
                text,
                centerX - textWidth / 2,
                y
        );
    }

    /*
     * =========================================================
     * LOAD EMPLOYEE PHOTO
     * =========================================================
     */

    private BufferedImage loadEmployeePhoto(
            Employee employee) {

        String id =
                String.valueOf(
                        employee.getId()
                );

        String[] extensions = {
                ".jpg",
                ".jpeg",
                ".png"
        };

        for (String extension :
                extensions) {

            Path photoPath =
                    Paths.get(
                            employeePhotoDirectory,
                            id + extension
                    );

            File file =
                    photoPath.toFile();

            if (file.exists() &&
                    file.isFile()) {

                try {

                    return ImageIO.read(
                            file
                    );

                } catch (IOException e) {

                    throw new RuntimeException(
                            "Unable to read employee photo: "
                                    + file.getAbsolutePath(),
                            e
                    );
                }
            }
        }

        /*
         * Photo not found.
         *
         * The card will use the PHOTO placeholder.
         */

        return null;
    }

    /*
     * =========================================================
     * SAFE TEXT
     * =========================================================
     */

    private String safe(
            String value) {

        if (value == null ||
                value.isBlank()) {

            return "Employee";
        }

        return value.trim();
    }
}