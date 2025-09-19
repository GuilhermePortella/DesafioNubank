# Code Challenge: Ganho de Capital

## Visão Geral
Este projeto calcula o imposto sobre operações de compra e venda de ações e imprime a saída em **CLI** no formato **JSON**, uma **linha por simulação** lida da `stdin`.

- **Entrada:** uma ou mais simulações; cada simulação é **uma lista JSON** de operações.
- **Saída:** para cada simulação, um **array JSON** com objetos `{ "tax": <valor> }`, um por operação.
- **Dependências:**
  - **JSON:** `jackson-databind 2.17.2`
  - **Testes:** `junit-jupiter 5.10.2`

## Requisitos
- **Java 21**
- **Maven 3.9+**

### Build
Em um terminal navegue até a raiz do projeto e digite:
```bash
mvn clean install
```
Isso irá baixar as dependências e criar o binário para a execução do código.

Para uma visualização mais detalhada do que está acontecendo:
```bash
mvn clean install -X
```

## Como Executar o Projeto
Após a criação do JAR na pasta `target`, siga estes passos:

Você pode executar o script que disponibilizei (`run.sh`) da seguinte forma:

```bash
./run.sh
```

### Conteúdo do script:
```bash
#!/usr/bin/env bash
java -jar ./target/nubankDesafioJava-1.0-SNAPSHOT-shaded.jar < ./resources/cases/inputCase1.txt
```

Para executar outro cenário de teste, basta alterar o número do arquivo de entrada:

```bash
java -jar ./target/nubankDesafioJava-1.0-SNAPSHOT-shaded.jar < ./resources/cases/inputCase2.txt
```

---

## Formato de Entrada/Saída

### Entrada (simulação)
Lista de operações, cada operação com `operation`, `unit-cost`, `quantity`:

```json
[
  {"operation":"buy",  "unit-cost":10.00, "quantity":100},
  {"operation":"sell", "unit-cost":15.00, "quantity":50}
]
```

#### São suportadas também múltiplas listas (uma por linha):
```json
[{"operation":"buy","unit-cost":10.00,"quantity":100},{"operation":"sell","unit-cost":15.00,"quantity":100}]
[{"operation":"buy","unit-cost":10.00,"quantity":100},{"operation":"sell","unit-cost":5.00,"quantity":100}]
```

Todos os 9 cenários de teste fornecidos estão em `resources/cases/`.

### Saída
Mesma cardinalidade da simulação de entrada:
```json
[{"tax":0.00},{"tax":0.00}]
```
Para múltiplos casos:
```json
[{"tax":0.00},{"tax":0.00}]
[{"tax":0.00},{"tax":0.00}]
```
Por padrão, a saída é **compacta em 1 linha**.  
Também é possível usar as flags:
- `--pretty` → saída identada
- `--wrap=N` → quebra a cada N itens

---

## Regras de Negócio
- **Compra:** isenta de impostos; apenas atualiza quantidade e preço médio.
- **Venda:**
  1. **Prejuízo:** imposto = **0** e acumula em `accLoss`.
  2. **Lucro** com **total ≤ 20.000**: isento (não consome `accLoss`).
  3. **Lucro** com **total > 20.000**: compensa com `accLoss` e aplica **20%** sobre o lucro.

![nuFluxo.PNG](resources/imagens/nuFluxo.PNG)

---

## Arquitetura e Estrutura de Pastas
Inspirada em **arquitetura hexagonal**:

```
src/main/java/
├─ br/nubank/adapters/
│  ├─ cli/
│  │  ├─ NubankDesafioJava.java   (main)
│  │  └─ CliApp.java              (camada de CLI)
│  └─ json/
│     ├─ JacksonJson.java
│     └─ dto/
│        ├─ OperationDTO.java
│        └─ TaxDTO.java
├─ br/nubank/application/
│  └─ CapitalGainsCalculator.java (cálculo puro/imutável)
└─ br/nubank/domain/
   ├─ Operation.java
   ├─ OperationType.java
   └─ TaxResult.java
```

Essa estrutura separa **domínio**, **aplicação** e **adapters**, facilitando manutenção, testes e evolução do projeto.

---

## Testes e Cobertura
O projeto inclui uma suíte de **testes unitários e de integração** cobrindo:
- Casos de borda (lucro zero, isenção ≤ 20k, exatamente 20k, consumo parcial/total de prejuízo, recompras).
- Parser JSON (arrays, NDJSON, entradas inválidas).
- Escrita JSON (`compacto`, `pretty`, `wrap`).
- CLI fim-a-fim (`CliAppIT`).

Para gerar o relatório de cobertura com **JaCoCo**:
```bash
mvn clean verify
```

O relatório estará em:
```
target/site/jacoco/index.html
```

> A cobertura foi configurada para ignorar classes de **bootstrap** (`NubankDesafioJava`), já testadas indiretamente pelo teste de integração do CLI.

---

## Agradecimentos
Agradeço pela oportunidade de participar do desafio. Foram dias de estudo e aprendizado, pude aplicar meus conhecimentos do dia a dia e aprender coisas novas.  
A experiência de implementar as regras de negócio com foco em simplicidade, clareza e testabilidade foi extremamente valiosa para mim.  
Estou ansioso para continuar me desenvolvendo e contribuir com a missão de descomplicar a vida dos clientes com as melhores soluções e tecnologias do mercado.
