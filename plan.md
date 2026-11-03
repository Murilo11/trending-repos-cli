---
name: Completar o CLI
overview: "Plano em etapas para você mesmo completar o que falta no trending-repos-cli: headers da API, saída formatada, erros com códigos de saída, fat jar e README. Sem código pronto — só o que fazer, onde e o que decidir."
todos:
  - id: headers
    content: Corrigir Accept e User-Agent no GithubClient; validar chamada 200
    status: pending
  - id: output
    content: Formatar saída (nulos + truncamento) e remover println provisório
    status: pending
  - id: errors
    content: Exceção própria, 403/429/422/5xx/rede/timeout/lista vazia, códigos de saída no call()
    status: pending
  - id: shade
    content: maven-shade-plugin com Main-Class; validar java -jar
    status: pending
  - id: readme
    content: README com pré-requisitos, build, uso, exemplos e erros
    status: pending
isProject: false
---

# Plano para completar o `trending-repos-cli`

Ordem pensada para você validar cada bloco no terminal antes de passar ao próximo. Não copie implementação: use isto como roteiro.

Estado atual: CLI, `TimeRange.cutoffDate`, testes de data, `GithubClient` + Jackson já existem. Falta qualidade da chamada, saída, erros e entrega.

---

## 1. Corrigir a chamada HTTP

Arquivo: [src/main/java/dev/murilo/trending/GithubClient.java](src/main/java/dev/murilo/trending/GithubClient.java)

O que está errado: o header `User-Agent` está com o valor que deveria ser o `Accept`. A API do GitHub exige os dois, separados.

O que você faz:

- Definir `Accept` como o media type da API REST do GitHub.
- Definir `User-Agent` com um identificador do seu app (nome do projeto + contato ou GitHub user). Sem User-Agent a API recusa ou se comporta mal.
- Conferir se o timeout de connect e o timeout do request continuam razoáveis (hoje ambos são 10s).
- Decidir se o `HttpClient` deve ser campo da classe (reuso) ou criado a cada chamada (hoje é criado dentro do método).

Como validar: rode o CLI de verdade. Se a query e o encoding estiverem ok, a resposta 200 deve parsear. Se os headers ainda estiverem errados, você verá 403 ou corpo de erro.

---

## 2. Saída formatada (e tirar o debug)

Arquivos:

- [src/main/java/dev/murilo/trending/Repository.java](src/main/java/dev/murilo/trending/Repository.java)
- [src/main/java/dev/murilo/trending/TrendingCommand.java](src/main/java/dev/murilo/trending/TrendingCommand.java)

Hoje o `call()` imprime o `toString()` do record e ainda o `println` provisório (duração, limite, cutoff).

O que você decide:

- Layout de cada linha (ex.: nome, estrelas, linguagem, descrição, URL). Uma linha por campo vs. um bloco por repo.
- Texto para `description` nula e `language` nula (ex.: “sem descrição”, “N/A”).
- Truncamento: comprimento máximo da descrição e o que fazer se passar (reticências). Cuidado com `null` antes de medir o tamanho.

Onde colocar a formatação: método no `Repository` (ex. `format()`) ou classe/`Printer` à parte. Evite deixar a regra de truncar espalhada no `call()`.

No `call()`: só listar os repositórios formatados. Remova o `println` de debug.

Como validar: rode com `--limit 3` e procure um repo sem linguagem/descrição (comum). Confira se a descrição longa não estoura o terminal.

---

## 3. Erros da API e códigos de saída

Arquivos novos + [GithubClient.java](src/main/java/dev/murilo/trending/GithubClient.java) e [TrendingCommand.java](src/main/java/dev/murilo/trending/TrendingCommand.java).

Hoje qualquer status ≠ 200 vira `IOException`. O `call()` não trata. Lista vazia não é considerada.

Fluxo alvo:

```mermaid
flowchart TD
  call[TrendingCommand.call]
  search[GithubClient.searchTrending]
  ok[HTTP 200]
  empty[items vazio]
  rate[403 ou 429]
  unprocessable[422]
  server[5xx]
  net[IOException ou timeout]
  print[Imprime formatado]
  err[Imprime mensagem no stderr]
  exit0[return 0]
  exitN[return codigo diferente de 0]
  call --> search
  search --> ok
  search --> rate
  search --> unprocessable
  search --> server
  search --> net
  ok --> empty
  ok --> print
  print --> exit0
  empty --> err
  rate --> err
  unprocessable --> err
  server --> err
  net --> err
  err --> exitN
```

O que você faz, nesta ordem:

1. Crie uma exceção própria (checked ou unchecked — escolha uma e use em todo o cliente). Ela deve carregar mensagem para o usuário e, se quiser, o código de saída.
2. No `GithubClient`, depois do `send`:
    - `200`: parsear. Se `items` for `null` ou vazio, trate como “nenhum repositório” (exceção ou retorno vazio — o `call()` precisa de uma mensagem clara).
    - `403` e `429`: leia o header `X-RateLimit-Reset` (epoch em segundos). Converta para horário legível. Mensagem: rate limit, quando volta.
    - `422`: query inválida (data/limit/encoding). Mensagem curta, sem dump de JSON.
    - `5xx`: GitHub indisponível; sugira tentar de novo.
    - Outros status: mensagem genérica com o código HTTP. Evite imprimir o corpo cru na versão final.
3. Envolva `client.send` para timeout (`HttpTimeoutException`) e falha de rede (`IOException`, `InterruptedException`). Traduza para a exceção própria, sem stacktrace no stdout.
4. No `call()`: `try/catch` da exceção própria, `System.err.println` da mensagem, `return` de código ≠ 0. Sucesso continua `return 0`.
5. Decida a tabela de códigos (ex.: 1 validação CLI já é do picocli; 2 rate limit; 3 rede/timeout; 4 API; 5 lista vazia). Documente no README o que você escolheu.

Validação: `--limit 0` já deve falhar na CLI. Para rate limit, não precisa forçar 403 de verdade: um teste unitário do mapeamento status → mensagem basta, se você extrair esse mapeamento para um método testável. Timeout/rede você valida mentalmente ou com URL inválida só em experimento local.

---

## 4. Fat jar

Arquivo: [pom.xml](pom.xml)

O que falta: plugin de shade (ou equivalent) que:

- Empacota as dependências (Jackson, picocli).
- Declara `Main-Class` apontando para `dev.murilo.trending.Main`.

Depois: `mvn package` e `java -jar target/...jar --help`. Se o jar “não encontra Main” ou “NoClassDefFoundError: picocli”, o plugin ou o manifesto está incompleto.

Observação: o [.gitignore](.gitignore) ignora `*.jar`. O artefato gerado em `target/` já está coberto por `target/`; não commite o jar.

---

## 5. README de verdade

Arquivo: [README.md](README.md) (hoje só o título)

Seções mínimas:

- O que o programa faz (lista repos criados no período, ordenados por estrelas).
- Pré-requisitos: JDK 17+ e Maven.
- Build: `mvn test`, `mvn package`.
- Uso: `java -jar ... --help`, opções `-d`/`--duration` e `-l`/`--limit`, valores padrão (`WEEK`, `10`).
- Exemplos: um comando curto e um com `-d MONTH -l 5`.
- Erros: rate limit, timeout, lista vazia, e os códigos de saída que você definiu.
- Limitação: Search API do GitHub (não é a página “Trending” do site); sem token o limite anônimo é baixo.

Não cole código-fonte no README.

---

## 6. Fechamento (checklist de aceite)

Antes de considerar pronto:

- `--help` e `--version` ok.
- `DAY`/`WEEK`/`MONTH`/`YEAR` (case-insensitive) ok.
- Limit 1–100; fora disso mensagem do picocli, não crash.
- Saída legível, nulos tratados, descrição truncada.
- Sem `println` de debug.
- `mvn test` verde (`TimeRangeTest` intacto).
- `java -jar` funciona.
- README descreve build, uso e erros.

Opcional: submeter no roadmap.sh. Não entra no código.

---

## O que não mexer

- `Main` (já tem um `execute` + `System.exit` + enum case-insensitive).
- Validação de `limit` com `@Spec`.
- `TimeRange` e `TimeRangeTest` (já cobrem 31/03 e bissexto).
- Dependências Jackson / picocli / JUnit / Surefire, salvo o shade.

---

## Ordem de branches (se continuar o fluxo por PR)

1. `fix/github-headers`
2. `feat/formatted-output` (já remove o println)
3. `feat/api-errors`
4. `chore/shade-and-readme`

Cada PR deve rodar no terminal e, se possível, `mvn test`.
