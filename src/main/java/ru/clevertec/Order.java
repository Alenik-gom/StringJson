package ru.clevertec;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
public class Order {
    private UUID id;
    private List<Product> products;
    private OffsetDateTime createDate;
}