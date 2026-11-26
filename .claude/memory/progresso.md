# Roadmap v2.0 — progresso

Status: `backlog` → `spec` (rascunho) → `aprovada` → `em andamento` → `concluída`
Atualize esta tabela sempre que uma spec mudar de status.

| ID | Feature | Depende de | Status | Spec |
|---|---|---|---|---|
| F1 | Etapa de validação antes do disparo: pré-visualização do lote e validação de dados em Java (duplicados, e-mails, vazios, horas), com confirmação explícita antes de gerar PDFs e enviar e-mails | — | concluída | [F1-validacao-pre-disparo.md](../specs/F1-validacao-pre-disparo.md) |
| F2 | Planilha com horas por participante (lote com cargas horárias diferentes) | F1 | concluída | [F2-horas-por-participante.md](../specs/F2-horas-por-participante.md) |
| F3 | Eventos longos: data de início e fim | — | backlog | — |
| F4 | Gestão configurável (nome, sigla, cidade, fundo, cursos) e curso por participante na planilha | F2 | concluída | [F4-gestao-configuravel-e-curso.md](../specs/F4-gestao-configuravel-e-curso.md) |
| F5 | Banco de dados (PostgreSQL + JPA): eventos, participantes, histórico; ativar `FonteDadosBancoStrategy` | F1 | backlog | — |
| F6 | Check-in via QR Code com painel de aprovação e emissão em massa | F5 | backlog | — |
| F7 | Qualidade e segurança: testes com mocks, validação de DTOs, tratamento de erros, autenticação | contínuo | backlog | — |
| F8 | RA obrigatório: RA vazio vira ERRO no lote (hoje é AVISO, regra da F1) e o backend passa a exigir RA no unitário (hoje só o frontend exige) | F4 | backlog | — |

## Ordem sugerida
F1 → F2 → F4 → F3 → F7 (em paralelo, em pequenas fatias) → F5 → F6

## Pontos em aberto (resolver na spec, perguntando ao dono do projeto)
- ~~**F4:** "modelo de certificado"~~: resolvido em 2026-11-26 (gestão configurável + curso por linha; veja a spec).
- **F3:** como o certificado exibe o período? A carga horária continua sendo total? Em qual semestre ou pasta do Drive fica um evento que cruza semestres?
- **F8** (pedido em 2026-11-26, depois da F4): RA vazio deve bloquear a linha. Decidir: o Sheets ainda grava "N/A"? O trecho condicional do RA no certificado (F4) continua necessário? Participantes externos sem RA deixam de poder receber certificado?
- **F5:** onde hospedar o PostgreSQL? Precisa de ferramenta de migração (Flyway)?
