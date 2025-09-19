package br.nubank.adapters.cli;

import br.nubank.adapters.json.JacksonJson;
import br.nubank.adapters.json.dto.OperationDTO;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class NubankDesafioJavaTest {
    
    private static ByteArrayInputStream in(String s) {
        return new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void arraysEmDuasLinhas_geraDoisCasos() throws Exception {
        String input =
            "[{\"operation\":\"buy\",\"unit-cost\":10.00,\"quantity\":100},{\"operation\":\"sell\",\"unit-cost\":15.00,\"quantity\":50}]\n" +
            "[{\"operation\":\"buy\",\"unit-cost\":20.00,\"quantity\":200},{\"operation\":\"sell\",\"unit-cost\":25.00,\"quantity\":100}]";

        List<List<OperationDTO>> cases = JacksonJson.readAllCases(in(input));

        assertEquals(2, cases.size());

        assertEquals(2, cases.get(0).size());
        assertEquals("buy",  cases.get(0).get(0).operation());
        assertEquals(100,    cases.get(0).get(0).quantity());

        assertEquals(2, cases.get(1).size());
        assertEquals("sell", cases.get(1).get(1).operation());
        assertEquals(100,    cases.get(1).get(1).quantity());
    }

    @Test
    void ndjsonObjetosSequenciais_agrupaComoUmCaso() throws Exception {
        String input =
            "{\"operation\":\"buy\",\"unit-cost\":10.00,\"quantity\":100}\n" +
            "{\"operation\":\"buy\",\"unit-cost\":12.00,\"quantity\":100}\n" +
            "{\"operation\":\"sell\",\"unit-cost\":15.00,\"quantity\":200}\n";

        List<List<OperationDTO>> cases = JacksonJson.readAllCases(in(input));

        assertEquals(1, cases.size());
        assertEquals(3, cases.get(0).size());
        assertEquals("sell", cases.get(0).get(2).operation());
        assertEquals(200,    cases.get(0).get(2).quantity());
    }

    @Test
    void mixArrayDepoisNdjson_criaDoisCasos() throws Exception {
        String input =
            // Caso 1: um array
            "[{\"operation\":\"buy\",\"unit-cost\":10.00,\"quantity\":100},{\"operation\":\"sell\",\"unit-cost\":10.00,\"quantity\":100}]\n" +
            // Caso 2: NDJSON (três objetos)
            "{\"operation\":\"buy\",\"unit-cost\":20.00,\"quantity\":100}\n" +
            "{\"operation\":\"sell\",\"unit-cost\":25.00,\"quantity\": 50}\n" +
            "{\"operation\":\"sell\",\"unit-cost\":25.00,\"quantity\": 50}\n";

        List<List<OperationDTO>> cases = JacksonJson.readAllCases(in(input));

        assertEquals(2, cases.size());
        assertEquals(2, cases.get(0).size());
        assertEquals(3, cases.get(1).size());
        assertEquals("buy",  cases.get(1).get(0).operation());
        assertEquals("sell", cases.get(1).get(2).operation());
    }
    
}
