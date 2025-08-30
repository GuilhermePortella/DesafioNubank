package br.nubank.application;

import br.nubank.domain.Operation;
import br.nubank.domain.OperationType;
import br.nubank.domain.TaxResult;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CapitalGainsCalculatorTest {

    private static Operation buy(double unit, long qty) {
        return new Operation(OperationType.BUY, bd(unit), qty);
    }

    private static Operation sell(double unit, long qty) {
        return new Operation(OperationType.SELL, bd(unit), qty);
    }

    private static BigDecimal bd(double v) {
        return new BigDecimal(String.format(java.util.Locale.US, "%.2f", v));
    }

    private static void assertTax(TaxResult r, double expected) {
        assertEquals(0, r.tax().compareTo(bd(expected)),
                "Tax diff: expected " + expected + " but was " + r.tax());
        assertTrue(r.tax().compareTo(BigDecimal.ZERO) >= 0, "Tax must be non-negative");
    }

    @Test
    void mediaPonderada_eVendaTributavel_maiorQue20k() {
        var ops = List.of(
                buy(10.00, 1000),
                buy(20.00, 1000), // avg = 15.00
                sell(25.00, 1000) // total=25,000; lucro=10,000; tax=2,000
        );
        var out = new CapitalGainsCalculator().compute(ops);
        assertTax(out.get(0), 0.00);
        assertTax(out.get(1), 0.00);
        assertTax(out.get(2), 2000.00);
    }

    @Test
    void vendaComPrejuizo_acumulaLoss_eZeraImposto() {
        var ops = List.of(
                buy(10.00, 1000),
                sell(5.00, 500), // loss 2,500
                sell(30.00, 1000) // lucro 20,000 -> tributa (20,000-2,500)*20% = 3,500
        );
        var out = new CapitalGainsCalculator().compute(ops);
        assertTax(out.get(0), 0.00);
        assertTax(out.get(1), 0.00);
        assertTax(out.get(2), 3500.00);
    }

    @Test
    void vendaIsenta_naoConsomePrejuizo() {
        var ops = List.of(
                buy(10.00, 10000),
                sell(5.00, 1000), // loss 5,000
                sell(30.00, 666), // total=19,980 <= 20k (isento) e NÃO consome loss
                sell(30.00, 1000) // agora tributa: (30-10)*1000 - 5,000 = 15,000 -> tax=3,000
        );
        var out = new CapitalGainsCalculator().compute(ops);
        assertTax(out.get(0), 0.00);
        assertTax(out.get(1), 0.00);
        assertTax(out.get(2), 0.00);
        assertTax(out.get(3), 3000.00);
    }

    @Test
    void borda_totalExatamente_20000_isento() {
        var ops = List.of(
                buy(10.00, 5000),
                sell(5.00, 600), // loss 3,000
                sell(20.00, 1000), // total=20,000 -> isento (não consome loss)
                sell(30.00, 1000) // lucro 20,000 - 3,000 = 17,000 -> tax=3,400
        );
        var out = new CapitalGainsCalculator().compute(ops);
        assertTax(out.get(0), 0.00);
        assertTax(out.get(1), 0.00);
        assertTax(out.get(2), 0.00);
        assertTax(out.get(3), 3400.00);
    }

    @Test
    void arredondamento_mediaEImposto_duasCasas_HALF_UP() {
        var ops = List.of(
                buy(10.00, 1000),
                buy(10.99, 1000), // avg -> 10.495 -> 10.50
                sell(10.51, 2000) // lucro 0.01*2000=20 -> total=21,020 (>20k) -> tax=4.00
        );
        var out = new CapitalGainsCalculator().compute(ops);
        assertTax(out.get(0), 0.00);
        assertTax(out.get(1), 0.00);
        assertTax(out.get(2), 4.00);
    }

    @Test
    void impostoNuncaNegativo_mesmoComLossSobrando() {
        var ops = List.of(
                buy(10.00, 10000),
                sell(2.00, 5000), // loss 40,000
                sell(30.00, 1000) // lucro 20,000 -> compensado por loss -> tax=0
        );
        var out = new CapitalGainsCalculator().compute(ops);
        assertTax(out.get(0), 0.00);
        assertTax(out.get(1), 0.00);
        assertTax(out.get(2), 0.00);
        out.forEach(r -> assertTrue(r.tax().compareTo(BigDecimal.ZERO) >= 0));
    }
}
