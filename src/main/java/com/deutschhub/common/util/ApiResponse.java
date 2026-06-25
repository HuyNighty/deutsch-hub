package com.deutschhub.common.util;

import com.deutschhub.common.exception.ErrorCodeDetail;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    @Builder.Default
    int code = 200;

    String message;

    T result;

    List<ErrorCodeDetail> errors;
}
