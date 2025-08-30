package br.nubank.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Portfolio {

    private long quantity;
    private BigDecimal avgPrice;
    private BigDecimal accLoss;

    public Portfolio() {
        this.quantity = 0L;
        this.avgPrice = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        this.accLoss = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    public long getQuantity() {
        return quantity;
    }

    public BigDecimal getAvgPrice() {
        return avgPrice;
    }

    public BigDecimal getAccLoss() {
        return accLoss;
    }

    public void setQuantity(long q) {
        this.quantity = q;
    }

    public void setAvgPrice(BigDecimal p) {
        this.avgPrice = p.setScale(2, RoundingMode.HALF_UP);
    }

    public void addLoss(BigDecimal loss) {
        if (loss.signum() > 0) {
            this.accLoss = this.accLoss.add(loss).setScale(2, RoundingMode.HALF_UP);
        }
    }

    public BigDecimal consumeLoss(BigDecimal profit) {

        if (profit.signum() <= 0) {
            return profit.setScale(2, RoundingMode.HALF_UP);
        }
        int cmp = profit.compareTo(accLoss);
        if (cmp <= 0) {
            this.accLoss = accLoss.subtract(profit).setScale(2, RoundingMode.HALF_UP);
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        } else {
            BigDecimal remaining = profit.subtract(accLoss).setScale(2, RoundingMode.HALF_UP);
            this.accLoss = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            return remaining;
        }
    }

    public void decreseQuantity(long qty) {
        this.quantity -= qty;
    }

}
