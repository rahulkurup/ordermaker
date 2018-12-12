package com.challenge.ordermaker.api.v1.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@RequiredArgsConstructor
public class OrderCreateRequest {

    @NotBlank
    private final String buyerEmailId;

    @NotEmpty
    @NotNull
    private final Set<Long> productSet;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private final LocalDateTime orderTime;
}
