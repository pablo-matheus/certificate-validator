package com.pablomatheus.certificatevalidator.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PKCS7CertificateResponse {

    private String certificate;
    private String format;

}
