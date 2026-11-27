# Roadmap v2.0 — progresso

Status: `backlog` → `spec` (rascunho) → `aprovada` → `em andamento` → `concluída`
Atualize esta tabela sempre que uma spec mudar de status.

| ID | Feature | Depende de | Status | Spec |
|---|---|---|---|---|
| F1 | Etapa de validação antes do disparo: pré-visualização do lote e validação de dados em Java (duplicados, e-mails, vazios, horas), com confirmação explícita antes de gerar PDFs e enviar e-mails | — | concluída | [F1-validacao-pre-disparo.md](../specs/F1-validacao-pre-disparo.md) |
| F2 | Planilha com horas por participante (lote com cargas horárias diferentes) | F1 | concluída | [F2-horas-por-participante.md](../specs/F2-horas-por-participante.md) |
| F3 | Eventos longos: data de início e fim | — | concluída | [F3-eventos-longos.md](../specs/F3-eventos-longos.md) |
| F4 | Gestão configurável (nome, sigla, cidade, fundo, cursos) e curso por participante na planilha | F2 | concluída | [F4-gestao-configuravel-e-curso.md](../specs/F4-gestao-configuravel-e-curso.md) |
| F5 | Banco de dados (PostgreSQL + JPA): eventos, participantes, histórico; ativar `FonteDadosBancoStrategy` | F1 | backlog | — |
| F6 | Check-in via QR Code com painel de aprovação e emissão em massa | F5 | backlog | — |
| F7 | Qualidade e segurança: testes com mocks, validação de DTOs, tratamento de erros, autenticação | contínuo | backlog | — |
| F8 | RA obrigatório: RA vazio vira ERRO no lote (hoje é AVISO, regra da F1) e o backend passa a exigir RA no unitário (hoje só o frontend exige) | F4 | backlog | — |

## Ordem sugerida
F1 → F2 → F4 → F3 → F7 (em paralelo, em pequenas fatias) → F5 → F6

## Pontos em aberto (resolver na spec, perguntando ao dono do projeto)
- ~~**F4:** "modelo de certificado"~~: resolvido em 2026-11-26 (gestão configurável + curso por linha; veja a spec).
- ~~**F3:** período no certificado, carga total, semestre do Drive~~: resolvido em 2026-11-27 (veja a spec).
- **F8** (pedido em 2026-11-26, depois da F4): RA vazio deve bloquear a linha. Decidir: o Sheets ainda grava "N/A"? O trecho condicional do RA no certificado (F4) continua necessário? Participantes externos sem RA deixam de poder receber certificado?
- **F5:** onde hospedar o PostgreSQL? Precisa de ferramenta de migração (Flyway)?

## Backlog da F7 (fatias pequenas)
Vindos do `/revisar` da F4 (2026-11-26), nenhum bloqueante:
- [ ] `certificado_evento.html`: trocar `th:if="${ra}"` por `th:if="${ra != null}"` (o Thymeleaf trata "false", "off" e "no" como falso).
- [ ] `GestaoProperties`: `@Validated` + `@NotBlank` em nome, sigla, cidade e imagem, para o backend não subir com configuração incompleta (hoje sairia "promovida pelo null (null)").
- [ ] `InterpretadorCurso`: recusar na inicialização um apelido repetido em dois cursos (hoje o último vence sem aviso).
- [ ] `LinhaCsv`: o construtor de 5 argumentos só existe para testes antigos; migrar os testes e removê-lo.

Vindo da F3 (P4, 2026-11-27):
- [ ] `certificado_evento.html`: concordância em "participou da <evento>, promovida pelo …, **realizado** em …" ("promovida" no feminino, "realizado" no masculino). Texto final a decidir com o desenvolvedor.

Vindos do `/revisar` da F3 (2026-11-27), nenhum bloqueante:
- [ ] Mensagem de data ausente: o backend diz "A data de realização é obrigatória." (`ValidadorParticipantes.validarEvento`) e o frontend diz "Data de início obrigatória". Alinhar o backend (e o teste) para "data de início".
- [ ] `PreVisualizacaoLote.jsx`: mostrar os dados do evento (nome, período como sairá no certificado, carga) antes da confirmação do lote.
- [ ] `EmissaoService`: logar o período do evento (início/fim) no começo do lote e do unitário, para diagnóstico no Render.
