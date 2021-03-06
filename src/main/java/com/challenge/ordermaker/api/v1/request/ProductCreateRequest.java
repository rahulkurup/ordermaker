package com.challenge.ordermaker.api.v1.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@RequiredArgsConstructor
public class ProductCreateRequest {

    @NotBlank
    private final String name;

    @NotNull
    private final float price;
}