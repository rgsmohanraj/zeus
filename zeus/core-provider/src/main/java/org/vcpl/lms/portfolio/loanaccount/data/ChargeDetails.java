package org.vcpl.lms.portfolio.loanaccount.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChargeDetails {
    private String name;
    private BigDecimal paid;
    private BigDecimal waivedOff;
}
