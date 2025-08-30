package br.nubank.adapters.json;

import br.nubank.adapters.json.dto.OperationDTO;
import br.nubank.adapters.json.dto.TaxDTO;
import br.nubank.domain.Operation;
import br.nubank.domain.OperationType;
import br.nubank.domain.TaxResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Reader;

import java.math.BigDecimal;
import java.util.List;

public final class JacksonJson {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  public static List<OperationDTO> readLine(String jsonLine) throws Exception {
    return MAPPER.readValue(jsonLine, new TypeReference<>() {});
  }

  public static List<OperationDTO> readAll(Reader reader) throws Exception {
    return MAPPER.readValue(reader, new TypeReference<>() {});
  }

  // Compacto (padrão)
  public static String writeLine(List<TaxDTO> taxes) throws Exception {
    return MAPPER.writeValueAsString(taxes);
  }

  // Pretty-print (opcional)
  public static String writePretty(List<TaxDTO> taxes) throws Exception {
    return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(taxes);
  }

  // “Wrap” simples: quebra linha após N itens (continua sendo JSON válido)
  public static String writeWrapped(List<TaxDTO> taxes, int every) throws Exception {
    if (every <= 0) return writeLine(taxes);
    StringBuilder sb = new StringBuilder();
    sb.append('[');
    for (int i = 0; i < taxes.size(); i++) {
      // reaproveita o mapper só para um item
      sb.append(MAPPER.writeValueAsString(taxes.get(i)));
      if (i < taxes.size() - 1) {
        sb.append(',');
        if ((i + 1) % every == 0) sb.append('\n'); // quebra visual
        else sb.append(' ');
      }
    }
    sb.append(']');
    return sb.toString();
  }

  public static List<Operation> toDomain(List<OperationDTO> in) {
    return in.stream().map(d ->
        new Operation(
            "buy".equalsIgnoreCase(d.operation()) ? OperationType.COMPRAR : OperationType.VENDER,
            BigDecimal.valueOf(d.unitCost()).setScale(2),
            d.quantity()
        )
    ).toList();
  }

  public static List<TaxDTO> toDto(List<TaxResult> results) {
    return results.stream().map(r -> new TaxDTO(r.tax().doubleValue())).toList();
  }
}
