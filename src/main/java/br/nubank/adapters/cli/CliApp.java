package br.nubank.adapters.cli;

import java.io.InputStream;
import java.io.PrintStream;
import br.nubank.adapters.json.JacksonJson;
import br.nubank.application.CapitalGainsCalculator;

final class CliApp {

    static void run(String[] args, InputStream in, PrintStream out) throws Exception {
        boolean pretty = false;
        int wrapEvery = 0;

        for (String a : args) {
            if ("--pretty".equalsIgnoreCase(a)) {
                pretty = true;
            } else if (a.startsWith("--wrap=")) {
                try {
                    wrapEvery = Integer.parseInt(a.substring("--wrap=".length()));
                } catch (NumberFormatException ignored) {
                }
            }
        }

        var calc = new CapitalGainsCalculator();

        for (var inCase : JacksonJson.readAllCases(in)) {
            var domainOps = JacksonJson.toDomain(inCase);
            var results = calc.compute(domainOps);
            var taxesDto = JacksonJson.toDto(results);

            String json = JacksonJson.writeJson(taxesDto, pretty, wrapEvery);

            out.println(json);
        }
    }

    private CliApp() {
    }
}
