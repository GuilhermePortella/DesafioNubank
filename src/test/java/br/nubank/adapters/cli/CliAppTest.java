package br.nubank.adapters.cli;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class CliAppTest {

    private static ByteArrayInputStream in(String s) {
        return new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void executaDoisCasos_eImprimeUmaLinhaPorCaso() throws Exception {
        String input =
            "[{\"operation\":\"buy\",\"unit-cost\":10.00,\"quantity\":1},{\"operation\":\"sell\",\"unit-cost\":10.00,\"quantity\":1}]\n" +
            "[{\"operation\":\"buy\",\"unit-cost\":100.00,\"quantity\":300},{\"operation\":\"sell\",\"unit-cost\":110.00,\"quantity\":300}]";

        var outBytes = new ByteArrayOutputStream();
        var out = new PrintStream(outBytes, true, StandardCharsets.UTF_8);

        CliApp.run(new String[] {}, in(input), out);

        String[] lines = outBytes.toString(StandardCharsets.UTF_8).trim().split("\\R");
        assertEquals(2, lines.length, "Deve imprimir uma linha por caso");

        assertEquals("[{\"tax\":0.00},{\"tax\":0.00}]", lines[0].trim());
        assertEquals("[{\"tax\":0.00},{\"tax\":600.00}]", lines[1].trim());
    }
}