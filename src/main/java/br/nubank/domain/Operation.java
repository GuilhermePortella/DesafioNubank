package br.nubank.domain;

import java.math.BigDecimal;
import java.util.Objects;

public record Operation(OperationType type, BigDecimal unitCost, long quantity) {
      
}
 