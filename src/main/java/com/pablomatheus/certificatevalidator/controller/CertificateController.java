package com.pablomatheus.certificatevalidator.controller;

import com.pablomatheus.certificatevalidator.domain.request.PKCS7CertificateRequest;
import com.pablomatheus.certificatevalidator.domain.request.PKCS7CertificateValidationRequest;
import com.pablomatheus.certificatevalidator.domain.response.PKCS7CertificateResponse;
import com.pablomatheus.certificatevalidator.domain.response.PKCS7CertificateValidationResponse;
import com.pablomatheus.certificatevalidator.domain.response.PKCS7DetailedCertificateResponse;
import com.pablomatheus.certificatevalidator.service.CertificateService;
import com.pablomatheus.certificatevalidator.service.CertificateValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/certificates")
@RestController
public class CertificateController {

    private final CertificateService certificateService;
    private final CertificateValidationService certificateValidationService;

    @PostMapping("/pkcs-7")
    public PKCS7CertificateResponse generate(@RequestBody PKCS7CertificateRequest request) {
        return certificateService.generate(request);
    }

    @PostMapping("/pkcs-7/details")
    public PKCS7DetailedCertificateResponse showDetails() {
        return null;
    }

    @PostMapping("/pkcs-7/validations/signatures")
    public PKCS7CertificateValidationResponse validateSignature(@RequestBody PKCS7CertificateValidationRequest request) {
        return certificateValidationService.validateSignature(request);
    }

}
