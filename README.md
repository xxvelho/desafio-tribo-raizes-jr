# Produtos API

API REST para criação, atualização, remoção e consulta de produtos, feita com Spring Boot e banco H2 em memória.

**Tecnologias:** Java 17, Spring Boot 3.5, Spring JDBC (`JdbcTemplate`), H2, Bean Validation, springdoc-openapi (Swagger), JUnit 5, Mockito e JaCoCo.

## Pré-requisitos

- JDK 17 ou superior
- Conexão com a internet na primeira execução (o Maven Wrapper baixa o Maven e as dependências)

Não é preciso instalar o Maven: o projeto traz o wrapper (`mvnw`).

## Como executar

```bash
./mvnw spring-boot:run
```

No Windows, use `mvnw.cmd spring-boot:run`.

A API sobe em `http://localhost:8080`. Os dados ficam em memória e são perdidos ao reiniciar.

| O quê | URL |
|-------|-----|
| API | http://localhost:8080/produtos |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Console do H2 | http://localhost:8080/h2-console |

Para entrar no console do H2: JDBC URL `jdbc:h2:mem:produtosdb`, usuário `sa` e senha em branco.

### Testes

```bash
./mvnw test      # executa todos os testes
./mvnw verify    # testes + relatório de cobertura em target/site/jacoco/index.html
```

## Endpoints

| Método | Rota | Sucesso | Erros |
|--------|------|---------|-------|
| POST | `/produtos` | 201 + header `Location` + produto criado | 400 |
| GET | `/produtos` | 200 (lista) | |
| GET | `/produtos/{id}` | 200 | 404 |
| PUT | `/produtos/{id}` | 200 | 400, 404 |
| DELETE | `/produtos/{id}` | 204 | 404 |

Validações do corpo (POST e PUT):

| Campo | Regra |
|-------|-------|
| `nome` | obrigatório, até 150 caracteres |
| `descricao` | opcional, até 500 caracteres |
| `preco` | obrigatório, maior que zero, no máximo `9999999999.99` e até 2 casas decimais (zeros à direita contam: `10.500` é recusado) |
| `quantidadeEstoque` | obrigatório, número inteiro (`10`, não `"10"` nem `10.5`), zero ou maior |

Todos os erros (400, 404, 405, 406, 415, 500...) devolvem o mesmo JSON, com `Content-Type: application/json`: `timestamp` (UTC), `status`, `mensagem` e, nas falhas de validação, `erros`. Cada item de `erros` é uma regra violada, ordenada por campo, então o mesmo campo pode aparecer mais de uma vez.

## Exemplos com curl

Criar um produto:

```bash
curl -i -X POST http://localhost:8080/produtos \
  -H "Content-Type: application/json" \
  -d '{"nome":"Teclado Mecânico","descricao":"Switch azul, ABNT2","preco":299.90,"quantidadeEstoque":15}'
```

```
HTTP/1.1 201
Location: http://localhost:8080/produtos/1

{"id":1,"nome":"Teclado Mecânico","descricao":"Switch azul, ABNT2","preco":299.90,"quantidadeEstoque":15}
```

Listar todos:

```bash
curl http://localhost:8080/produtos
```

```
[{"id":1,"nome":"Teclado Mecânico","descricao":"Switch azul, ABNT2","preco":299.90,"quantidadeEstoque":15}]
```

Buscar por ID:

```bash
curl http://localhost:8080/produtos/1
```

```
{"id":1,"nome":"Teclado Mecânico","descricao":"Switch azul, ABNT2","preco":299.90,"quantidadeEstoque":15}
```

Atualizar (o PUT substitui todos os campos):

```bash
curl -X PUT http://localhost:8080/produtos/1 \
  -H "Content-Type: application/json" \
  -d '{"nome":"Teclado Mecânico","descricao":"Switch marrom, ABNT2","preco":279.90,"quantidadeEstoque":12}'
```

```
{"id":1,"nome":"Teclado Mecânico","descricao":"Switch marrom, ABNT2","preco":279.90,"quantidadeEstoque":12}
```

Remover:

```bash
curl -i -X DELETE http://localhost:8080/produtos/1
```

```
HTTP/1.1 204
```

Produto inexistente (404):

```bash
curl -i http://localhost:8080/produtos/99
```

```
HTTP/1.1 404

{"timestamp":"2026-10-28T20:50:50.798573122Z","status":404,"mensagem":"Produto não encontrado com id 99"}
```

Dados inválidos (400):

```bash
curl -i -X POST http://localhost:8080/produtos \
  -H "Content-Type: application/json" \
  -d '{"nome":"","preco":-10,"quantidadeEstoque":-1}'
```

```
HTTP/1.1 400

{"timestamp":"2026-10-28T20:50:50.838754976Z","status":400,"mensagem":"Dados inválidos","erros":[{"campo":"nome","mensagem":"O nome é obrigatório"},{"campo":"preco","mensagem":"O preço deve ser maior que zero"},{"campo":"quantidadeEstoque","mensagem":"A quantidade em estoque não pode ser negativa"}]}
```

## Estrutura e padrões de projeto

```
com.exemplo.produtos
├── controller/   ProdutoController
├── service/      ProdutoService (interface) + ProdutoServiceImpl
├── dao/          ProdutoDAO (interface) + ProdutoDAOJdbc + DAOFactory
├── dto/          ProdutoRequestDTO, ProdutoResponseDTO, ErroResponseDTO
├── model/        Produto
├── mapper/       ProdutoMapper
├── exception/    ProdutoNaoEncontradoException + GlobalExceptionHandler
└── config/       OpenApiConfig
```

- **DAO:** `ProdutoDAO` define as operações de persistência (`salvar`, `atualizar`, `remover`, `buscarPorId`, `listarTodos`). Todo o SQL fica isolado em `ProdutoDAOJdbc`, que usa `JdbcTemplate`.
- **Factory:** `DAOFactory.criarProdutoDAO()` é quem instancia o DAO. O `ProdutoServiceImpl` depende da factory e da interface `ProdutoDAO`, nunca da implementação concreta. Trocar a forma de persistência exige mexer apenas na factory.
- **DTO:** o controller recebe `ProdutoRequestDTO` (com as validações) e devolve `ProdutoResponseDTO`. A entidade `Produto` não é exposta pela API. O `ProdutoMapper` faz a conversão.
- **Tratamento de erros:** o `GlobalExceptionHandler` (`@RestControllerAdvice`) devolve um JSON padronizado (`ErroResponseDTO`) para 404 (produto inexistente), 400 (validação, JSON ou id inválido) e para os demais erros do Spring MVC (405, 406, 415...). Erros inesperados viram 500 sem expor detalhes ao cliente, e o detalhe vai para o log. A classe estende `ResponseEntityExceptionHandler`.

O schema da tabela está em `src/main/resources/schema.sql` e é executado na inicialização.

## Testes

| Classe | Tipo | O que cobre |
|--------|------|-------------|
| `ProdutoRequestDTOTest` | unitário (Bean Validation) | cada regra de validação do DTO, incluindo os limites (150/151 caracteres, 2 casas decimais...) |
| `ProdutoServiceImplTest` | unitário (Mockito) | criar, atualizar, remover, buscar por ID e listar, incluindo os erros de ID inexistente e a escala do preço |
| `ProdutoDAOJdbcTest` | `@JdbcTest` com H2 real | SQL de cada operação do DAO |
| `DAOFactoryTest` | `@JdbcTest` com H2 real | a factory devolve um `ProdutoDAOJdbc` que funciona com o `JdbcTemplate` recebido |
| `ProdutoControllerTest` | `@WebMvcTest` + MockMvc | status HTTP, header `Location`, validações (400), 404, 405, 406, 415 e 500 no JSON padrão, inclusive quando o `Accept` não é JSON |
| `ProdutosApiApplicationTests` | `@SpringBootTest` | a aplicação sobe com o contexto completo |

O JaCoCo mede cerca de 97% de cobertura de linhas. As linhas sem teste são o `main` da aplicação e duas mensagens do `GlobalExceptionHandler` (400 genérico e status não previstos) que a API não gera hoje.

No JDK 21, o Mockito imprime o aviso `A Java agent has been loaded dynamically`. É apenas um aviso e não afeta os testes.
