package br.nubank.adapters.json.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OperationDTO(
        @JsonProperty("operation")
        String operation,
        @JsonProperty("unit-cost")
        double unitCost,
        @JsonProperty("quantity")
        long quantity) {

}
