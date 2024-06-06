package com.pablomatheus.certificatevalidator.service.impl;

import com.pablomatheus.certificatevalidator.domain.exception.BusinessException;
import com.pablomatheus.certificatevalidator.domain.request.PKCS7CertificateRequest;
import com.pablomatheus.certificatevalidator.domain.response.PKCS7CertificateResponse;
import com.pablomatheus.certificatevalidator.service.CertificateGenerationService;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.security.auth.x500.X500Principal;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.pablomatheus.certificatevalidator.util.CommonConstants.BOUNCY_CASTLE_PROVIDER;
import static com.pablomatheus.certificatevalidator.util.PKCS7Constants.PKCS7_FOOTER;
import static com.pablomatheus.certificatevalidator.util.PKCS7Constants.PKCS7_HEADER;

@Slf4j
@Service
public class PKCS7CertificateGenerationServiceImpl implements CertificateGenerationService {

    @Override
    public PKCS7CertificateResponse generate(PKCS7CertificateRequest pkcs7CertificateRequest) {
        Security.addProvider(new BouncyCastleProvider());

        KeyPair keyPair = generateKeyPair();
        X509Certificate certificate = generateSelfSignedCertificate(keyPair, pkcs7CertificateRequest.getSignatureAlgorithm().getValue());

        CMSSignedData signedData =
                createPKCS7SignedData(
                        certificate, keyPair, pkcs7CertificateRequest.getMessage().getValue(),
                        pkcs7CertificateRequest.getMessage().getIsAttachedToSignature(),
                        pkcs7CertificateRequest.getSignatureAlgorithm().getValue());

        return formatCertificate(signedData);
    }

    private static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC", BOUNCY_CASTLE_PROVIDER);
            keyPairGenerator.initialize(256);
            return keyPairGenerator.generateKeyPair();
        } catch (Exception exception) {
            log.error("Error while generating the key pair", exception);
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Key pair generation error");
        }
    }

    private static X509Certificate generateSelfSignedCertificate(KeyPair keyPair, String algorithm) {
        try {
            X500Principal subject = new X500Principal("CN=Test Certificate");
            BigInteger serial = BigInteger.valueOf(new SecureRandom().nextInt());
            Date notBefore = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.YEAR, 1);
            Date notAfter = calendar.getTime();

            JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                    subject, serial, notBefore, notAfter, subject, keyPair.getPublic());

            JcaContentSignerBuilder signerBuilder = new JcaContentSignerBuilder(algorithm);

            ContentSigner contentSigner = signerBuilder.build(keyPair.getPrivate());

            X509CertificateHolder certHolder = certBuilder.build(contentSigner);
            return new JcaX509CertificateConverter().getCertificate(certHolder);
        } catch (Exception exception) {
            log.error("Error while generating the certificate", exception);
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Certificate generation error");
        }
    }

    private static CMSSignedData createPKCS7SignedData(X509Certificate certificate,
                                                       KeyPair keyPair,
                                                       String message,
                                                       boolean includeMessageInTheSignature,
                                                       String algorithm) {
        try {
            List<X509Certificate> certList = new ArrayList<>();
            certList.add(certificate);
            JcaCertStore certs = new JcaCertStore(certList);

            JcaContentSignerBuilder signerBuilder = new JcaContentSignerBuilder(algorithm);
            signerBuilder.setProvider(BOUNCY_CASTLE_PROVIDER);

            CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
            generator.addSignerInfoGenerator(
                    new JcaSignerInfoGeneratorBuilder(
                            new JcaDigestCalculatorProviderBuilder()
                                    .setProvider(BOUNCY_CASTLE_PROVIDER)
                                    .build())
                            .build(signerBuilder.build(keyPair.getPrivate()), certificate));

            generator.addCertificates(certs);

            CMSTypedData contentToSign = new CMSProcessableByteArray(message.getBytes());
            return generator.generate(contentToSign, includeMessageInTheSignature);
        } catch (Exception exception) {
            log.error("Error while signing the certificate", exception);
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Certificate signing error");
        }
    }

    private static PKCS7CertificateResponse formatCertificate(CMSSignedData signedData) {
        try {
            byte[] pkcs7Bytes = signedData.getEncoded();
            String pkcs7Base64 = Base64.toBase64String(pkcs7Bytes);
            return PKCS7CertificateResponse.builder()
                    .format("PKCS-7")
                    .certificate(String.format("%s%n%s%n%s", PKCS7_HEADER, pkcs7Base64, PKCS7_FOOTER))
                    .build();
        } catch (Exception exception) {
            log.error("Error while formatting the certificate", exception);
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Error formatting the certificate");
        }
    }

}
