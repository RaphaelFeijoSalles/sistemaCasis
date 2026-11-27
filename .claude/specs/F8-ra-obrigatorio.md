---
id: F8
titulo: RA obrigatório, com marcador de participante externo
status: aprovada        # rascunho | aprovada | em andamento | concluída
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

## 10. Registro de execução
