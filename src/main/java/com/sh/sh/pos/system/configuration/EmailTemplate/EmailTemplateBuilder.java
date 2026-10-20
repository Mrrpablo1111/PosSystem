package com.sh.sh.pos.system.configuration.emailtemplate;

import org.springframework.stereotype.Component;

@Component
public class EmailTemplateBuilder {

    // =========================================================
    // RESET PASSWORD LINK
    // =========================================================

    public String buildResetPasswordEmail(String link) {

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                </head>

                <body style="
                    margin:0;
                    padding:0;
                    background-color:#f4f5f7;
                    font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Arial,sans-serif;
                ">

                <table width="100%%" cellpadding="0" cellspacing="0" role="presentation">
                    <tr>
                        <td align="center" style="padding:40px 20px;">

                            <table width="520" cellpadding="0" cellspacing="0" role="presentation"
                                style="
                                    background:#ffffff;
                                    border-radius:18px;
                                    overflow:hidden;
                                    border:1px solid #e5e7eb;
                                    box-shadow:0 8px 24px rgba(0,0,0,0.05);
                                ">

                                <!-- HEADER -->
                                <tr>
                                    <td align="center"
                                        style="
                                            background:#0f766e;
                                            padding:40px 32px 36px;
                                        ">

                                        <table cellpadding="0" cellspacing="0" role="presentation">
                                            <tr>
                                                <td align="center"
                                                    style="
                                                        width:56px;
                                                        height:56px;
                                                        border-radius:14px;
                                                        background:rgba(255,255,255,0.15);
                                                        border:1.5px solid rgba(255,255,255,0.3);
                                                        font-size:15px;
                                                        font-weight:600;
                                                        color:#ffffff;
                                                        letter-spacing:0.5px;
                                                    ">
                                                    POS
                                                </td>
                                            </tr>
                                        </table>

                                        <p style="
                                            margin:14px 0 4px;
                                            font-size:18px;
                                            font-weight:600;
                                            color:#ffffff;
                                        ">
                                            SH POS System
                                        </p>

                                        <p style="
                                            margin:0;
                                            font-size:11px;
                                            color:rgba(255,255,255,0.6);
                                            letter-spacing:1px;
                                            text-transform:uppercase;
                                        ">
                                            Smart Point of Sale Management
                                        </p>

                                    </td>
                                </tr>

                                <!-- BODY -->
                                <tr>
                                    <td style="padding:32px 28px;">

                                        <h3 style="
                                            margin:0 0 12px;
                                            font-size:18px;
                                            color:#111827;
                                        ">
                                            Reset your password
                                        </h3>

                                        <p style="
                                            margin:0 0 20px;
                                            font-size:14px;
                                            color:#6b7280;
                                            line-height:1.7;
                                        ">
                                            We received a request to reset your SH POS account password.
                                            Click the button below to create a new password.
                                        </p>

                                        <!-- BUTTON -->
                                        <div style="text-align:center;margin:24px 0;">
                                            <a href="%s"
                                                style="
                                                    background:#0f766e;
                                                    color:#ffffff;
                                                    text-decoration:none;
                                                    padding:14px 34px;
                                                    border-radius:10px;
                                                    font-size:14px;
                                                    font-weight:600;
                                                    display:inline-block;
                                                    box-shadow:0 6px 14px rgba(15,118,110,0.25);
                                                ">
                                                Reset Password
                                            </a>
                                        </div>

                                        <div style="
                                            margin-top:24px;
                                            padding:12px;
                                            background:#f9fafb;
                                            border-radius:10px;
                                            font-size:12px;
                                            color:#6b7280;
                                            line-height:1.6;
                                        ">
                                            - This link will expire in
                                            <b style="color:#111827;">15 minutes</b>.
                                        </div>

                                        <div style="
                                            margin-top:12px;
                                            padding:12px;
                                            border:1px solid #e5e7eb;
                                            border-radius:10px;
                                            font-size:12px;
                                            color:#6b7280;
                                            line-height:1.6;
                                        ">
                                            - If you did not request a password reset,
                                            you can safely ignore this email.
                                        </div>

                                    </td>
                                </tr>

                                <!-- FOOTER -->
                                <tr>
                                    <td style="
                                        padding:16px;
                                        border-top:1px solid #f3f4f6;
                                        text-align:center;
                                        font-size:11px;
                                        color:#9ca3af;
                                    ">
                                        © 2026 SH POS System · Secure · Fast · Reliable
                                    </td>
                                </tr>

                            </table>
                        </td>
                    </tr>
                </table>

                </body>
                </html>
                """.formatted(link);
    }

    // =========================================================
    // PASSWORD RESET OTP EMAIL
    // =========================================================

    public String buildPasswordResetOtpEmail(
            String name,
            String otp) {

        return """
                <!DOCTYPE html>
                <html lang="en">

                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport"
                          content="width=device-width, initial-scale=1.0">
                </head>

                <body style="
                    margin:0;
                    padding:0;
                    background-color:#f4f5f7;
                    font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Arial,sans-serif;
                ">

                <table width="100%%"
                       cellpadding="0"
                       cellspacing="0"
                       role="presentation">

                    <tr>
                        <td align="center"
                            style="padding:40px 20px;">

                            <!-- CONTAINER -->
                            <table width="520"
                                   cellpadding="0"
                                   cellspacing="0"
                                   role="presentation"
                                   style="
                                       background:#ffffff;
                                       border-radius:18px;
                                       overflow:hidden;
                                       border:1px solid #e5e7eb;
                                       box-shadow:0 8px 24px rgba(0,0,0,0.05);
                                   ">

                                <!-- HEADER -->
                                <tr>
                                    <td align="center"
                                        style="
                                            background:#0f766e;
                                            padding:40px 32px 36px;
                                        ">

                                        <table cellpadding="0"
                                               cellspacing="0"
                                               role="presentation">

                                            <tr>
                                                <td align="center"
                                                    style="
                                                        width:56px;
                                                        height:56px;
                                                        border-radius:14px;
                                                        background:rgba(255,255,255,0.15);
                                                        border:1.5px solid rgba(255,255,255,0.3);
                                                        font-size:15px;
                                                        font-weight:600;
                                                        color:#ffffff;
                                                        letter-spacing:0.5px;
                                                    ">
                                                    POS
                                                </td>
                                            </tr>

                                        </table>

                                        <p style="
                                            margin:14px 0 4px;
                                            font-size:18px;
                                            font-weight:600;
                                            color:#ffffff;
                                        ">
                                            SH POS System
                                        </p>

                                        <p style="
                                            margin:0;
                                            font-size:11px;
                                            color:rgba(255,255,255,0.6);
                                            letter-spacing:1px;
                                            text-transform:uppercase;
                                        ">
                                            Smart Point of Sale Management
                                        </p>

                                    </td>
                                </tr>


                                <!-- BODY -->
                                <tr>
                                    <td style="padding:32px 28px;">

                                        <h3 style="
                                            margin:0 0 12px;
                                            font-size:18px;
                                            color:#111827;
                                        ">
                                            Password reset verification
                                        </h3>


                                        <p style="
                                            margin:0 0 20px;
                                            font-size:14px;
                                            color:#6b7280;
                                            line-height:1.7;
                                        ">
                                            Hello <b style="color:#111827;">%s</b>,
                                        </p>


                                        <p style="
                                            margin:0 0 20px;
                                            font-size:14px;
                                            color:#6b7280;
                                            line-height:1.7;
                                        ">
                                            We received a request to reset your
                                            SH POS account password.
                                            Use the verification code below.
                                        </p>


                                        <!-- OTP -->
                                        <div style="
                                            text-align:center;
                                            margin:28px 0;
                                        ">

                                            <div style="
                                                display:inline-block;
                                                padding:18px 34px;
                                                background:#f0fdfa;
                                                border:1px solid #99f6e4;
                                                border-radius:12px;
                                                color:#0f766e;
                                                font-size:32px;
                                                font-weight:700;
                                                letter-spacing:8px;
                                            ">
                                                %s
                                            </div>

                                        </div>


                                        <!-- EXPIRY -->
                                        <div style="
                                            margin-top:24px;
                                            padding:14px;
                                            background:#f9fafb;
                                            border-radius:10px;
                                            font-size:12px;
                                            color:#6b7280;
                                            line-height:1.6;
                                        ">

                                            Your verification code will expire in
                                            <b style="color:#111827;">
                                                5 minutes
                                            </b>.

                                        </div>


                                        <!-- SECURITY -->
                                        <div style="
                                            margin-top:12px;
                                            padding:14px;
                                            border:1px solid #e5e7eb;
                                            border-radius:10px;
                                            font-size:12px;
                                            color:#6b7280;
                                            line-height:1.6;
                                        ">

                                            If you did not request a password reset,
                                            you can safely ignore this email.
                                            Your password will remain unchanged.

                                        </div>

                                    </td>
                                </tr>


                                <!-- FOOTER -->
                                <tr>
                                    <td style="
                                        padding:16px;
                                        border-top:1px solid #f3f4f6;
                                        text-align:center;
                                        font-size:11px;
                                        color:#9ca3af;
                                    ">

                                        © 2026 SH POS System
                                        · Secure · Fast · Reliable

                                    </td>
                                </tr>

                            </table>

                        </td>
                    </tr>

                </table>

                </body>
                </html>
                """.formatted(name, otp);
    }

    public String buildVerificationOtpEmail(String name, String otp) {

        String safeName = escapeHtml(
                name == null || name.isBlank() ? "User" : name);

        String safeOtp = escapeHtml(otp);

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport"
                          content="width=device-width, initial-scale=1.0">
                </head>

                <body style="
                    margin:0;
                    padding:0;
                    background-color:#f4f5f7;
                    font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Arial,sans-serif;
                ">

                <table width="100%%" cellpadding="0" cellspacing="0"
                       role="presentation">
                    <tr>
                        <td align="center" style="padding:40px 20px;">

                            <table width="520" cellpadding="0" cellspacing="0"
                                   role="presentation"
                                   style="
                                       background:#ffffff;
                                       border-radius:18px;
                                       overflow:hidden;
                                       border:1px solid #e5e7eb;
                                   ">

                                <!-- HEADER -->
                                <tr>
                                    <td align="center"
                                        style="
                                            background:#0f766e;
                                            padding:40px 32px 36px;
                                        ">

                                        <div style="
                                            width:56px;
                                            height:56px;
                                            line-height:56px;
                                            border-radius:14px;
                                            background:rgba(255,255,255,0.15);
                                            border:1px solid rgba(255,255,255,0.3);
                                            font-size:15px;
                                            font-weight:600;
                                            color:#ffffff;
                                            text-align:center;
                                        ">
                                            POS
                                        </div>

                                        <p style="
                                            margin:14px 0 4px;
                                            font-size:18px;
                                            font-weight:600;
                                            color:#ffffff;
                                        ">
                                            SH POS System
                                        </p>

                                        <p style="
                                            margin:0;
                                            font-size:11px;
                                            color:#d1fae5;
                                            letter-spacing:1px;
                                        ">
                                            SMART POINT OF SALE MANAGEMENT
                                        </p>
                                    </td>
                                </tr>

                                <!-- BODY -->
                                <tr>
                                    <td style="padding:32px 28px;">

                                        <h3 style="
                                            margin:0 0 16px;
                                            font-size:20px;
                                            color:#111827;
                                        ">
                                            Verify your account
                                        </h3>

                                        <p style="
                                            margin:0 0 20px;
                                            font-size:14px;
                                            color:#6b7280;
                                            line-height:1.7;
                                        ">
                                            Hello <b style="color:#111827;">%s</b>,
                                        </p>

                                        <p style="
                                            margin:0 0 20px;
                                            font-size:14px;
                                            color:#6b7280;
                                            line-height:1.7;
                                        ">
                                            Thank you for registering with SH POS
                                            System. Enter the verification code
                                            below to activate your account.
                                        </p>

                                        <!-- OTP -->
                                        <div style="
                                            text-align:center;
                                            margin:28px 0;
                                            padding:20px;
                                            background:#f0fdfa;
                                            border:1px solid #99f6e4;
                                            border-radius:12px;
                                        ">
                                            <div style="
                                                color:#0f766e;
                                                font-size:32px;
                                                font-weight:700;
                                                letter-spacing:8px;
                                            ">
                                                %s
                                            </div>
                                        </div>

                                        <!-- EXPIRY -->
                                        <div style="
                                            padding:14px;
                                            background:#f9fafb;
                                            border-radius:10px;
                                            font-size:12px;
                                            color:#6b7280;
                                            line-height:1.6;
                                        ">
                                            Your verification code expires in
                                            <b style="color:#111827;">
                                                5 minutes
                                            </b>.
                                            Please do not share this code with
                                            anyone.
                                        </div>

                                        <!-- SECURITY -->
                                        <div style="
                                            margin-top:12px;
                                            padding:14px;
                                            border:1px solid #e5e7eb;
                                            border-radius:10px;
                                            font-size:12px;
                                            color:#6b7280;
                                            line-height:1.6;
                                        ">
                                            If you did not create an SH POS
                                            account, you can safely ignore
                                            this email.
                                        </div>

                                    </td>
                                </tr>

                                <!-- FOOTER -->
                                <tr>
                                    <td style="
                                        padding:16px;
                                        border-top:1px solid #f3f4f6;
                                        text-align:center;
                                        font-size:11px;
                                        color:#9ca3af;
                                    ">
                                        © 2026 SH POS System
                                        · Secure · Fast · Reliable
                                    </td>
                                </tr>

                            </table>
                        </td>
                    </tr>
                </table>

                </body>
                </html>
                """.formatted(safeName, safeOtp);
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

}