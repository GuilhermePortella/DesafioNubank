package br.nubank.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record TaxResult(BigDecimal tax) {

    public TaxResult {

        if (tax == null) {
            tax = BigDecimal.ZERO;
        }
        
        tax = tax.setScale(2, RoundingMode.HALF_UP);
    }
}
