package br.nubank.application;

import br.nubank.domain.Operation;
import br.nubank.domain.OperationType;
import br.nubank.domain.Portfolio;
import br.nubank.domain.TaxResult;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public final class CapitalGainsCalculator {

    private static final BigDecimal TWENTY_K = new BigDecimal("20000.00");
    private static final BigDecimal RATE = new BigDecimal("0.20");

    public List<TaxResult> compute(List<Operation> ops) {
        var portfolio = new Portfolio();
        var out = new ArrayList<TaxResult>(ops.size());

        for (Operation op : ops) {
            if (op.type() == OperationType.COMPRAR) {
                applyBuy(portfolio, op);
                out.add(zeroTax());
            } else {
                out.add(applySell(portfolio, op));
            }
        }
        return out;
    }
    
    private static TaxResult zeroTax() {
        return new TaxResult(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
    }
    
    private static void applyBuy(Portfolio p, Operation op){
        
        long oldQty = p.getQuantity();
        long newQty = oldQty + op.quantity();
        
        BigDecimal oldCost = p.getAvgPrice()
                .multiply(BigDecimal.valueOf(oldQty))
                .setScale(2, RoundingMode.HALF_UP);
        
        BigDecimal newCost = op.unitCost()
                .multiply(BigDecimal.valueOf(op.quantity()))
                .setScale(2, RoundingMode.HALF_UP);
        
        BigDecimal newAvg = (oldQty == 0)
                ? op.unitCost().setScale(2, RoundingMode.HALF_UP):
                oldCost.add(newCost).divide(BigDecimal.valueOf(newQty), 
                2, RoundingMode.HALF_UP);
        
        p.setQuantity(newQty);
        p.setAvgPrice(newAvg);
    }
    
    private static TaxResult applySell(Portfolio p, Operation op) {
        
        BigDecimal unit = op.unitCost().setScale(2, RoundingMode.HALF_UP);
        long qty = op.quantity();
        
        BigDecimal total = unit.multiply(BigDecimal.valueOf(qty)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal avg = p.getAvgPrice();

        // Lucro/prejuízo bruto desta venda
        BigDecimal profit = unit.subtract(avg)
                .multiply(BigDecimal.valueOf(qty))
                .setScale(2, RoundingMode.HALF_UP);
        
        // Caso 1: prejuízo → acumula, reduz posição, imposto 0
        if (profit.signum() < 0) {
            p.addLoss(profit.abs());
            p.decreseQuantity(qty);
            return zeroTax();
        }
        
        // Caso 2: total da venda ≤ 20k → isenção e NÃO consome prejuízos
        if(total.compareTo(TWENTY_K) <= 0) {
            p.decreseQuantity(qty);
            return zeroTax();
        }

        // Caso 3: venda tributável (> 20k). Compensa prejuízos acumulados e aplica 20%
        BigDecimal taxable = p.consumeLoss(profit);
        BigDecimal tax = (taxable.signum() > 0)
                ? taxable.multiply(RATE).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
                
        p.decreseQuantity(qty);
        return new TaxResult(tax);
    }
    
}
