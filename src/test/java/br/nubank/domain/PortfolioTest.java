package br.nubank.domain;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

public class PortfolioTest {

    private static BigDecimal bd(double v) {
        return new BigDecimal(String.format(java.util.Locale.US, "%.2f", v));
    }

    @Test
    void consumeLoss_naoGeraNegativo_quandoLucroMenorQueLoss() {
        var p = new Portfolio();
        p.addLoss(bd(1000.00));
        var rem = p.consumeLoss(bd(200.00));
        assertEquals(0, rem.compareTo(bd(0.00)));
        // accLoss reduziu para 800
        assertEquals(0, p.getAccLoss().compareTo(bd(800.00)));
    }

    @Test
    void consumeLoss_zeraLoss_quandoLucroMaiorQueLoss() {
        var p = new Portfolio();
        p.addLoss(bd(500.00));
        var rem = p.consumeLoss(bd(1200.00));
        assertEquals(0, rem.compareTo(bd(700.00)));
        assertEquals(0, p.getAccLoss().compareTo(bd(0.00)));
    }
}
