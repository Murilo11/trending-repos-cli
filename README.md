# trending-repos-cli

CLI que consulta a API de busca do GitHub e lista os repositórios criados em um período, ordenados por número de estrelas (do maior para o menor).

Cada resultado exibe estrelas, nome completo, linguagem, descrição (truncada em 100 caracteres) e URL. Campos ausentes são tratados como `N/A` ou `Sem descrição`.

## Pré-requisitos

- JDK 17 ou superior
- Maven 3.8+

## Build

```bash
mvn test
mvn package
```

O `mvn package` gera o fat jar em `target/trending-repos.jar` (com Jackson e picocli empacotados).

## Uso

```bash
java -jar target/trending-repos.jar --help
java -jar target/trending-repos.jar --version
```

Opções:

| Opção | Descrição | Valores | Padrão |
| --- | --- | --- | --- |
| `-d`, `--duration` | Período considerado a partir da data atual | `DAY`, `WEEK`, `MONTH`, `YEAR` (case-insensitive) | `WEEK` |
| `-l`, `--limit` | Quantidade de repositórios exibidos | `1` a `100` | `10` |

## Exemplos

Últimos 10 repositórios da semana:

```bash
java -jar target/trending-repos.jar
```

Repositórios criados no último mês, mostrando 5:

```bash
java -jar target/trending-repos.jar -d MONTH -l 5
```

## Erros e códigos de saída

As mensagens de erro são enviadas para o `stderr`, sem stacktrace.

| Código | Situação |
| --- | --- |
| `0` | Sucesso |
| `2` | Argumentos inválidos (picocli) ou limite de requisições da API excedido (HTTP 403/429) |
| `3` | Falha de rede ou timeout na chamada ao GitHub |
| `4` | Erro da API do GitHub (HTTP 422, 5xx ou outro status inesperado) ou falha ao processar a resposta |
| `5` | Nenhum repositório encontrado para os critérios informados |

Em caso de rate limit, a mensagem informa o horário de renovação do limite quando o header `X-RateLimit-Reset` está disponível.

## Limitação

Este projeto usa a **Search API** do GitHub (`/search/repositories`), e não a página "Trending" do site — os resultados podem divergir dela. Sem um token de autenticação, o limite anônimo de requisições é baixo e pode ser atingido rapidamente.
