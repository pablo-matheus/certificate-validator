package com.pablomatheus.certificatevalidator.domain.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PKCS7SignatureAlgorithm {

    SHA256_WITH_ECDSA("SHA256withECDSA"),
    SHA256_WITH_RSA("SHA256withRSA");

    private final String value;

}
