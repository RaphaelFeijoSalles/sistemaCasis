---
id: F4
titulo: Gestão configurável e curso por participante
status: aprovada        # rascunho | aprovada | em andamento | concluída
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
(preenchido por `/planejar`)

## 10. Registro de execução
