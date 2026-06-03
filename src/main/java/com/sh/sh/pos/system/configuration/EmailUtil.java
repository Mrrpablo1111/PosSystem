// package com.sh.sh.pos.system.configuration;

// import org.springframework.mail.MailException;
// import org.springframework.mail.MailSendException;
// import org.springframework.mail.javamail.JavaMailSender;
// import org.springframework.mail.javamail.MimeMessageHelper;
// import org.springframework.scheduling.annotation.Async;
// import org.springframework.stereotype.Component;

// import jakarta.mail.MessagingException;
// import jakarta.mail.internet.MimeMessage;

// @Component
// public class EmailUtil {

//     private final JavaMailSender javaMailSender;

//     public EmailUtil(JavaMailSender javaMailSender) {
//         this.javaMailSender = javaMailSender;
//     }

//     @Async("taskExecutor")
//     public void sendLinkEmail(String email, String link) {
//         System.out.println("🔥 Email method thread: " + Thread.currentThread().getName());
//         try {
//             MimeMessage message = javaMailSender.createMimeMessage();
//             MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

//             helper.setTo(email);
//             helper.setSubject("Reset Your Password - SH POS System");

//             String html = """
//                     <!DOCTYPE html>
//                     <html lang="en">
//                     <head>
//                         <meta charset="UTF-8">
//                         <meta name="viewport" content="width=device-width, initial-scale=1.0">
//                     </head>

//                     <body style="
//                         margin:0;
//                         padding:0;
//                         background-color:#f4f5f7;
//                         font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Arial,sans-serif;
//                     ">

//                     <table width="100%%" cellpadding="0" cellspacing="0" role="presentation">
//                         <tr>
//                             <td align="center" style="padding:40px 20px;">

//                                 <!-- Container -->
//                                 <table width="520" cellpadding="0" cellspacing="0" role="presentation"
//                                     style="
//                                         background:#ffffff;
//                                         border-radius:18px;
//                                         overflow:hidden;
//                                         border:1px solid #e5e7eb;
//                                         box-shadow:0 8px 24px rgba(0,0,0,0.05);
//                                     ">

//                                     <!-- Header -->
//                                     <tr>
//                                         <td align="center" style="background: #0f766e; padding: 40px 32px 36px;">

//                                             <table cellpadding="0" cellspacing="0" role="presentation">
//                                                 <tr>
//                                                     <td align="center"
//                                                         style="
//                                                             width: 56px;
//                                                             height: 56px;
//                                                             border-radius: 14px;
//                                                             background: rgba(255,255,255,0.15);
//                                                             border: 1.5px solid rgba(255,255,255,0.3);
//                                                             font-size: 15px;
//                                                             font-weight: 600;
//                                                             color: #ffffff;
//                                                             letter-spacing: 0.5px;
//                                                         ">
//                                                         POS
//                                                     </td>
//                                                 </tr>
//                                             </table>

//                                             <p style="
//                                                 margin: 14px 0 4px;
//                                                 font-size: 18px;
//                                                 font-weight: 600;
//                                                 color: #ffffff;
//                                                 letter-spacing: 0.2px;
//                                             ">
//                                                 SH POS System
//                                             </p>

//                                             <p style="
//                                                 margin: 0;
//                                                 font-size: 11px;
//                                                 color: rgba(255,255,255,0.6);
//                                                 letter-spacing: 1px;
//                                                 text-transform: uppercase;
//                                             ">
//                                                 Smart Point of Sale Management
//                                             </p>

//                                         </td>
//                                     </tr>

//                                     <!-- BODY -->
//                                     <tr>
//                                         <td style="padding:32px 28px;">

//                                             <h3 style="
//                                                 margin:0 0 12px;
//                                                 font-size:18px;
//                                                 color:#111827;
//                                             ">
//                                                 Reset your password
//                                             </h3>

//                                             <p style="
//                                                 margin:0 0 20px;
//                                                 font-size:14px;
//                                                 color:#6b7280;
//                                                 line-height:1.7;
//                                             ">
//                                                 We received a request to reset your SH POS account password.
//                                                 Click the button below to create a new password.
//                                             </p>

//                                             <!-- BUTTON -->
//                                             <div style="text-align:center;margin:24px 0;">
//                                                 <a href="%s"
//                                                     style="
//                                                         background:#0f766e;
//                                                         color:#ffffff;
//                                                         text-decoration:none;
//                                                         padding:14px 34px;
//                                                         border-radius:10px;
//                                                         font-size:14px;
//                                                         font-weight:600;
//                                                         display:inline-block;
//                                                         box-shadow:0 6px 14px rgba(15,118,110,0.25);
//                                                     ">
//                                                     Reset Password
//                                                 </a>
//                                             </div>

//                                             <!-- EXPIRY NOTICE -->
//                                             <div style="
//                                                 margin-top:24px;
//                                                 padding:12px;
//                                                 background:#f9fafb;
//                                                 border-radius:10px;
//                                                 font-size:12px;
//                                                 color:#6b7280;
//                                                 line-height:1.6;
//                                             ">
//                                                 - This link will expire in <b style="color:#111827;">15 minutes</b>.
//                                                 Please use it before it becomes invalid.
//                                             </div>

//                                             <!-- SECURITY NOTICE -->
//                                             <div style="
//                                                 margin-top:12px;
//                                                 padding:12px;
//                                                 border:1px solid #e5e7eb;
//                                                 border-radius:10px;
//                                                 font-size:12px;
//                                                 color:#6b7280;
//                                                 line-height:1.6;
//                                             ">
//                                                 - If you did not request this password reset, you can safely ignore this email.
//                                                 Your password will remain unchanged.
//                                             </div>

//                                         </td>
//                                     </tr>

//                                     <!-- FOOTER -->
//                                     <tr>
//                                         <td style="
//                                             padding:16px;
//                                             border-top:1px solid #f3f4f6;
//                                             text-align:center;
//                                             font-size:11px;
//                                             color:#9ca3af;
//                                         ">
//                                             © 2026 SH POS System · Secure · Fast · Reliable
//                                         </td>
//                                     </tr>

//                                 </table>
//                             </td>
//                         </tr>
//                     </table>

//                     </body>
//                     </html>
//                     """
//                     .formatted(link);

//             helper.setText(html, true);
//             javaMailSender.send(message);
//             System.out.println(" Email sent to: " + email);

//         } catch (Exception e) {
//             System.err.println(" Email error: " + e.getMessage());
//         }
//     }
// }