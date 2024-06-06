package com.pablomatheus.certificatevalidator.service.impl;

import com.pablomatheus.certificatevalidator.exception.BusinessException;
import com.pablomatheus.certificatevalidator.request.PKCS7CertificateValidationRequest;
import com.pablomatheus.certificatevalidator.response.PKCS7CertificateValidationResponse;
import com.pablomatheus.certificatevalidator.service.CertificateValidationService;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignerDigestMismatchException;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.Store;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;

import static com.pablomatheus.certificatevalidator.util.PKCS7Constants.PKCS7_FOOTER;
import static com.pablomatheus.certificatevalidator.util.PKCS7Constants.PKCS7_HEADER;

@Slf4j
@Service
public class PKCS7CertificateValidationServiceImpl implements CertificateValidationService {

    @Override
    public PKCS7CertificateValidationResponse validateSignature(PKCS7CertificateValidationRequest request) {
        Security.addProvider(new BouncyCastleProvider());

        String formattedCertificate = formatCertificate(request);
        byte[] formattedCertificateBase64 = Base64.decode(formattedCertificate);

        CMSSignedData cmsSignedData = getCMSSignedData(formattedCertificateBase64, request.getMessage());
        X509Certificate signerCert = getX509Certificate(cmsSignedData);

        return validateSignature(cmsSignedData, signerCert);
    }

    private PKCS7CertificateValidationResponse validateSignature(CMSSignedData cmsSignedData, X509Certificate signerCert) {
        try {
            SignerInformationStore signers = cmsSignedData.getSignerInfos();
            Collection<SignerInformation> signerInfos = signers.getSigners();
            boolean signatureValid = false;
            for (SignerInformation signerInfo : signerInfos) {
                if (signerInfo.verify(new JcaSimpleSignerInfoVerifierBuilder().setProvider("BC").build(signerCert))) {
                    signatureValid = true;
                    break;
                }
            }

            return PKCS7CertificateValidationResponse.builder()
                    .isValid(signatureValid)
                    .build();
        } catch (CMSSignerDigestMismatchException exception) {
            log.warn("Invalid message", exception);
            return PKCS7CertificateValidationResponse.builder()
                    .isValid(false)
                    .description("The provided message does not match with the used to generate the signature")
                    .build();

        } catch (Exception exception) {
            log.error("Error while validating the signature", exception);
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while validating the signature");
        }
    }

    private CMSSignedData getCMSSignedData(byte[] rawCertificateByteArray, String message) {
        try {
            if (message != null) {
                return new CMSSignedData(new CMSProcessableByteArray(message.getBytes()), rawCertificateByteArray);
            }

            return new CMSSignedData(new ByteArrayInputStream(rawCertificateByteArray));
        } catch (Exception exception) {
            log.error("Error while getting the CMS signed data", exception);
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Error while getting the CMS signed data");
        }
    }

    private X509Certificate getX509Certificate(CMSSignedData signedData) {
        Store<X509CertificateHolder> certStore = signedData.getCertificates();
        Collection<X509CertificateHolder> certs = certStore.getMatches(null);

        JcaX509CertificateConverter certConverter = new JcaX509CertificateConverter();
        return getSignerCert(certs, certConverter);
    }

    private X509Certificate getSignerCert(Collection<X509CertificateHolder> certs, JcaX509CertificateConverter certConverter) {
        return certs.stream()
                .findFirst()
                .map(cert -> {
                    try {
                        return certConverter.getCertificate(cert);
                    } catch (CertificateException exception) {
                        log.error("Error while converting the certificate to X509", exception);
                        throw new BusinessException(HttpStatus.BAD_REQUEST, "Error while converting the certificate to X509");
                    }
                })
                .orElseThrow(() -> {
                    log.error("Error while getting the signer cert");
                    return new BusinessException(HttpStatus.BAD_REQUEST, "No signer certificate found in the PKCS-7 data");
                });
    }

    private String formatCertificate(PKCS7CertificateValidationRequest request) {
        String pkcs7Base64 = request.getCertificate();

        return pkcs7Base64.replaceAll(PKCS7_HEADER, "")
                .replaceAll(PKCS7_FOOTER, "")
                .replaceAll("\\s", "");
    }

}
