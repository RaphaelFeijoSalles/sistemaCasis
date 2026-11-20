---
id: F2
titulo: Horas por participante no lote
status: em andamento    # rascunho | aprovada | em andamento | concluída
branch: feature/F2-horas-por-participante
criada: 2026-11-20
---

# F2 — Horas por participante no lote

## 1. Problema
Hoje todo o lote usa a mesma carga horária, a do formulário. Em eventos em que cada participante ficou um tempo diferente (chegou depois, saiu antes, participou de só uma parte), a diretoria precisa dividir o CSV e emitir vários lotes, um por carga horária. A F2 permite informar as horas de cada participante numa coluna do CSV.

## 2. Comportamento esperado
1. A diretoria adiciona ao CSV uma coluna chamada **`Horas`** (opcional) e preenche as horas de cada participante.
2. O campo "Carga horária" do formulário continua obrigatório e passa a ser o **padrão**: vale para as linhas com a célula vazia e para o CSV inteiro quando ele não tem a coluna.
3. A pré-visualização mostra uma coluna **Horas** com o texto que vai no certificado ("1 hora e 30 minutos"). Quando o valor veio do formulário, aparece a marca "(formulário)".
4. Valor inválido na célula: a linha fica `ERRO` e não é emitida.
5. Na emissão, cada certificado (PDF) e cada registro no Sheets usam as horas da própria linha.

## 3. Critérios de aceitação
- [ ] CA1: Dado um CSV com a coluna cujo cabeçalho normalizado (sem acento, minúsculo, sem espaços nas bordas) é exatamente `horas`, quando leio as linhas, então cada `LinhaCsv` traz o texto bruto da célula de horas. Outros cabeçalhos ("Carga horária", "CH", "Hora") **não** são reconhecidos.
- [ ] CA2: Dados os valores `2`, `1.5`, `1,5`, ` 1,5 `, `01:30`, `1:30:00` e `1:30:45`, quando interpreto as horas, então obtenho 2.0, 1.5, 1.5, 1.5, 1.5, 1.5 e 1.5 (os segundos são **ignorados**).
- [ ] CA3: Dados os valores `abc`, `0`, `0,0`, `-2`, `1:75`, `0:00:45` e `1.5h`, quando valido a linha, então ela fica `ERRO` com a mensagem `Horas inválidas: <valor>`, sem PDF nem e-mail.
- [ ] CA4: Dada uma linha com a célula de horas vazia, ou um CSV sem a coluna `Horas`, quando valido, então a linha usa a carga do formulário, com `horasDoFormulario = true`, **sem** aviso adicional.
- [ ] CA5: Dada uma linha válida, quando chamo `/pre-visualizar-lote`, então `LinhaValidadaDTO` traz `horas` (decimal), `horasTexto` (igual a `CargaHorariaFormatter.formatarTexto(horas)`) e `horasDoFormulario`. Numa linha `ERRO` por horas inválidas, `horas` e `horasTexto` são `null`.
- [ ] CA6: Dado um lote com horas diferentes por linha, quando chamo `/emitir-lote`, então o PDF de cada participante é gerado com as horas da sua linha e `registrarEmissao` recebe essas mesmas horas (verificado com mocks).
- [ ] CA7: Dado um CSV sem a coluna `Horas`, quando emito, então o resultado é idêntico ao da F1: todas as linhas usam a carga do formulário.
- [ ] CA8: No frontend, a tabela da prévia tem a coluna **Horas** com `horasTexto`, acrescido de "(formulário)" quando `horasDoFormulario` for verdadeiro, e "—" quando `horasTexto` for `null`.
- [ ] CA9: Dada uma linha com horas inválidas **e** e-mail inválido, quando valido, então as duas mensagens aparecem, separadas na lista `mensagens`.

## 4. Fora do escopo
- Emissão unitária: não muda.
- Sinônimos para o nome da coluna (só `Horas`).
- Reemitir quem já tem certificado no Drive com horas diferentes: a anti-duplicação continua pelo nome do arquivo, então o resultado é `EXISTENTE`.
- Mudar o texto do template ("carga horária total de ...").
- Limite máximo de horas.

## 5. Contrato
- **Rotas:** nenhuma nova. `/pre-visualizar-lote` e `/emitir-lote` mantêm rota e request. `cargaHoraria` continua obrigatório (> 0) e passa a ser o padrão.
- **`LinhaCsv`:** ganha `String horas`. É o texto bruto sem espaços nas bordas, e `null` quando não há coluna ou célula.
- **`LinhaValidadaDTO`:** ganha `Double horas`, `String horasTexto` e `boolean horasDoFormulario`, nesta ordem, depois de `ra`.
- **Interpretação:** decimal `^\d+([.,]\d+)?$` ou duração `^\d+:[0-5]\d(:[0-5]\d)?$` (segundos ignorados). O resultado precisa ser > 0.
- **CSV:** coluna nova opcional `Horas`. Com delimitador `,`, um valor como `1,5` precisa vir entre aspas, como o Google Sheets já exporta.
- **Compatibilidade:** CSV antigo funciona igual (CA7). O frontend em produção ignora os campos novos do DTO.

## 6. Impacto no código
| Arquivo | Mudança |
|---|---|
| `service/CsvService.java` | mapear `indexHoras` (cabeçalho `horas`) e preencher `LinhaCsv.horas` |
| `service/LinhaCsv.java` | componente `horas` |
| `service/InterpretadorHoras.java` (novo, classe pura) | texto → `Double` em horas decimais, ou vazio se inválido (CA2, CA3) |
| `service/ValidadorParticipantes.java` | `validarLinhas(linhas, cargaPadrao)`: resolve as horas da linha (CA3 a CA5, CA9) |
| `dto/LinhaValidadaDTO.java` | 3 componentes novos |
| `service/EmissaoService.java` | PDF e Sheets com as horas da linha (cópia do request com `cargaHoraria` da linha; `PdfService` não muda) |
| `frontend/src/components/PreVisualizacaoLote.jsx` (+ `App.css`) | coluna Horas (CA8) |
| `CertificadoForm.jsx` / `AjudaEmissao.jsx` | textos: coluna `Horas` opcional e campo do formulário como padrão |

## 7. Riscos e regras críticas tocadas
- **Anti-duplicação:** o nome do PDF não muda. Quem já foi emitido continua `EXISTENTE`, mesmo com horas diferentes.
- **Efeitos reais:** a prévia continua sem tocar nos serviços Google (testes da F1 com `verifyNoInteractions` seguem valendo).
- **Delimitador:** com CSV separado por `,` e sem aspas, `1,5` quebra as colunas da linha. A ajuda precisa mencionar isso.
- **Arredondamento:** `CargaHorariaFormatter` arredonda ao minuto. Decimais como `1.33` viram "1 hora e 20 minutos", o que já acontece hoje.

## 8. Perguntas em aberto
- [x] P1: nome da coluna: só `Horas`.
- [x] P2: formatos: decimal (`.` ou `,`) e `HH:MM[:SS]`, com segundos ignorados.
- [x] P3: célula vazia ou sem coluna: usa o formulário, sem aviso.
- [x] P4: valor inválido: `ERRO` na linha.
- [x] P5: unitário não muda. A prévia mostra a coluna Horas com a origem.
- [x] P6: branch `feature/F2-horas-por-participante` a partir de `main`, depois do merge da F1 no fork (sem `v2.0`).

## 9. Plano de implementação
- [x] P1 — `InterpretadorHoras.interpretar(String)` (classe pura, package-private): devolve `Optional<Double>` com as horas decimais ou vazio se inválido. Aceita decimal com `.` ou `,` e `HH:MM[:SS]` com segundos ignorados; exige resultado > 0 (CA2, CA3) · arquivos: `service/InterpretadorHoras.java` (novo) · teste: `InterpretadorHorasTest` (todos os valores de CA2 e CA3, `null` e vazio) · commit: `feat(horas): interpreta horas em decimal ou HH:MM` · agente: dev-backend
- [ ] P2 — `LinhaCsv` ganha `horas`; `CsvService` mapeia o cabeçalho normalizado exatamente `horas` e preenche o texto bruto (`null` sem coluna ou célula). Atualizar as construções de `LinhaCsv` nos testes existentes (CA1) · arquivos: `service/LinhaCsv.java`, `service/CsvService.java`, `ValidadorParticipantesTest` (só construtores) · teste: `CsvServiceTest` (coluna `Horas`, `HORAS`, sem coluna, célula vazia, "Carga horária" não reconhecida) · commit: `feat(csv): lê a coluna Horas` · agente: dev-backend
- [ ] P3 — `LinhaValidadaDTO` ganha `horas`, `horasTexto`, `horasDoFormulario`; `ValidadorParticipantes.validarLinhas(linhas, cargaPadrao)` resolve as horas (célula vazia → padrão; inválida → `ERRO` "Horas inválidas: X", acumulando com outros erros). `EmissaoService.validarLote` passa a carga do formulário. `horasTexto` vem de `CargaHorariaFormatter.formatarTexto` (CA3, CA4, CA5, CA9) · arquivos: `dto/LinhaValidadaDTO.java`, `service/ValidadorParticipantes.java`, `service/EmissaoService.java` (só a chamada) · teste: `ValidadorParticipantesTest` (novos casos) + `CertificadoControllerTest` (JSON da prévia traz os 3 campos) · commit: `feat(validacao): resolve as horas de cada linha do lote` · agente: dev-backend
- [ ] P4 ⚠️ — `processarEmissao` gera o PDF e registra no Sheets com `linha.horas()`, usando uma cópia do `EmissaoLoteEventoRequestDTO` com a `cargaHoraria` da linha. `PdfService`, anti-duplicação, e-mail e ordem dos efeitos não mudam (CA6, CA7) · arquivos: `service/EmissaoService.java` · teste: `EmissaoServiceTest` (lote com horas diferentes: `ArgumentCaptor` no `PdfService` e `registrarEmissao` por linha; CSV sem coluna usa o formulário) · commit: `feat(emissao): certificado e planilha com as horas de cada participante` · agente: dev-backend
- [ ] P5 — Coluna **Horas** na tabela da prévia: `horasTexto`, "(formulário)" quando `horasDoFormulario`, "—" quando `null` (CA8) · arquivos: `components/PreVisualizacaoLote.jsx`, `App.css` · teste: `npm run lint` + `npm run build` · commit: `feat(front): coluna de horas na pré-visualização` · agente: dev-frontend
- [ ] P6 — Textos: dica da dropzone e do campo "Carga Horária" (padrão quando o CSV não traz `Horas`) e `AjudaEmissao` (coluna `Horas` opcional, formatos aceitos, `1,5` entre aspas em CSV separado por vírgula) · arquivos: `components/CertificadoForm.jsx`, `components/AjudaEmissao.jsx` · teste: lint + build + verificação manual pelo desenvolvedor · commit: `docs(front): explica a coluna Horas no formulário e na ajuda` · agente: dev-frontend

## 10. Registro de execução
- P1 (2026-11-20): `InterpretadorHoras` package-private, retorna `Optional<Double>`. Nulo ou em branco também retorna vazio; quem chama (P3) decide que célula vazia usa o formulário. Minutos e segundos exigem dois dígitos (`1:5` é inválido); a parte de horas aceita qualquer número de dígitos (`100:15`). `0:00` é recusado por resultar em zero. 27 casos parametrizados; suíte com 86 testes verdes (sem `BackendApplicationTests`).
