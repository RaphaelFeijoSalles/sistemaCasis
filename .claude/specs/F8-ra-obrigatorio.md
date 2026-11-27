---
id: F8
titulo: RA obrigatório, com marcador de participante externo
status: em andamento        # rascunho | aprovada | em andamento | concluída
branch: feature/F8-ra-obrigatorio
criada: 2026-11-27
---

# F8 — RA obrigatório, com marcador de participante externo

## 1. Problema
Hoje o RA vazio é só um AVISO no lote (regra da F1), e no unitário só o frontend exige o RA. Certificados saem sem RA por esquecimento, e não dá para distinguir esse esquecimento de um participante que realmente não tem RA (palestrante, aluno de outra instituição). A diretoria quer o RA obrigatório e um jeito explícito de emitir para externos.

## 2. Comportamento esperado
1. O CSV precisa ter a coluna **RA**. Sem ela, o arquivo é rejeitado, como já acontece quando falta Nome ou E-mail.
2. Uma linha com RA vazio vira **ERRO** e não é emitida.
3. RA = `EXTERNO` (sem diferenciar maiúsculas de minúsculas, sem espaços nas bordas) marca um participante externo: a linha recebe o AVISO "Participante externo" e é emitida. Se a coluna Curso estiver preenchida nessa linha, ela vira **ERRO**.
4. O certificado do externo não tem o trecho do RA nem o do curso: "<NOME>, participou da <evento>, promovida pelo ...".
5. No Sheets, o externo fica com `ra = EXTERNO` e `curso` vazio.
6. No unitário, o novo checkbox **Participante externo** desabilita os campos RA e Curso e envia `raParticipante = "EXTERNO"` sem `cursoParticipante`. O backend recusa RA vazio com 400.
7. O RA não tem validação de formato: qualquer texto não vazio vale.

## 3. Critérios de aceitação
- [ ] CA1: Dado um CSV sem a coluna RA, quando ele é pré-visualizado ou emitido, então a resposta é 400 e nada é enviado.
- [ ] CA2: Dada uma linha com RA vazio, então ela fica com ERRO "RA não informado" e não é emitida (o AVISO de mesmo texto deixa de existir).
- [ ] CA3: Dada uma linha com RA `externo` ou ` EXTERNO `, sem Curso, então ela fica com AVISO "Participante externo" e é emitida.
- [ ] CA4: Dada uma linha com RA `EXTERNO` e a coluna Curso preenchida (com um curso válido ou não), então ela fica com ERRO "Participante externo não tem curso" e não é emitida.
- [ ] CA5: Dado um externo, quando o PDF é gerado, então o texto não contém "portador(a) do RA", "aluno(a) do curso de" nem "EXTERNO".
- [ ] CA6: Dado um aluno com RA, então o certificado continua igual ao de hoje (RA e curso).
- [ ] CA7: Dado um externo, então a linha do Sheets é `nome | EXTERNO | (vazio) | data | evento | carga_horaria | data_fim`.
- [ ] CA8: Dado um unitário com `raParticipante` nulo ou em branco, então a resposta é 400 e nada é enviado.
- [ ] CA9: Dado um unitário com `raParticipante = "EXTERNO"` e sem `cursoParticipante`, então ele é emitido conforme o CA5 e o CA7.
- [ ] CA10: No frontend, marcar **Participante externo** desabilita e limpa RA e Curso. O guia lateral, a dica da dropzone e a mensagem de cabeçalho inválido passam a dizer que o RA é obrigatório e explicam o `EXTERNO`.

## 4. Fora do escopo
- Validar o formato ou o tamanho do RA.
- Uma coluna `Tipo` nova ou um campo novo no DTO (o marcador fica no próprio RA).
- Textos diferentes para externos no e-mail (o e-mail não mostra RA nem curso).
- Remover o "N/A" do Sheets e o `th:if` do RA no template: ficam como defesa. O item `th:if="${ra != null}"` da F7 continua no backlog.
- Corrigir certificados já emitidos sem RA.

## 5. Contrato
- **Endpoints:** sem rota nova. `/pre-visualizar-lote` e `/emitir-lote` dão 400 sem a coluna RA. `/emitir-unitario` dá 400 com RA em branco.
- **DTOs:** sem campo novo. `EmissaoIndividualRequestDTO.raParticipante` passa a ser obrigatório (valor `EXTERNO` para externos). `LinhaValidadaDTO` sem mudança: para externos, `curso` e `cursoTexto` vão nulos.
- **CSV:** cabeçalho com Nome, E-mail e RA obrigatórios; Horas e Curso continuam opcionais.
- **Template:** o trecho "portador(a) do RA ... aluno(a) do curso de <curso>," só aparece para quem não é externo.
- **Compatibilidade:** CSVs antigos sem a coluna RA deixam de ser aceitos (mudança intencional). O frontend antigo já exige RA no unitário e continua funcionando. Deploy: Render antes da Vercel; até lá, o guia antigo diz "RA opcional".

## 6. Impacto no código
| Arquivo | Mudança |
|---|---|
| `service/RegistroAcademico.java` (novo) | `ehExterno(String)` e a constante `EXTERNO`, classe pura testável |
| `service/CsvService.java` | `lerLinhas` exige a coluna RA; mensagem do cabeçalho |
| `service/ValidadorParticipantes.java` | RA vazio = ERRO; externo = AVISO; externo com curso = ERRO, sem resolver curso; `validarParticipante` exige RA |
| `service/EmissaoService.java` | unitário e lote: externo sem curso; Sheets com `EXTERNO` e curso vazio |
| `service/GoogleSheetsService.java` | curso vazio fica vazio para externo (hoje vira "Sistemas de Informação"); achado no `/planejar` |
| `service/PdfService.java` + `certificado_evento.html` | variável `externo` que omite o trecho RA/curso |
| `frontend/src/App.jsx`, `CertificadoForm.jsx` | checkbox externo; validação; não envia o curso |
| `AjudaEmissao.jsx`, dicas e mensagens | textos do RA obrigatório e do `EXTERNO` |

## 7. Riscos e regras críticas tocadas
- A anti-duplicação, as pastas do Drive e o nome do PDF não mudam.
- **Mudança de comportamento em produção:** CSVs que a diretoria já usa sem a coluna RA passam a ser recusados. É preciso avisar no deploy.
- O curso "SI" padrão (compatibilidade da F4) **não** pode ser aplicado a externos no unitário.
- `extrairAlunos` e `FonteDadosCsvStrategy` (sem uso) não recebem a regra; registrar em `armadilhas.md` para a F5.

## 8. Perguntas em aberto
- [x] P1 (resolvida em 2026-11-27: 400): No unitário, um externo com `cursoParticipante` preenchido deve dar 400, como o CA4 do lote, ou o curso deve ser ignorado? Proposta: 400, por coerência com o lote.
- [x] P2 (aprovada em 2026-11-27): Textos propostos: cabeçalho "Cabeçalho inválido. Não foi possível identificar as colunas 'Nome', 'E-mail' e 'RA'."; unitário "O RA do participante é obrigatório. Para participante externo, use EXTERNO."; erros/avisos como nos CA2–CA4. Pode ser assim?

## 9. Plano de implementação
- [x] P1 — `RegistroAcademico` (novo, puro): `EXTERNO`, `ehExterno(String)` (trim, sem caixa) e `normalizar(String)` (trim; externo vira `EXTERNO`) · arquivos: `service/RegistroAcademico.java` · teste: `RegistroAcademicoTest` · commit: `feat(ra): utilitário de RA e marcador EXTERNO` · agente: dev-backend
- [x] P2 — `CsvService.lerLinhas` exige a coluna RA, com a mensagem da P2 (`extrairAlunos`, sem uso, fica igual); migrar para `Nome;RA;E-mail` os fixtures sem RA (`CsvServiceTest`, `EmissaoServiceTest`, `CertificadoControllerTest`) · teste: `CsvServiceTest` (cabeçalho sem RA → `DadosInvalidosException`) · CA1 · commit: `feat(csv): coluna RA obrigatória no lote` · agente: dev-backend
- [x] P3 — `ValidadorParticipantes.validarLinhas`: RA vazio = ERRO; externo = AVISO "Participante externo"; externo com Curso = ERRO "Participante externo não tem curso"; externo sem curso resolvido (`curso`/`cursoTexto` nulos) · teste: `ValidadorParticipantesTest` · CA2–CA4 · commit: `feat(validacao): RA vazio vira erro e externo vira aviso` · agente: dev-backend
- [x] P4 — Unitário: `validarParticipante` exige RA (mensagem da P2); externo com `cursoParticipante` → 400; externo não passa por `resolverCurso` · arquivos: `ValidadorParticipantes`, `EmissaoService` (só a validação do unitário) · teste: `ValidadorParticipantesTest`, `EmissaoServiceTest` (nenhum mock Google chamado no 400) · CA8, P1 · commit: `feat(unitario): RA obrigatório no backend` · agente: dev-backend
- [x] P5 — ⚠️ Certificado: `PdfService` define `externo` (via `RegistroAcademico`), não resolve curso para externo; o template omite "portador(a) do RA … aluno(a) do curso de …," quando `externo` · arquivos: `PdfService`, `certificado_evento.html` · teste: `PdfServiceTest` (CA5 e CA6) · commit: `feat(pdf): certificado de participante externo sem RA e curso` · agente: dev-backend
- [ ] P6 — ⚠️ Sheets: curso vazio para externo; o fallback "Sistemas de Informação" e o "N/A" ficam para os demais casos · arquivos: `GoogleSheetsService` (extrair a montagem da linha para um método testável) · teste: `GoogleSheetsServiceTest` · CA7 · commit: `feat(sheets): externo sem curso na planilha` · agente: dev-backend
- [ ] P7 — ⚠️ `EmissaoService` lote e unitário: aluno com RA normalizado (`EXTERNO`), curso nulo para externo no PDF e no Sheets; nome do PDF e pasta intactos · teste: `EmissaoServiceTest` (externo no lote e no unitário: args de `pdfService` e `registrarEmissao`; `nomePdf` igual) · CA7, CA9 · commit: `feat(emissao): emissão de participante externo` · agente: dev-backend
- [ ] P8 — Frontend unitário: checkbox **Participante externo** desabilita e limpa RA e Curso; `validarRegistro` aceita externo; payload com `raParticipante: "EXTERNO"` e sem `cursoParticipante` · arquivos: `App.jsx`, `CertificadoForm.jsx` (+ CSS se precisar) · teste: `npm run lint && npm run build` + verificação manual pelo desenvolvedor · CA10 · commit: `feat(frontend): checkbox de participante externo no unitário` · agente: dev-frontend
- [ ] P9 — Frontend textos: guia lateral (RA obrigatório, `EXTERNO`, erros/avisos da prévia), dica da dropzone, mensagens de cabeçalho/CSV ausente no `App.jsx` · arquivos: `AjudaEmissao.jsx`, `CertificadoForm.jsx`, `App.jsx` · teste: lint + build · CA10 · commit: `docs(frontend): guia e dicas do RA obrigatório` · agente: dev-frontend
- [ ] P10 — Memória: `armadilhas.md` (convenção `EXTERNO`; CSV sem RA recusado; `extrairAlunos`/Strategy sem a regra para a F5; deploy Render antes da Vercel e aviso à diretoria) · teste: suíte completa sem `contextLoads` · commit: `docs(memoria): armadilhas da F8`

## 10. Registro de execução
- 2026-11-27 · P1 · `RegistroAcademico` (`EXTERNO`, `ehExterno`, `normalizar`) + `RegistroAcademicoTest` (14 casos); suíte sem `contextLoads`: 214 testes verdes.
- 2026-11-27 · P2 · `lerLinhas` exige a coluna RA (`HeaderMap.isValidoComRa`; `extrairAlunos` igual); fixtures migrados em `CsvServiceTest`, `EmissaoServiceTest` (inclusive o `registrarEmissao` de `loteComFim`, que passou de `null` para `"12345"`) e `CertificadoControllerTest`; `deveRetornarRaNuloQuandoNaoHaColunaRa` virou o teste de recusa; +2 testes (RA vazio/nulo na célula, `extrairAlunos` sem RA). Suíte sem `contextLoads`: 216 testes verdes.
- 2026-11-27 · P3 · `validarLinhas`: RA vazio vira ERRO (depois do e-mail, antes das horas); externo = AVISO "Participante externo" (depois do homônimo), sem curso (`curso`/`cursoTexto` nulos, `cursoDoFormulario` false); externo com Curso = ERRO "Participante externo não tem curso" (sem "Curso inválido"); `ra` do DTO sem normalização. +8 testes em `ValidadorParticipantesTest`; fixtures ajustados em `EmissaoServiceTest` (prévia com `EXTERNO`; emissão com RA e linha nova sem RA como ERRO) e `CertificadoControllerTest`. Suíte sem `contextLoads`: 222 testes verdes.
- 2026-11-27 · P4 · `validarParticipante(nome, email, ra)`: RA nulo/em branco → "O RA do participante é obrigatório. Para participante externo, use EXTERNO." (depois de nome e e-mail); novo `resolverCursoDoParticipante(ra, curso, interpretador)`: externo com curso → "Participante externo não tem curso." (com ponto, como as demais exceções do unitário), externo sem curso → `null`, demais delegam a `resolverCurso`; `EmissaoService` só na etapa 0. +7 testes em `ValidadorParticipantesTest`, +2 em `EmissaoServiceTest` (sem efeitos); fixtures do unitário já tinham RA. Suíte sem `contextLoads`: 231 testes verdes.
- 2026-11-27 · P5 · `PdfService.montarHtml` define `externo` (`RegistroAcademico.ehExterno`); externo não resolve curso (`cursoNome` e `ra` nulos); template envolve "portador(a) do RA … aluno(a) do curso de …, " em `th:unless="${externo}"` (o `th:if="${ra}"` interno fica). +3 testes em `PdfServiceTest` (CA5 com 3 grafias, externo com `CDIA`, PDF de externo); testes de CA6 e RA vazio sem alteração. Suíte sem `contextLoads`: 234 testes verdes.
