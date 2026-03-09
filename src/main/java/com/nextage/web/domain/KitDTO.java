package com.nextage.web.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class KitDTO {

    private Long kitId;
    private String name;
    private String manufacturer;
    private int price;
    private int stock;

    private String mainImage1;
    private String mainImage2;
    private String detailImage;

    private boolean isVisible;
    private LocalDateTime createdAt;
}