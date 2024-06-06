package com.pablomatheus.certificatevalidator.service;

import com.pablomatheus.certificatevalidator.request.PKCS7CertificateRequest;
import com.pablomatheus.certificatevalidator.request.PKCS7CertificateValidationRequest;
import com.pablomatheus.certificatevalidator.response.PKCS7CertificateResponse;

public interface CertificateService {

    PKCS7CertificateResponse generate(PKCS7CertificateRequest pkcs7CertificateRequest);

}
