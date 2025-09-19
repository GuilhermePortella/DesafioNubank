package br.nubank.application;

import br.nubank.domain.Operation;
import br.nubank.domain.OperationType;
import br.nubank.domain.TaxResult;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public final class CapitalGainsCalculator {

    private static final BigDecimal TWENTY_K = new BigDecimal("20000.00");
    private static final BigDecimal RATE = new BigDecimal("0.20");

    private static record State(long qty, BigDecimal avgPrice, BigDecimal accLoss) {
        State {
            if (avgPrice == null) avgPrice = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            if (accLoss == null)  accLoss  = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        static State empty() {
            return new State(0L,
                    BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                    BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        }
    }

    private static record SellResult(State next, TaxResult tax) {}

    public List<TaxResult> compute(List<Operation> ops) {
        var out = new ArrayList<TaxResult>(ops.size());
        State s = State.empty();

        for (Operation op : ops) {
            if (op.type() == OperationType.BUY) {
                s = applyBuy(s, op);
                out.add(zeroTax());
            } else {
                SellResult r = applySell(s, op);
                s = r.next();
                out.add(r.tax());
            }
        }
        return out;
    }

    private static TaxResult zeroTax() {
        return new TaxResult(BigDecimal.ZERO);
    }

    private static State applyBuy(State s, Operation op) {
        long oldQty = s.qty();
        long newQty = oldQty + op.quantity();

        BigDecimal oldCost = s.avgPrice().multiply(BigDecimal.valueOf(oldQty));
        BigDecimal newCost = op.unitCost().multiply(BigDecimal.valueOf(op.quantity()));

        BigDecimal newAvg = (oldQty == 0)
                ? op.unitCost().setScale(2, RoundingMode.HALF_UP)
                : oldCost.add(newCost).divide(BigDecimal.valueOf(newQty), 2, RoundingMode.HALF_UP);

        return new State(newQty, newAvg, s.accLoss());
    }

    private static SellResult applySell(State s, Operation op) {
        BigDecimal unit = op.unitCost(); 
        long qty = op.quantity();

        if (qty > s.qty()) {
            throw new IllegalArgumentException( "Operação inválida: venda de " + qty +
                    " ações, mas apenas " + s.qty() + " disponíveis."   );

        }

        BigDecimal total = unit.multiply(BigDecimal.valueOf(qty));
        BigDecimal avg   = s.avgPrice();

        BigDecimal profit = unit.subtract(avg).multiply(BigDecimal.valueOf(qty));

        if (profit.signum() < 0) {
            BigDecimal newAccLoss = s.accLoss().add(profit.abs()).setScale(2, RoundingMode.HALF_UP);
            State next = new State(s.qty() - qty, s.avgPrice(), newAccLoss);
            return new SellResult(next, zeroTax());
        }

        if (total.compareTo(TWENTY_K) <= 0) {
            State next = new State(s.qty() - qty, s.avgPrice(), s.accLoss());
            return new SellResult(next, zeroTax());
        }

        var consumed = consumeLoss(s.accLoss(), profit);
        BigDecimal taxable = consumed.remainingProfit();
        State afterConsume = new State(s.qty() - qty, s.avgPrice(), consumed.newAccLoss());

        BigDecimal tax = (taxable.signum() > 0) ? taxable.multiply(RATE) : BigDecimal.ZERO;
        return new SellResult(afterConsume, new TaxResult(tax));
    }

    private static record LossConsumption(BigDecimal newAccLoss, BigDecimal remainingProfit) {}

    private static LossConsumption consumeLoss(BigDecimal accLoss, BigDecimal profit) {
        if (profit.signum() <= 0) {
            return new LossConsumption(accLoss, profit);
        }
        int cmp = profit.compareTo(accLoss);
        if (cmp <= 0) {
            BigDecimal newAcc = accLoss.subtract(profit).setScale(2, RoundingMode.HALF_UP);
            return new LossConsumption(newAcc, BigDecimal.ZERO);
        } else {
            BigDecimal remaining = profit.subtract(accLoss);
            return new LossConsumption(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), remaining);
        }
    }
}