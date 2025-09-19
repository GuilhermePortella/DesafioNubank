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
                buy(20.00, 1000),
                sell(25.00, 1000));
        var out = new CapitalGainsCalculator().compute(ops);
        assertTax(out.get(0), 0.00);
        assertTax(out.get(1), 0.00);
        assertTax(out.get(2), 2000.00);
    }

    @Test
    void vendaComPrejuizo_acumulaLoss_eZeraImposto() {
        var ops = List.of(
                buy(10.00, 10000),
                sell(5.00, 5000),
                sell(12.00, 5000)
        );
        var out = new CapitalGainsCalculator().compute(ops);
        assertTax(out.get(0), 0.00);
        assertTax(out.get(1), 0.00);
        assertTax(out.get(2), 0.00);
    }

    @Test
    void vendaIsenta_naoConsomePrejuizo() {
        var ops = List.of(
                buy(10.00, 10000),
                sell(5.00, 1000),
                sell(30.00, 666),
                sell(30.00, 1000));
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
                sell(5.00, 600),
                sell(20.00, 1000),
                sell(30.00, 1000));
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
                buy(10.99, 1000),
                sell(10.51, 2000));
        var out = new CapitalGainsCalculator().compute(ops);
        assertTax(out.get(0), 0.00);
        assertTax(out.get(1), 0.00);
        assertTax(out.get(2), 4.00);
    }

    @Test
    void impostoNuncaNegativo_mesmoComLossSobrando() {
        var ops = List.of(
                buy(10.00, 10000),
                sell(2.00, 5000),
                sell(30.00, 1000));
        var out = new CapitalGainsCalculator().compute(ops);
        assertTax(out.get(0), 0.00);
        assertTax(out.get(1), 0.00);
        assertTax(out.get(2), 0.00);
        out.forEach(r -> assertTrue(r.tax().compareTo(BigDecimal.ZERO) >= 0));
    }

    @Test
    void lucroZero_totalAcimaDe20k_isento() {
        var ops = List.of(
                buy(10.00, 3000),
                sell(10.00, 3000));
        var out = new CapitalGainsCalculator().compute(ops);
        assertTax(out.get(0), 0.00);
        assertTax(out.get(1), 0.00);
    }

    @Test
    void consumoParcialDePerdas_primeiraTributavelZeraTax_segundaTributavelPaga() {
        var ops = List.of(
                buy(10.00, 10000),
                sell(5.00, 5000), // Loss of 25000
                sell(14.00, 2000), // Profit of 8000, consumed by loss -> tax 0. Remaining loss 17000
                sell(20.00, 2000)  // Profit of 20000, taxable gain 3000 -> tax 600
        );
        var out = new CapitalGainsCalculator().compute(ops);
        assertTax(out.get(0), 0.00);
        assertTax(out.get(1), 0.00);
        assertTax(out.get(2), 0.00);
        assertTax(out.get(3), 600.00);
    }

    @Test
    void vendaTributavel_totalApenasAcimaDe20k() {
        var ops = List.of(
                buy(10.00, 1000),
                sell(30.00, 667));
        var out = new CapitalGainsCalculator().compute(ops);
        assertTax(out.get(0), 0.00);
        assertTax(out.get(1), 2668.00);
    }

    @Test
    void resetDeMedia_aposZerarPosicao() {
        var ops = List.of(
                buy(10.00, 100),
                sell(10.00, 100),
                buy(30.00, 1000),
                sell(50.00, 1000));
        var out = new CapitalGainsCalculator().compute(ops);
        assertTax(out.get(0), 0.00);
        assertTax(out.get(1), 0.00);
        assertTax(out.get(2), 0.00);
        assertTax(out.get(3), 4000.00);
    }

    @Test
    void consumoTotalDePerdas_emUmaUnicaTributavel() {
        var ops = List.of(
                buy(10.00, 1000),
                sell(7.00, 1000),
                buy(10.00, 2000),
                sell(13.00, 2000));
        var out = new CapitalGainsCalculator().compute(ops);
        assertTax(out.get(0), 0.00);
        assertTax(out.get(1), 0.00);
        assertTax(out.get(2), 0.00);
        assertTax(out.get(3), 600.00);
    }

}
