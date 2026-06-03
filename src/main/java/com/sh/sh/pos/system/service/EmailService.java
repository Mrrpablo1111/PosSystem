package com.sh.sh.pos.system.service;

public interface EmailService {
    void sendEmail(String  to, String subject, String body);
}
