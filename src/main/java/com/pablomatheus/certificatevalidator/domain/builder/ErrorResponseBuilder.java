package com.pablomatheus.certificatevalidator.domain.builder;

import com.pablomatheus.certificatevalidator.domain.exception.BusinessException;
import com.pablomatheus.certificatevalidator.domain.response.ErrorResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorResponseBuilder {

    public static ErrorResponse build(BusinessException businessException) {
        return ErrorResponse.builder()
                .code(businessException.getHttpStatus().getReasonPhrase())
                .message(businessException.getMessage())
                .build();
    }

}
