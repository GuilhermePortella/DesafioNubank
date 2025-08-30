package br.nubank.adapters.json;

import br.nubank.adapters.json.dto.OperationDTO;
import br.nubank.adapters.json.dto.TaxDTO;
import br.nubank.domain.Operation;
import br.nubank.domain.OperationType;
import br.nubank.domain.TaxResult;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import java.io.InputStream;
import java.io.PushbackReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public final class JacksonJson {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  /** Compatível com formato "1 linha = 1 caso". */
  public static List<OperationDTO> readLine(String jsonLine) throws Exception {
    return MAPPER.readValue(jsonLine, new TypeReference<>() {});
  }

  /** Lê TODO o reader como um único array JSON (multi-linha). */
  public static List<OperationDTO> readAll(Reader reader) throws Exception {
    return MAPPER.readValue(reader, new TypeReference<>() {});
  }

  /**
   * NOVO (recomendado): consome N "casos" a partir de um InputStream.
   * - [ ... ]\n[ ... ] => 2 casos (listas)
   * - [ ... ]          => 1 caso
   * - { ... }\n{ ... } => 1 caso (agrega todos os objetos em uma lista)
   *
   * Usar InputStream permite ao Jackson tratar BOM UTF-8 corretamente.
   */
  public static List<List<OperationDTO>> readAllCases(InputStream in) throws Exception {
    List<List<OperationDTO>> cases = new ArrayList<>();
    List<OperationDTO> ndjsonBuffer = null;

    try (JsonParser p = MAPPER.getFactory().createParser(in)) {
      for (JsonToken t = p.nextToken(); t != null; t = p.nextToken()) {
        if (t == JsonToken.START_ARRAY) {
          // fecha e adiciona buffer NDJSON pendente antes de um novo array
          if (ndjsonBuffer != null && !ndjsonBuffer.isEmpty()) {
            cases.add(ndjsonBuffer);
            ndjsonBuffer = null;
          }
          // consome o array inteiro como List<OperationDTO>
          List<OperationDTO> arr = MAPPER.readValue(p, new TypeReference<List<OperationDTO>>() {});
          cases.add(arr);
        } else if (t == JsonToken.START_OBJECT) {
          // modo NDJSON: agrega todos os objetos em um único "caso"
          if (ndjsonBuffer == null) ndjsonBuffer = new ArrayList<>();
          OperationDTO dto = MAPPER.readValue(p, OperationDTO.class);
          ndjsonBuffer.add(dto);
          // o loop chamará nextToken() de novo no topo e continua
        } else {
          // ignora tokens inesperados e pula quaisquer estruturas aninhadas
          p.skipChildren();
        }
      }
    }

    // se houver objetos NDJSON acumulados, vira 1 caso
    if (ndjsonBuffer != null && !ndjsonBuffer.isEmpty()) {
      cases.add(ndjsonBuffer);
    }

    return cases;
  }

  /** Saída compacta (1 linha) — padrão compatível com avaliadores. */
  public static String writeLine(List<TaxDTO> taxes) throws Exception {
    return MAPPER.writeValueAsString(taxes);
  }

  /** Saída identada (opcional). */
  public static String writePretty(List<TaxDTO> taxes) throws Exception {
    return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(taxes);
  }

  /**
   * “Quebra visual”: insere \n a cada N itens mantendo JSON válido.
   * Ex.: --wrap=5 → nova linha após cada 5 elementos.
   */
  public static String writeWrapped(List<TaxDTO> taxes, int every) throws Exception {
    if (every <= 0) return writeLine(taxes);
    StringBuilder sb = new StringBuilder();
    sb.append('[');
    for (int i = 0; i < taxes.size(); i++) {
      sb.append(MAPPER.writeValueAsString(taxes.get(i)));
      if (i < taxes.size() - 1) {
        sb.append(',');
        if ((i + 1) % every == 0) sb.append('\n');
        else sb.append(' ');
      }
    }
    sb.append(']');
    return sb.toString();
  }

  /** Mapeia DTO → domínio. */
  public static List<Operation> toDomain(List<OperationDTO> in) {
    return in.stream().map(d ->
      new Operation(
        "buy".equalsIgnoreCase(d.operation()) ? OperationType.COMPRAR : OperationType.VENDER,
        BigDecimal.valueOf(d.unitCost()).setScale(2, RoundingMode.HALF_UP),
        d.quantity()
      )
    ).toList();
  }

  /** Mapeia domínio → DTO para saída JSON. */
  public static List<TaxDTO> toDto(List<TaxResult> results) {
    return results.stream()
      .map(r -> new TaxDTO(r.tax().doubleValue()))
      .toList();
  }

  private JacksonJson() {}
}