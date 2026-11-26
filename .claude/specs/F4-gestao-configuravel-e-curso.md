---
id: F4
titulo: Gestão configurável e curso por participante
status: em andamento        # rascunho | aprovada | em andamento | concluída
branch: feature/F4-gestao-configuravel-e-curso
criada: 2026-11-26
---

# F4 — Gestão configurável e curso por participante

## 1. Problema
O CASIS virou **DACOMP (Diretório Acadêmico de Computação)**, mudaram presidente e secretariado, e o diretório passou a atender dois cursos: Sistemas de Informação e Ciência de Dados e IA. Hoje o nome do centro acadêmico, a cidade e o curso estão fixos no template e no e-mail, e o curso não pode variar por participante. A cada troca de gestão é preciso editar código. A F4 resolve as duas coisas: dados da gestão em configuração e curso por linha da planilha.

## 2. Comportamento esperado
1. Os dados da gestão (nome, sigla, cidade, imagem de fundo e lista de cursos) ficam em propriedades `certificado.*`, com padrão no `application.properties` e sobrescrita por variável de ambiente no Render. Trocar de gestão = trocar a imagem e/ou as propriedades, sem mexer em Java nem HTML.
2. O PNG novo do DACOMP (mesmo nome, `bg_certificado_evento.png`) passa a ser o fundo. Conferido pelo desenvolvedor em 2026-11-26: o layout atual do HTML serve.
3. O CSV aceita uma coluna opcional **`Curso`**. Valores aceitos (sem diferenciar maiúsculas nem acentos): `SI` ou `Sistemas de Informação`; `CDIA` ou `Ciência de Dados e Inteligência Artificial`. Valor fora da lista = linha `ERRO`.
4. O formulário do lote ganha o campo obrigatório **Curso padrão** (SI / CDIA, com SI pré-selecionado), usado nas células vazias e quando o CSV não tem a coluna (mesmo esquema das horas da F2). A prévia mostra a coluna **Curso**, com "(formulário)" quando veio do padrão.
5. A emissão unitária ganha um seletor de curso.
6. O certificado sai com o nome completo do curso e o nome da gestão configurado. Sem RA, o trecho "portador(a) do RA ..." some.
7. O e-mail usa o nome e a sigla da gestão no título, cabeçalho e rodapé. O 🐍 do assunto **fica**.
8. O frontend troca CASIS por DACOMP nos textos visíveis (fixo no código).

## 3. Critérios de aceitação
- [ ] CA1: Dado o `application.properties` padrão, quando a aplicação lê `certificado.*`, então obtém nome `Diretório Acadêmico de Computação`, sigla `DACOMP`, cidade `Londrina`, imagem `static/images/bg_certificado_evento.png` e os cursos `SI` e `CDIA`, **com acentos corretos** (teste de binding).
- [ ] CA2: Dada a gestão configurada, quando gero o PDF, então o HTML processado contém "promovida pelo Diretório Acadêmico de Computação (DACOMP)", "aluno(a) do curso de Bacharelado em Sistemas de Informação" (ou CDIA) e "Londrina, <data>". Nenhum "CASIS" ou "Centro Acadêmico de Sistemas de Informação" no HTML processado.
- [ ] CA3: Dado um participante sem RA (nulo ou em branco), quando gero o PDF, então o HTML processado **não** contém "portador(a) do RA" nem "N/A" e começa o parágrafo por "aluno(a) do curso de ...". Com RA, o trecho aparece como hoje.
- [ ] CA4: Dado um CSV com cabeçalho normalizado exatamente `curso`, quando leio as linhas, então `LinhaCsv.curso` traz o texto bruto da célula. Outros cabeçalhos ("Cursos", "Graduação") não são reconhecidos.
- [ ] CA5: Dados `SI`, `si`, ` Sistemas de Informação `, `sistemas de informacao`, `CDIA`, `Ciência de Dados e Inteligência Artificial`, `ciencia de dados e inteligencia artificial`, quando interpreto o curso, então obtenho `SI`, `SI`, `SI`, `SI`, `CDIA`, `CDIA`, `CDIA`.
- [ ] CA6: Dados `ADS`, `Sistemas`, `CD`, quando valido a linha, então ela fica `ERRO` com a mensagem `Curso inválido: <valor>`, sem PDF nem e-mail. Junto com outro erro (ex.: e-mail), as duas mensagens aparecem.
- [ ] CA7: Dada célula vazia ou CSV sem a coluna `Curso`, quando valido, então a linha usa o `cursoPadrao` do formulário com `cursoDoFormulario = true`, sem aviso.
- [ ] CA8: Dado `cursoPadrao` fora da lista, quando chamo `/pre-visualizar-lote`, `/emitir-lote` ou `/emitir-unitario`, então recebo 400 com "Curso inválido: <valor>". Dado `cursoPadrao` ausente (frontend antigo), então vale `SI` (comportamento atual).
- [ ] CA9: Dada uma linha válida, quando chamo `/pre-visualizar-lote`, então `LinhaValidadaDTO` traz `curso` (código), `cursoTexto` (nome completo) e `cursoDoFormulario`. Em linha `ERRO` por curso inválido, `curso` e `cursoTexto` são `null`.
- [ ] CA10: Dado um lote com cursos diferentes, quando emito, então cada PDF usa o curso da sua linha e `registrarEmissao` recebe o **nome curto** do curso (`Sistemas de Informação` / `Ciência de Dados e Inteligência Artificial`) (mocks).
- [ ] CA11: Dada a emissão unitária com `cursoParticipante = CDIA`, quando emito, então o PDF e o Sheets usam CDIA.
- [ ] CA12: O e-mail processado contém o nome e a sigla da gestão configurados e não contém "CASIS"; o assunto continua "Seu Certificado chegou! 🐍 - <evento>".
- [ ] CA13: No frontend: campo "Curso padrão" (lote) e "Curso" (unitário) como `<select>` SI/CDIA, enviados como `cursoPadrao` / `cursoParticipante`; coluna **Curso** na prévia ("—" quando `null`); ajuda explica a coluna `Curso`; títulos e rodapé dizem DACOMP / Diretório Acadêmico de Computação.
- [ ] CA14: Dado um CSV sem a coluna `Curso` e `cursoPadrao = SI`, quando emito, então o resultado só difere do atual pelos textos da gestão e pelo fundo novo.

## 4. Fora do escopo
- Datas de início e fim (F3); o texto "realizado no período de {data}" não muda.
- Tela de administração / upload de imagem pelo frontend (fica para depois da F5).
- Templates `certificado_gestao1/2.html` (vazios) e `TipoParticipacao`.
- Renomear itens internos: pacote `br.edu.utfpr.casis`, classes CSS `casis-*`, chave `casis_api_key`, propriedade `email.casis`.
- RA "N/A" na planilha do Sheets (continua igual; muda só no certificado).
- Sinônimos do cabeçalho (só `Curso`); cursos além de SI e CDIA (basta acrescentar na configuração).
- Anti-duplicação e pastas do Drive: não mudam.

## 5. Contrato
- **Rotas:** nenhuma nova.
  - `/pre-visualizar-lote` e `/emitir-lote` (multipart): campo novo opcional **`cursoPadrao`** (`SI` | `CDIA`; ausente = `SI`).
  - `/emitir-unitario` (JSON): campo novo opcional **`cursoParticipante`** (`SI` | `CDIA`; ausente = `SI`).
  - Código inválido → 400 `{ "Curso inválido: <valor>" }` pelo mesmo caminho de `DadosInvalidosException`.
- **DTOs / records:**
  - `EmissaoLoteEventoRequestDTO(nomeEvento, dataRealizacao, cargaHoraria, cursoPadrao, arquivoCsv)`
  - `EmissaoIndividualRequestDTO(..., raParticipante, cursoParticipante)`
  - `LinhaCsv(linha, nome, email, ra, horas, curso)`
  - `LinhaValidadaDTO`: + `String curso`, `String cursoTexto`, `boolean cursoDoFormulario`
  - Novo `GestaoProperties` (`@ConfigurationProperties("certificado")`): `nome`, `sigla`, `cidade`, `imagemFundo`, `Map<String, Curso> cursos` com `Curso(nomeCompleto, nomeCurto, List<String> apelidos)`.
- **Propriedades padrão** (`application.properties`, acentos como `\uXXXX`):
  ```
  certificado.nome=Diretório Acadêmico de Computação
  certificado.sigla=DACOMP
  certificado.cidade=Londrina
  certificado.imagem-fundo=static/images/bg_certificado_evento.png
  certificado.cursos.SI.nome-completo=Bacharelado em Sistemas de Informação
  certificado.cursos.SI.nome-curto=Sistemas de Informação
  certificado.cursos.SI.apelidos=SI,Sistemas de Informação
  certificado.cursos.CDIA.nome-completo=Bacharelado em Ciência de Dados e Inteligência Artificial
  certificado.cursos.CDIA.nome-curto=Ciência de Dados e Inteligência Artificial
  certificado.cursos.CDIA.apelidos=CDIA,Ciência de Dados e Inteligência Artificial
  ```
- **CSV:** coluna opcional `Curso`, ao lado de `Nome`, `E-mail`, `RA`, `Horas`.
- **Template:** variáveis novas `nomeGestao`, `siglaGestao`, `cursoNome`; `ra` passa a ser `null` quando ausente (condicional `th:if`); `dataEmissao` usa `cidade`.
- **Compatibilidade:** CSV antigo (sem `Curso`) continua aceito. Frontend antigo (sem `cursoPadrao`) continua funcionando com SI. Deploy: backend (Render) antes do frontend (Vercel).

## 6. Impacto no código
| Arquivo | Mudança |
|---|---|
| `config/GestaoProperties.java` (novo) + `@EnableConfigurationProperties` | binding de `certificado.*` |
| `application.properties` | padrões da gestão DACOMP |
| `service/InterpretadorCurso.java` (novo) | apelido → código, normalizando acento e caixa |
| `service/CsvService.java`, `LinhaCsv.java` | ler a coluna `Curso` |
| `service/ValidadorParticipantes.java`, `dto/LinhaValidadaDTO.java` | validar curso, padrão do formulário |
| `dto/EmissaoLoteEventoRequestDTO.java`, `dto/EmissaoIndividualRequestDTO.java` | `cursoPadrao` / `cursoParticipante` |
| `service/EmissaoService.java` | passar o curso ao PDF e ao Sheets |
| `service/PdfService.java`, `templates/certificado_evento.html` | gestão, curso, cidade, RA condicional, imagem configurável; remover CSS duplicado |
| `service/EmailService.java`, `templates/certificado-email.html` | nome/sigla da gestão |
| `static/images/bg_certificado_evento.png` | PNG do DACOMP (já no lugar, não commitado) |
| `frontend/src/App.jsx`, `CertificadoForm.jsx`, `PreVisualizacaoLote.jsx`, `AjudaEmissao.jsx`, `index.html` | selects de curso, coluna Curso, textos DACOMP |
| testes | `InterpretadorCursoTest`, `CsvServiceTest`, `ValidadorParticipantesTest`, `EmissaoServiceTest`, `PdfService` (HTML processado), controller, binding |

## 7. Riscos e regras críticas tocadas
- **Acentos em `.properties`:** o Spring Boot lê `.properties` como ISO-8859-1. Usar `\uXXXX` nos padrões e cobrir com o teste do CA1. No Render, variáveis de ambiente não têm esse problema.
- **Lista de cursos duplicada:** o frontend fixa SI/CDIA nos selects; o backend valida pela configuração. Ao acrescentar curso, mudar os dois (registrar em `armadilhas.md`).
- **Anti-duplicação e Drive:** não tocados. Quem já tem certificado no Drive continua `EXISTENTE`, mesmo com curso diferente.
- **Efeitos reais:** testes só com mocks; o teste de PDF roda sem Spring e sem Google.
- **`FonteDadosCsvStrategy`/`extrairAlunos`** ignoram a coluna `Curso` (como já ignoram `Horas`); registrar em `armadilhas.md`.

## 8. Perguntas em aberto
- [x] P1 (2026-11-26: nome curto): No Sheets, gravar o **nome curto** do curso ("Sistemas de Informação", como já sai hoje pelo padrão) em vez do nome completo "Bacharelado em ...". Recomendação: nome curto, para manter o histórico da planilha consistente.
- [x] P2 (2026-11-26: SI pré-selecionado): Campo "Curso padrão" sem valor pré-selecionado (obriga escolher) ou já com **SI** selecionado? Recomendação: SI selecionado, para não travar o uso atual.

## 9. Plano de implementação
- [x] P1 — `GestaoProperties` (`@ConfigurationProperties("certificado")`, record com `Curso`) + `@EnableConfigurationProperties` + padrões DACOMP no `application.properties` com `\uXXXX` · arquivos: `config/GestaoProperties.java`, `BackendApplication.java`, `application.properties` · teste: `GestaoPropertiesTest` (Binder sobre o `application.properties` real, sem contexto Spring; acentos corretos) — CA1 · commit: `feat(config): dados da gestão e cursos configuráveis` · agente: `dev-backend`
- [x] P2 — `InterpretadorCurso` (classe pura construída com o mapa de cursos): apelido → código, normalizando caixa, acento e espaços; `nomeCompleto`/`nomeCurto` por código · arquivos: `service/InterpretadorCurso.java` · teste: `InterpretadorCursoTest` — CA5 e inválidos do CA6 · commit: `feat(curso): interpreta o curso por código ou nome` · agente: `dev-backend`
- [x] P3 — Ler a coluna `Curso` no CSV · arquivos: `service/LinhaCsv.java`, `service/CsvService.java` · teste: `CsvServiceTest` — CA4 (e CSV sem a coluna = `null`) · commit: `feat(csv): lê a coluna opcional Curso` · agente: `dev-backend`
- [x] P4 — Validar o curso por linha com padrão do formulário; `LinhaValidadaDTO` + `curso`, `cursoTexto`, `cursoDoFormulario` · arquivos: `service/ValidadorParticipantes.java`, `dto/LinhaValidadaDTO.java` · teste: `ValidadorParticipantesTest` — CA6, CA7, CA9 · commit: `feat(validacao): valida o curso de cada linha` · agente: `dev-backend`
- [x] P5 — Contrato: `cursoPadrao` no DTO do lote e `cursoParticipante` no unitário (ausente = `SI`, inválido = 400); `EmissaoService.validarLote` passa o curso ao validador · arquivos: `dto/EmissaoLoteEventoRequestDTO.java`, `dto/EmissaoIndividualRequestDTO.java`, `service/EmissaoService.java` (só validação) · teste: `CertificadoControllerTest` (prévia com/sem `cursoPadrao`, inválido → 400) + `EmissaoServiceTest` — CA8, CA9 · commit: `feat(api): recebe o curso padrão no lote e no unitário` · agente: `dev-backend`
- [x] P6 ⚠️ — Certificado: `PdfService` recebe curso e gestão; template com `nomeGestao`, `siglaGestao`, `cursoNome`, cidade configurável, RA condicional (`th:if`), imagem por `imagemFundo`, sem CSS duplicado; commita o PNG do DACOMP · arquivos: `service/PdfService.java`, `templates/certificado_evento.html`, `static/images/bg_certificado_evento.png` · teste: `PdfServiceTest` (HTML processado via engine sem Spring; PDF não vazio) — CA2, CA3 · commit: `feat(pdf): certificado com gestão e curso configuráveis` · agente: `dev-backend`
- [ ] P7 ⚠️ — Emissão: lote e unitário passam o curso da linha ao PDF e o **nome curto** ao Sheets; anti-duplicação e Drive intocados · arquivos: `service/EmissaoService.java` · teste: `EmissaoServiceTest` com mocks — CA10, CA11, CA14 · commit: `feat(emissao): usa o curso de cada participante no PDF e no Sheets` · agente: `dev-backend`
- [ ] P8 ⚠️ — E-mail com nome e sigla da gestão (título, cabeçalho, rodapé); assunto com 🐍 inalterado · arquivos: `service/EmailService.java`, `templates/certificado-email.html` · teste: `EmailServiceTest` sobre `montarCorpoEmail` (sem Gmail) — CA12 · commit: `feat(email): textos da gestão configuráveis` · agente: `dev-backend`
- [ ] P9 — Frontend: selects "Curso padrão" (lote, SI pré-selecionado) e "Curso" (unitário), envio de `cursoPadrao`/`cursoParticipante`, coluna Curso na prévia · arquivos: `App.jsx`, `components/CertificadoForm.jsx`, `components/PreVisualizacaoLote.jsx` · teste: `npm run lint` + `npm run build` + conferência no localhost — CA13 (parte) · commit: `feat(front): curso padrão e coluna Curso na prévia` · agente: `dev-frontend`
- [ ] P10 — Frontend: textos DACOMP (cabeçalho, rodapé, título da aba) e ajuda sobre a coluna `Curso` · arquivos: `App.jsx`, `components/AjudaEmissao.jsx`, `index.html` · teste: lint + build + conferência visual — CA13 (resto) · commit: `feat(front): identidade DACOMP e ajuda da coluna Curso` · agente: `dev-frontend`
- [ ] P11 — Fechamento: `armadilhas.md` (acentos em `.properties`, cursos duplicados front/back, Strategy ignora `Curso`), verificação manual em localhost com credenciais de teste, spec → concluída · arquivos: `.claude/memory/*`, spec · teste: suíte completa `./mvnw test` + verificação manual · commit: `docs(spec): conclui F4`

Deploy: backend (Render) antes do frontend (Vercel).

## 10. Registro de execução
- P1: códigos de curso normalizados para maiúsculas no construtor do record (variáveis de ambiente chegam em minúsculas). Teste de binding usa `Binder` sobre o `application.properties` real, sem contexto Spring. 126 testes verdes.
- P2: `InterpretadorCurso` é `@Component` com construtor `(GestaoProperties)`; aceita só os apelidos (o nome completo "Bacharelado em ..." não é apelido). Espaços internos repetidos são reduzidos a um. 144 testes verdes.
- P3: `LinhaCsv` ganhou o componente `curso` e um construtor de 5 argumentos (sem curso) para não reescrever os testes antigos. 149 testes verdes.
- P4: `validarLinhas(linhas, carga, cursoPadrao, interpretadorCurso)`. A versão antiga `validarLinhas(linhas, carga)` ficou **temporária** (ignora a coluna Curso, campos de curso nulos) só para a `EmissaoService` compilar; **remover no P5**. Ordem das mensagens: e-mail, horas, curso. 155 testes verdes.
- P5: overload temporário removido. `ValidadorParticipantes.resolverCurso` (ausente/branco = `CURSO_LEGADO` "SI"; aceita código ou apelido; inválido = `DadosInvalidosException` "Curso inválido: X"). `EmissaoService` recebe `InterpretadorCurso` no construtor. O código do curso já segue no DTO interno de cada linha/unitário (`cursoPadrao`), mas o PDF e o Sheets só passam a usá-lo no P6/P7. Fixture de teste `GestaoDeTeste`. 163 testes verdes.
- P6: `PdfService(engine, GestaoProperties, InterpretadorCurso)`; texto testável via `montarHtml`. Curso lido de `dadosEvento.cursoPadrao()` (código da linha; ausente = SI). Fundo sempre declarado como `image/png` (registrar em armadilhas). Amostras SI/CDIA/sem RA/longo conferidas pelo desenvolvedor. 169 testes verdes.
