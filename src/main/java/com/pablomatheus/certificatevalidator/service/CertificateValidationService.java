package com.pablomatheus.certificatevalidator.service;

import com.pablomatheus.certificatevalidator.domain.request.PKCS7CertificateValidationRequest;
import com.pablomatheus.certificatevalidator.domain.response.PKCS7CertificateValidationResponse;

public interface CertificateValidationService {

    PKCS7CertificateValidationResponse validateSignature(PKCS7CertificateValidationRequest request);

}
