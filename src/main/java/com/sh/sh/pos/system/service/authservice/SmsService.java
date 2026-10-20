package com.sh.sh.pos.system.service.authservice;

public interface SmsService {
    void sendOtp(String phone, String otp);
}
