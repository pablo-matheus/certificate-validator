package com.pablomatheus.certificatevalidator.service;

import com.pablomatheus.certificatevalidator.domain.request.PKCS7CertificateRequest;
import com.pablomatheus.certificatevalidator.domain.response.PKCS7CertificateResponse;

public interface CertificateGenerationService {

    PKCS7CertificateResponse generate(PKCS7CertificateRequest pkcs7CertificateRequest);

}
