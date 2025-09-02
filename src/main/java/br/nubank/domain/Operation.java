package br.nubank.domain;

import java.math.BigDecimal;

public record Operation(OperationType type, BigDecimal unitCost, long quantity) {

}
