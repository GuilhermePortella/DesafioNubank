package br.nubank.adapters.cli;

import br.nubank.adapters.json.JacksonJson;
import br.nubank.adapters.json.dto.OperationDTO;
import br.nubank.application.CapitalGainsCalculator;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

public class NubankDesafioJava {

    public static void main(String[] args) throws Exception {
        var calc = new CapitalGainsCalculator();

        // Lê TODO o stdin como um único array JSON (suporta arquivos “multi-linha”)
        try (var reader = new InputStreamReader(System.in)) {
            List<OperationDTO> in = JacksonJson.readAll(reader);
            var domainOps = JacksonJson.toDomain(in);
            var taxes = calc.compute(domainOps);
            var out = JacksonJson.toDto(taxes);
            System.out.println(JacksonJson.writeLine(out));
        }
    }
}
