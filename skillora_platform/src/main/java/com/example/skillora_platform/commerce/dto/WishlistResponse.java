package com.example.skillora_platform.commerce.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class WishlistResponse {

    private int itemCount;
    private List<WishlistItemResponse> items;
}
