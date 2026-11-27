---
id: F3
titulo: Eventos longos (data de início e fim)
status: em andamento    # rascunho | aprovada | em andamento | concluída
branch: feature/F3-eventos-longos
criada: 2026-11-27
---

# F3 — Eventos longos (data de início e fim)

## 1. Problema
Hoje o evento tem uma única data (`dataRealizacao`). Semanas acadêmicas e cursos de vários dias saem com um dia só no certificado, e o texto "realizado no período de 15 de março de 2026" fica estranho até para eventos de um dia. A diretoria precisa informar início e fim e ver o período correto no certificado e no Sheets.

## 2. Comportamento esperado
1. No formulário (lote e unitário), o campo atual passa a se chamar **Data de início** e surge **Data de fim (opcional)**.
2. Fim vazio = evento de um dia, comportamento igual ao de hoje.
3. O certificado mostra o período em forma compacta; a carga horária continua sendo o **total** (formulário ou coluna `Horas` da F2).
4. A pasta do Drive continua sendo escolhida pela **data de início**.
5. O Sheets ganha a coluna `data_fim` no final.

## 3. Critérios de aceitação
- [ ] CA1: Dado fim vazio e início 15/03/2026, quando o PDF é gerado, então o texto diz "realizado em 15 de março de 2026".
- [ ] CA2: Dado início 01/04/2026 e fim 05/04/2026, então "realizado no período de 01 a 05 de abril de 2026".
- [ ] CA3: Dado 28/04/2026 a 02/05/2026, então "realizado no período de 28 de abril a 02 de maio de 2026".
- [ ] CA4: Dado 28/12/2026 a 03/01/2027, então "realizado no período de 28 de dezembro de 2026 a 03 de janeiro de 2027".
- [ ] CA5: Dado fim igual ao início, então o texto é o mesmo do CA1 (evento de um dia).
- [ ] CA6: Dado fim anterior ao início, quando o lote é pré-visualizado ou emitido (ou o unitário é emitido), então a resposta é 400 com "A data de fim não pode ser anterior à data de início." e nada é enviado.
- [ ] CA7: Dado fim mais de 366 dias depois do início, então a resposta é 400 com "O evento deve durar no máximo 366 dias." e nada é enviado.
- [ ] CA8: Dado 28/06/2026 a 03/07/2026, então a pasta usada é `Raiz/2026-1/<Evento>` (semestre do início).
- [ ] CA9: Dada uma requisição sem o campo `dataFim` (frontend antigo), então a emissão funciona como hoje e o certificado segue o CA1.
- [ ] CA10: Dado um evento com fim, então a linha do Sheets é `nome | ra | curso | data (início) | evento | carga_horaria | data_fim (dd/MM/yyyy)`; sem fim, `data_fim` fica vazio.
- [ ] CA11: No frontend, fim anterior ao início ou duração > 366 dias mostra toast `{ titulo, detalhe }` e não chama a API.

## 4. Fora do escopo
- Horas por dia ou cálculo de carga a partir das datas.
- Datas por participante na planilha (o período é do evento, vem do formulário).
- Mudar o texto do e-mail (ele não mostra data hoje).
- Migrar linhas antigas do Sheets ou criar o cabeçalho `data_fim` automaticamente (o desenvolvedor acrescenta o cabeçalho à mão).

## 5. Contrato
- **Endpoints:** sem rotas novas. `/pre-visualizar-lote`, `/emitir-lote` (multipart) e `/emitir-unitario` (JSON) aceitam o campo opcional `dataFim` (`yyyy-MM-dd`). Erros de período → 400 (`DadosInvalidosException`), como as demais validações do evento.
- **DTOs:** `EmissaoLoteEventoRequestDTO` e `EmissaoIndividualRequestDTO` ganham `LocalDate dataFim` (opcional). `dataRealizacao` **mantém o nome** e passa a significar a data de início (Javadoc atualizado).
- **Template:** `certificado_evento.html` troca "realizado no período de `${dataRealizacao}`" por "realizado `${periodoRealizacao}`", em que a variável já traz "em ..." ou "no período de ...". A frase vem de uma classe utilitária pura `PeriodoFormatter` (no estilo de `CargaHorariaFormatter`).
- **Sheets:** `registrarEmissao` ganha o parâmetro `dataFim`; a 7ª coluna é `data_fim`.
- **Compatibilidade:** o frontend antigo (sem `dataFim`) continua funcionando (CA9). O CSV não muda. Deploy: Render antes da Vercel.

## 6. Impacto no código
| Arquivo | Mudança |
|---|---|
| `dto/EmissaoLoteEventoRequestDTO`, `dto/EmissaoIndividualRequestDTO` | componente `dataFim` + Javadoc |
| `service/PeriodoFormatter` (novo) | frase do período (CA1–CA5) |
| `service/ValidadorParticipantes.validarEvento` | regras do CA6 e do CA7 |
| `service/PdfService` + `templates/certificado_evento.html` | usar `periodoRealizacao` |
| `service/EmissaoService` | repassar `dataFim` (lote e unitário; a pasta continua pela `dataRealizacao`) |
| `service/GoogleSheetsService` | coluna `data_fim` |
| `frontend/src/App.jsx`, `components/CertificadoForm.jsx`, `AjudaEmissao.jsx` | campo, validação, FormData/JSON, rótulos |

## 7. Riscos e regras críticas tocadas
- **Pastas do Drive:** a regra não muda (semestre da data de início = a data atual). Não pode passar a usar `dataFim`.
- **Anti-duplicação:** o nome do arquivo não muda.
- **Contrato `@ModelAttribute`:** o campo do FormData tem de se chamar exatamente `dataFim`.
- **Sheets:** sem o cabeçalho `data_fim` na planilha oficial, a coluna aparece sem título (não quebra nada).
- **Efeitos reais:** testes só com mocks; `PeriodoFormatter` testado sem Spring.

## 8. Perguntas em aberto
- [x] P1 Texto do período → compacto (CA1–CA4).
- [x] P2 Formulário → início + fim opcional.
- [x] P3 Carga horária → continua total.
- [x] P4 Pasta do Drive → semestre do início.
- [x] P5 Sheets → nova coluna `data_fim` no final.
- [x] P6 Limite → até 366 dias.

## 9. Plano de implementação
- [x] P1 — `PeriodoFormatter.formatar(inicio, fim)`: "em ..." / "no período de ..." nas 4 formas compactas, com `fim` nulo ou igual ao início valendo um dia · arquivos: `service/PeriodoFormatter.java` (novo) · teste: `PeriodoFormatterTest` (CA1–CA5, sem Spring) · commit: `feat(periodo): formata o período do evento por extenso` · agente: `dev-backend`
- [x] P2 — Componente `LocalDate dataFim` nos dois DTOs (logo depois de `dataRealizacao`) + Javadoc; atualizar as chamadas dos construtores em `EmissaoService` e nos testes, repassando o `dataFim` recebido · arquivos: `EmissaoLoteEventoRequestDTO`, `EmissaoIndividualRequestDTO`, `EmissaoService`, `EmissaoServiceTest`, `PdfServiceTest` · teste: `CertificadoControllerTest` — multipart e JSON **sem** `dataFim` continuam 200 (CA9) e `dataFim=2026-04-05` chega ao serviço · commit: `feat(dto): campo opcional dataFim no lote e no unitário` · agente: `dev-backend`
- [x] P3 — `validarEvento` recebe `dataFim`: fim < início e fim > início + 366 dias → `DadosInvalidosException` com as mensagens da spec; chamado na prévia, no lote e no unitário · arquivos: `ValidadorParticipantes`, `EmissaoService` · teste: `ValidadorParticipantesTest` (limites: igual, +366 aceito, +367 e −1 recusados) e `EmissaoServiceTest` (CA6/CA7: nenhum mock de Drive/Gmail/Sheets acionado) · commit: `feat(validacao): valida a data de fim do evento` · agente: `dev-backend`
- [x] P4 — `PdfService` define `periodoRealizacao` via `PeriodoFormatter`; template troca "realizado no período de `${dataRealizacao}`" por "realizado `${periodoRealizacao}`" · arquivos: `PdfService`, `templates/certificado_evento.html` · teste: `PdfServiceTest.montarHtml` (um dia e mesmo mês) · commit: `feat(pdf): período de início e fim no certificado` · agente: `dev-backend`
- [x] P5 — Sheets: parâmetro `dataFim` em `registrarEmissao`, 7ª coluna `data_fim` (dd/MM/yyyy ou vazio); `EmissaoService` repassa no lote e no unitário · arquivos: `GoogleSheetsService`, `EmissaoService`, `EmissaoServiceTest` · teste: `EmissaoServiceTest` com `verify(sheetsService).registrarEmissao(..., dataFim)` com e sem fim (CA10) · commit: `feat(sheets): registra a data de fim do evento` · agente: `dev-backend`
- [x] P6 ⚠️ — Proteção da regra de pastas: testes garantindo que `obterOuCriarPastaEvento` recebe a **data de início** mesmo com fim em outro semestre, no lote e no unitário (CA8). Sem mudança de código de produção · arquivos: `EmissaoServiceTest` · commit: `test(drive): pasta do evento longo segue a data de início` · agente: `testador`
- [x] P7 — Frontend: rótulo "Data de início", campo "Data de fim (opcional)" com `min` = início, validação em `App.jsx` (CA11, mesmas mensagens do backend), `dataFim` no FormData e no JSON do unitário (omitido quando vazio), limpeza do estado · arquivos: `App.jsx`, `components/CertificadoForm.jsx` · teste: `npm run lint` + `npm run build` + conferência manual em localhost pelo desenvolvedor · commit: `feat(front): data de início e de fim do evento` · agente: `dev-frontend`
- [x] P8 — Guia lateral: explicar início/fim, fim opcional e que a pasta segue o início · arquivos: `components/AjudaEmissao.jsx` · teste: `npm run lint` · commit: `docs(front): ajuda sobre o período do evento` · agente: `dev-frontend`
- [ ] P9 — Fechamento: `armadilhas.md` (coluna `data_fim` sem cabeçalho na planilha oficial; deploy Render antes da Vercel; pasta pela data de início), `./mvnw test` completo, spec `concluída` · arquivos: `.claude/memory/*`, spec · commit: `docs(spec): conclui F3`

## 10. Registro de execução
- P1: `PeriodoFormatter` + 6 testes (inclui mesmo mês em anos diferentes, sem compactar). Suíte: 183 verdes (sem `contextLoads`).
- P2: `dataFim` nos dois DTOs, repassado no `EmissaoService` (lote e unitário). 2 testes novos no controller + `dataFim == null` nos testes sem o campo (CA9). Suíte: 185 verdes.
- P3: `validarEvento(nome, inicio, fim, carga)` + constante `MAXIMO_DIAS_EVENTO = 366`. 4 testes no validador (inclui início ausente com fim informado) e 3 no serviço (prévia, lote, unitário, todos sem efeitos). Suíte: 192 verdes.
- P4: `periodoRealizacao` no `PdfService` (a variável `dataRealizacao` saiu do template). Teste do evento de um dia agora confere a frase inteira; 1 teste novo de vários dias. Suíte: 193 verdes.
- P5: `registrarEmissao(..., cargaHoraria, dataFim)`, 7ª coluna `data_fim` (dd/MM/yyyy ou vazia). 13 `verify` antigos com `null`; 2 testes novos (lote e unitário com fim). `GoogleSheetsService` não tem teste unitário (monta o cliente real); a formatação da coluna fica para a conferência manual. Suíte: 195 verdes.
- P6 ⚠️: 2 testes (lote e unitário, 28/06 a 03/07/2026) garantem que a pasta recebe a data de início e nunca a de fim. Sem mudança em produção. Suíte: 197 verdes.
- P7: campos "Data de Início" e "Data de Fim (opcional)" numa linha própria (carga horária foi para a linha de baixo). `validarPeriodo` em `App.jsx` (limite conferido: +366 aceito, +367 recusado). `dataFim` omitido do FormData e do JSON quando vazio. **Desvio do plano:** sem `min` nativo no campo de fim, porque o `<form>` não tem `noValidate` e o navegador bloquearia o envio antes do toast (mesma armadilha da carga horária). lint e build verdes; conferência manual em localhost pendente (desenvolvedor).
- P8: item "Datas" do guia lateral com os dois formatos e a regra da pasta. lint e build verdes.
