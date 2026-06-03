// package com.sh.sh.pos.system.service.serviceImpl;

// import org.springframework.mail.MailException;
// import org.springframework.mail.MailSendException;
// import org.springframework.mail.javamail.JavaMailSender;
// import org.springframework.mail.javamail.MimeMessageHelper;
// import org.springframework.scheduling.annotation.Async;
// import org.springframework.stereotype.Service;

// import com.sh.sh.pos.system.service.EmailService;

// import jakarta.mail.MessagingException;
// import jakarta.mail.internet.MimeMessage;
// import lombok.RequiredArgsConstructor;

// @Service
// @RequiredArgsConstructor
// public class EmailServiceImpl implements EmailService{
//     private final JavaMailSender javaMailSender;

//      @Async("taskExecutor")
//     @Override
//     public void sendEmail(String to, String subject, String body) {
        
//         try {
//             MimeMessage mimeMessage = javaMailSender.createMimeMessage();
//             MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

//             helper.setSubject(subject);
//             helper.setText(body, true);
//             helper.setTo(to);
            
//             javaMailSender.send(mimeMessage);
//             System.out.println(" EMAIL SENT TO: " + to);
            
//         } catch (MailException | MessagingException e) {
//             throw new MailSendException("Failed to send email");
//         }
//     }
        
// }
