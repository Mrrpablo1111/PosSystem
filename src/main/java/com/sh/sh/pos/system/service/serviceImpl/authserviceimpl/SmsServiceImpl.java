package com.sh.sh.pos.system.service.serviceimpl.authserviceimpl;

import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.service.authservice.SmsService;

import lombok.extern.slf4j.Slf4j;

@Slf4j 
@Service 
public class SmsServiceImpl implements SmsService {

    @Override
    public void sendOtp(String phone, String otp) {
        /*
         * * DEVELOPMENT ONLY * * Later replace this with: * Twilio * Vonage * another
         * SMS provider
         */ log.info("SMS OTP → phone={} otp={}", phone, otp);
    }

}
