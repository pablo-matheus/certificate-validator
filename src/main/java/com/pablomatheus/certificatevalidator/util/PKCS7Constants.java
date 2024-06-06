package com.pablomatheus.certificatevalidator.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PKCS7Constants {

    public static final String PKCS7_HEADER = "-----BEGIN PKCS7-----";
    public static final String PKCS7_FOOTER = "-----END PKCS7-----";

}
