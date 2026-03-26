package com.nirvanafire.ocadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 余额DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceDTO {
    private Long userId;
    private Integer year;
    private BigDecimal total;
    private BigDecimal used;
    private BigDecimal available;
}
