package br.nubank.domain;

import java.math.BigDecimal;
import java.util.Objects;

public record Operation(OperationType type, BigDecimal unitCost, long quantity) {
    
//    public Operation{
//        
//        Objects.requireNonNull(type, "tipo");
//        Objects.requireNonNull(unitCost, "custoUnitario");
//        
//        //remover essa verificação pois o doc diz que nao tera entradas erradas
//        if (quantity <= 0) throw new IllegalArgumentException("quantity must be > 0");
//        
//        
//    }
   
}
 