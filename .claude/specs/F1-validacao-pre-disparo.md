---
id: F1
titulo: Validação e pré-visualização antes do disparo
status: em andamento        # rascunho | aprovada | em andamento | concluída
branch: feat/validation-pre-fire
criada: 2026-11-20
---

# F1 — Validação e pré-visualização antes do disparo

## 1. Problema
Hoje o botão "Disparar" lê o CSV e já gera PDFs, envia e-mails e grava no Drive e no Sheets, tudo numa chamada só. Um e-mail digitado errado, uma linha duplicada ou uma linha vazia só aparecem depois que o efeito real já aconteceu, e as linhas sem nome ou e-mail são descartadas em silêncio. A diretoria precisa ver e conferir o lote **antes** de confirmar.

## 2. Comportamento esperado
1. A diretoria preenche o evento, anexa o CSV e clica em **"Pré-visualizar lote"**.
2. O backend lê e valida o CSV **sem nenhum efeito colateral**: não gera PDF nem e-mail e não acessa Drive nem Sheets.
3. A tela mostra um resumo (total, válidas, com aviso, com erro) e uma tabela por linha do CSV, com número da linha, nome, e-mail, RA, situação e mensagens.
4. **"Confirmar emissão de N certificados"** reenvia o mesmo CSV e os mesmos campos para `/emitir-lote`, que revalida e executa só as linhas válidas ou com aviso. **"Voltar"** retorna ao formulário com os dados mantidos.
5. O relatório final mostra as linhas inválidas como `ERRO`, com o motivo.

## 3. Critérios de aceitação
- [ ] CA1: Dado um CSV válido, quando chamo `/pre-visualizar-lote`, então recebo a prévia e `PdfService`, `EmailService`, `GoogleDriveService` e `GoogleSheetsService` **nunca** são chamados.
- [ ] CA2: Dada uma linha com nome vazio ou com e-mail vazio ou fora do padrão `^[^\s@]+@[^\s@]+\.[^\s@]+$`, quando valido, então ela fica com situação `ERRO` e uma mensagem que cita o campo.
- [ ] CA3: Dado um e-mail repetido (sem diferenciar maiúsculas e ignorando espaços nas bordas), quando valido, então a 1ª ocorrência é válida e as seguintes ficam `ERRO`, com "E-mail repetido (linha N)".
- [ ] CA4: Dados dois nomes que geram o mesmo nome de PDF com e-mails diferentes, quando valido, então a 2ª ocorrência fica `AVISO` ("Mesmo nome da linha N: o Drive terá dois arquivos com o mesmo nome"). Na execução, **os dois** recebem o certificado.
- [ ] CA5: Dada uma linha com nome e e-mail válidos e RA vazio (ou sem coluna RA), quando valido, então ela fica `AVISO` ("RA não informado") e é emitida normalmente.
- [ ] CA6: Dado `nomeEvento` vazio, `dataRealizacao` ausente ou `cargaHoraria` nula ou ≤ 0, quando chamo `/pre-visualizar-lote`, `/emitir-lote` ou `/emitir-unitario`, então recebo **400** com uma mensagem em português e nenhum efeito ocorre.
- [ ] CA7: Dado um CSV vazio ou sem as colunas Nome e E-mail, quando chamo `/pre-visualizar-lote` ou `/emitir-lote`, então recebo **400** com a mensagem atual de cabeçalho inválido (hoje sai como 500).
- [ ] CA8: Dado um lote com linhas `ERRO`, quando chamo `/emitir-lote`, então essas linhas entram no relatório como `StatusEmissao.ERRO`, com as mensagens em `mensagemErro`, e não geram PDF nem e-mail. As demais seguem o fluxo atual, incluindo a anti-duplicação.
- [ ] CA9: Dado `/emitir-unitario` com nome vazio ou e-mail inválido, então recebo **400** e nenhum efeito ocorre.
- [ ] CA10: No frontend, dada uma prévia com 0 linhas emitíveis, o botão de confirmar fica desabilitado. Com N > 0, o rótulo mostra N.
- [ ] CA11: Dada uma linha em que todas as células estão vazias, quando valido ou emito, então ela é ignorada: não conta em `totalLinhas` e não aparece na prévia nem no relatório.

## 4. Fora do escopo
- Consultar o Drive na prévia para marcar quem já é `EXISTENTE`. A anti-duplicação continua só na execução.
- Excluir ou editar linhas na tela de prévia.
- Mudar a fórmula do nome do PDF ou a regra de homônimos.
- Horas por participante (F2) e obrigar a passar pela prévia (sem token nem flag).

## 5. Contrato
- **Novo:** `POST /api/certificados/pre-visualizar-lote`, `multipart/form-data`, header `X-API-KEY`. Os campos são os mesmos de `EmissaoLoteEventoRequestDTO` (`nomeEvento`, `dataRealizacao`, `cargaHoraria`, `arquivoCsv`). Respostas: 200 `PreVisualizacaoLoteDTO`, 400 (formulário ou CSV inválido), 403 (chave).
- **Alterados:** `/emitir-lote` e `/emitir-unitario` mantêm rota, request e response, e passam a validar (CA6 a CA9).
- **DTOs (records novos em `dto/`):**
  - `PreVisualizacaoLoteDTO(int totalLinhas, int validas, int comAviso, int comErro, List<LinhaValidadaDTO> linhas)`
  - `LinhaValidadaDTO(int linha, String nome, String email, String ra, SituacaoLinha situacao, List<String> mensagens)`. `linha` é o número da linha no arquivo; o cabeçalho é a linha 1.
  - `enum SituacaoLinha { VALIDA, AVISO, ERRO }`
- **CSV:** nenhuma mudança de formato. Continua mapeado por nome de cabeçalho.
- **Compatibilidade:** o frontend atual continua funcionando, porque `/emitir-lote` só ganha a validação. O CSV antigo continua aceito.

## 6. Impacto no código
| Arquivo | Mudança |
|---|---|
| `service/CsvService.java` | novo método que devolve as linhas brutas com o número da linha (`LinhaCsv`), sem descartar as linhas incompletas |
| `service/ValidadorParticipantes.java` (novo, classe pura) | valida o evento e as linhas: CA2 a CA6 |
| `service/EmissaoService.java` | `preVisualizarLote(dto)`. `processarEmissao` usa o validador e manda as linhas `ERRO` direto para o relatório |
| `controller/CertificadoController.java` | rota nova e respostas 400 de validação |
| `dto/` | 3 tipos novos (seção 5) |
| `frontend/src/App.jsx` | estado `previa`, chamada da prévia e confirmação com o mesmo `FormData` |
| `frontend/src/components/PreVisualizacaoLote.jsx` (novo) | resumo, tabela, Confirmar e Voltar |
| `CertificadoForm.jsx` | botão do lote vira "Pré-visualizar lote" |

## 7. Riscos e regras críticas tocadas
- **Anti-duplicação:** a fórmula do nome do PDF não muda. O validador usa a **mesma** fórmula para detectar homônimos. Se der para centralizar sem mudar o resultado, centralizar num único método.
- **Efeitos reais:** a prévia não pode depender de nenhum serviço Google (CA1, teste com `never()`).
- **Revalidação:** o CSV reenviado pode diferir do pré-visualizado. Como a execução revalida, isso é aceitável.

## 8. Perguntas em aberto
- [x] P1: linhas totalmente em branco (ex.: `,,,` remanescentes da exportação do Sheets) são **ignoradas**: não contam no total nem aparecem na prévia ou no relatório.
- [x] P2: usar a branch atual `feat/validation-pre-fire` (sem criar `v2.0`).

## 9. Plano de implementação
- [x] P1 ⚠️ — Centralizar o nome do PDF em `NomeArquivoCertificado.gerar(nome)`, sem mudar o resultado, e usá-lo nos dois pontos do `EmissaoService` · arquivos: `service/NomeArquivoCertificado.java` (novo), `EmissaoService.java` · teste: `NomeArquivoCertificadoTest` (espaços nas bordas e espaços múltiplos) · commit: `refactor(emissao): centraliza nome do arquivo do certificado` · agente: dev-backend
- [x] P2 — Record `LinhaCsv(linha, nome, email, ra)` e `CsvService.lerLinhas(arquivo)`: numera as linhas (cabeçalho = 1), mantém as linhas incompletas, ignora as totalmente em branco (CA11) e lança `DadosInvalidosException` (nova) para CSV vazio ou sem cabeçalho. `extrairAlunos` não muda · arquivos: `CsvService.java`, `service/LinhaCsv.java`, `service/DadosInvalidosException.java` · teste: `CsvServiceTest` (novos casos) · commit: `feat(csv): leitura de linhas brutas com numeração` · agente: dev-backend
- [x] P3 — DTOs `SituacaoLinha`, `LinhaValidadaDTO`, `PreVisualizacaoLoteDTO` e `ValidadorParticipantes.validarLinhas(List<LinhaCsv>)` cobrindo CA2 a CA5 · arquivos: `dto/*` (3), `service/ValidadorParticipantes.java` · teste: `ValidadorParticipantesTest` · commit: `feat(validacao): valida linhas do lote` · agente: dev-backend
- [ ] P4 — `ValidadorParticipantes.validarEvento(nome, data, carga)` e `validarParticipante(nome, email)`, que lançam `DadosInvalidosException` (CA6, CA9) · arquivos: `ValidadorParticipantes.java` · teste: `ValidadorParticipantesTest` · commit: `feat(validacao): valida dados do evento e do participante` · agente: dev-backend
- [ ] P5 — `EmissaoService.preVisualizarLote(dto)`: valida o evento, lê as linhas, valida e monta o resumo. Não chama nenhum serviço Google (CA1) · arquivos: `EmissaoService.java` · teste: `EmissaoServiceTest` com `@Mock` dos 4 serviços e `verifyNoInteractions` · commit: `feat(emissao): pré-visualização do lote sem efeitos` · agente: dev-backend
- [ ] P6 ⚠️ — `processarEmissao` passa a usar `lerLinhas` e o validador: as linhas `ERRO` vão para o relatório como `StatusEmissao.ERRO`, sem PDF nem e-mail, e homônimos com aviso são emitidos. `processarEmissaoIndividual` valida antes de qualquer efeito (CA6, CA8, CA9). A anti-duplicação e a ordem dos efeitos não mudam · arquivos: `EmissaoService.java` · teste: `EmissaoServiceTest` (`verify`/`never` por linha) · commit: `feat(emissao): emissão aplica validação e pula linhas inválidas` · agente: dev-backend
- [ ] P7 — Controller: `POST /pre-visualizar-lote` e 400 para `DadosInvalidosException` nas três rotas (CA6, CA7, CA9) · arquivos: `CertificadoController.java` · teste: `CertificadoControllerTest` (`@WebMvcTest` + `@MockitoBean EmissaoService`: 200, 400, 403) · commit: `feat(api): rota de pré-visualização e erros 400 de validação` · agente: dev-backend
- [ ] P8 — Componente `PreVisualizacaoLote.jsx` (resumo, tabela por linha, Confirmar N desabilitado com 0, Voltar), com CSS em `App.css` · arquivos: `components/PreVisualizacaoLote.jsx`, `App.css` · teste: `npm run lint` + `npm run build` · commit: `feat(front): tela de pré-visualização do lote` · agente: dev-frontend
- [ ] P9 — `App.jsx` e `CertificadoForm.jsx`: o botão do lote vira "Pré-visualizar lote" e chama a rota nova; Confirmar reenvia o mesmo `FormData` para `/emitir-lote`; Voltar mantém os dados (CA10) · arquivos: `App.jsx`, `CertificadoForm.jsx` · teste: lint + build + verificação manual pelo desenvolvedor · commit: `feat(front): fluxo prévia → confirmação no lote` · agente: dev-frontend

## 10. Registro de execução
- P1 (2026-11-20): `NomeArquivoCertificado` package-private, fórmula idêntica. `BackendApplicationTests.contextLoads` não foi rodado porque exige as credenciais Google.
- P2 (2026-11-20): `LinhaCsv` e `DadosInvalidosException` públicos no pacote `service`. A numeração usa `CSVReader.getLinesRead()`, então campos entre aspas com quebra de linha recebem o número da linha onde começam. Uma célula de RA vazia vira `""` e uma coluna ou célula ausente vira `null`: o validador deve tratar os dois como "RA não informado". Erros de leitura também viram `DadosInvalidosException` (400).
- P3 (2026-11-20): decisão do dono do projeto: repetição de e-mail e homônimo só contam contra linhas anteriores que **serão emitidas**. Uma linha com erro mostra só os erros, sem o aviso de RA. O homônimo compara pelo nome do PDF (`NomeArquivoCertificado`), então "Ana  Silva" e "Ana Silva" são homônimos, mas "ANA SILVA" não é.
