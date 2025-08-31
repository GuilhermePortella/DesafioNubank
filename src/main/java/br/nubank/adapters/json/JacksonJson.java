package br.nubank.adapters.json;

import br.nubank.adapters.json.dto.OperationDTO;
import br.nubank.adapters.json.dto.TaxDTO;
import br.nubank.domain.Operation;
import br.nubank.domain.OperationType;
import br.nubank.domain.TaxResult;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class JacksonJson {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static List<List<OperationDTO>> readAllCases(InputStream in) throws Exception {
        List<List<OperationDTO>> cases = new ArrayList<>();
        List<OperationDTO> ndjsonBuffer = null;

        try (JsonParser p = MAPPER.getFactory().createParser(in)) {
            for (JsonToken t = p.nextToken(); t != null; t = p.nextToken()) {
                if (t == JsonToken.START_ARRAY) {

                    if (ndjsonBuffer != null && !ndjsonBuffer.isEmpty()) {
                        cases.add(ndjsonBuffer);
                        ndjsonBuffer = null;
                    }
                    List<OperationDTO> arr = MAPPER.readValue(p, new TypeReference<List<OperationDTO>>() {
                    });
                    cases.add(arr);
                } else if (t == JsonToken.START_OBJECT) {
                    if (ndjsonBuffer == null) {
                        ndjsonBuffer = new ArrayList<>();
                    }
                    OperationDTO dto = MAPPER.readValue(p, OperationDTO.class);
                    ndjsonBuffer.add(dto);
                } else {
                    p.skipChildren();
                }
            }
        }

        if (ndjsonBuffer != null && !ndjsonBuffer.isEmpty()) {
            cases.add(ndjsonBuffer);
        }

        return cases;
    }

    private static OperationType parseOperationType(String raw) {
        String s = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
        switch (s) {
            case "buy":
            case "comprar":
                return OperationType.BUY;
            case "sell":
            case "vender":
                return OperationType.SELL;
            default:
                throw new IllegalArgumentException(
                        "Campo 'operation' inválido: '" + raw + "'. Aceitos: buy/sell.");
        }
    }

    public static String writeLine(List<TaxDTO> taxes) throws Exception {
        return MAPPER.writeValueAsString(taxes);
    }

    public static String writePretty(List<TaxDTO> taxes) throws Exception {
        return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(taxes);
    }

    public static String writeJson(List<TaxDTO> taxes, boolean pretty, int wrapEvery) throws Exception {
        if (wrapEvery > 0) {
            // Mantém JSON válido e insere quebras visuais a cada N itens
            return writeWrapped(taxes, wrapEvery);
        }
        // Compacto (default) ou pretty, usando o ObjectMapper
        return pretty
                ? MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(taxes)
                : MAPPER.writeValueAsString(taxes);
    }

    private static String writeWrapped(List<TaxDTO> taxes, int every) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < taxes.size(); i++) {
            sb.append(MAPPER.writeValueAsString(taxes.get(i)));
            if (i < taxes.size() - 1) {
                sb.append(',');
                if ((i + 1) % every == 0) {
                    sb.append('\n');
                } else {
                    sb.append(' ');
                }
            }
        }
        sb.append(']');
        return sb.toString();
    }

    public static List<Operation> toDomain(List<OperationDTO> in) {
        var out = new ArrayList<Operation>(in.size());
        for (int i = 0; i < in.size(); i++) {
            var d = in.get(i);
            try {
                out.add(new Operation(
                        parseOperationType(d.operation()),
                        BigDecimal.valueOf(d.unitCost()).setScale(2, RoundingMode.HALF_UP),
                        d.quantity()
                ));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(e);
            }
        }
        return out;
    }

    public static List<TaxDTO> toDto(List<TaxResult> results) {
        return results.stream()
                .map(r -> new TaxDTO(r.tax().doubleValue()))
                .toList();
    }

    private JacksonJson() {
    }
}
