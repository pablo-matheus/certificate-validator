package com.pablomatheus.certificatevalidator.request;

import com.pablomatheus.certificatevalidator.enumeration.PKCS7SignatureAlgorithm;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PKCS7CertificateRequest {

    @NotNull
    private PKCS7CertificateMessageRequest message;

    @NotNull
    private PKCS7SignatureAlgorithm signatureAlgorithm;

}
