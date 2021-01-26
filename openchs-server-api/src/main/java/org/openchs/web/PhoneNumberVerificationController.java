package org.openchs.web;

import org.openchs.service.PhoneNumberVerificationService;
import org.openchs.web.request.PhoneNumberVerificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class PhoneNumberVerificationController {
    private final Logger logger;
    private final PhoneNumberVerificationService phoneNumberVerificationService;

    @Autowired
    public PhoneNumberVerificationController(PhoneNumberVerificationService phoneNumberVerificationService) {
        this.logger =  LoggerFactory.getLogger(this.getClass());
        this.phoneNumberVerificationService = phoneNumberVerificationService;
    }

    @RequestMapping(value = "/phoneNumberVerification/otp/send", method = RequestMethod.POST)
    @PreAuthorize(value = "hasAnyAuthority('user', 'organisation_admin')")
    public ResponseEntity.BodyBuilder sendOTP(@RequestBody PhoneNumberVerificationRequest phoneNumberVerificationRequest) {
        logger.info("Request: " + phoneNumberVerificationRequest.getPhoneNumber());
        try {
            phoneNumberVerificationService.sendOTP(phoneNumberVerificationRequest.getPhoneNumber());
            return ResponseEntity.ok();
        } catch (Exception e) {
            return ResponseEntity.badRequest();
        }
    }

    @RequestMapping(value = "/phoneNumberVerification/otp/resend", method = RequestMethod.POST)
    @PreAuthorize(value = "hasAnyAuthority('user', 'organisation_admin')")
    public void resendOTP(@RequestBody PhoneNumberVerificationRequest phoneNumberVerificationRequest) {
        phoneNumberVerificationService.resendOTP(phoneNumberVerificationRequest.getPhoneNumber());
    }

    @RequestMapping(value = "/phoneNumberVerification/otp/verify", method = RequestMethod.POST)
    @PreAuthorize(value = "hasAnyAuthority('user', 'organisation_admin')")
    public void verifyOTP(@RequestBody PhoneNumberVerificationRequest phoneNumberVerificationRequest) {
        phoneNumberVerificationService.verifyOTP(phoneNumberVerificationRequest.getPhoneNumber(), phoneNumberVerificationRequest.getOtp());
    }

    @RequestMapping(value = "/phoneNumberVerification/otp/setup", method = RequestMethod.POST)
    @PreAuthorize(value = "hasAnyAuthority('user', 'organisation_admin')")
    public void storeConfiguration() {

    }

    @RequestMapping(value = "/phoneNumberVerification/otp/setup/check", method = RequestMethod.POST)
    @PreAuthorize(value = "hasAnyAuthority('user', 'organisation_admin')")
    public void verifyConfiguration() {
        phoneNumberVerificationService.checkBalance("352153AeIrN0yEO0T600670dfP1");
    }

}
