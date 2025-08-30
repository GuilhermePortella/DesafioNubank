package br.nubank.adapters.json;

import br.nubank.adapters.json.dto.OperationDTO;
import br.nubank.domain.Operation;
import br.nubank.domain.OperationType;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class JacksonJsonTest {

    @Test
    void toDomain_mapeiaCampos_eTipoOperacao() {
        var dtos = List.of(
                new OperationDTO("buy", 10.0, 100),
                new OperationDTO("sell", 15.0, 50)
        );
        List<Operation> ops = JacksonJson.toDomain(dtos);

        assertEquals(OperationType.BUY, ops.get(0).type());
        assertEquals(OperationType.SELL, ops.get(1).type());

        // compara valor (ignora scale)
        assertEquals(0, ops.get(0).unitCost().compareTo(new BigDecimal("10.00")));
        assertEquals(0, ops.get(1).unitCost().compareTo(new BigDecimal("15.00")));

        assertEquals(100, ops.get(0).quantity());
        assertEquals(50, ops.get(1).quantity());

    }
}
