package ru.clevertec;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
public class Product {
    private UUID id;
    private String name;
    private Double price;
    private Map<UUID, BigDecimal> prices;
}