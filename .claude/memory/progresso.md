# Roadmap v2.0 — progresso

Status: `backlog` → `spec` (rascunho) → `aprovada` → `em andamento` → `concluída`
Atualize esta tabela sempre que uma spec mudar de status.

| ID | Feature | Depende de | Status | Spec |
|---|---|---|---|---|
| F1 | Etapa de validação antes do disparo: pré-visualização do lote e validação de dados em Java (duplicados, e-mails, vazios, horas), com confirmação explícita antes de gerar PDFs e enviar e-mails | — | concluída | [F1-validacao-pre-disparo.md](../specs/F1-validacao-pre-disparo.md) |
| F2 | Planilha com horas por participante (lote com cargas horárias diferentes) | F1 | em andamento | [F2-horas-por-participante.md](../specs/F2-horas-por-participante.md) |
| F3 | Eventos longos: data de início e fim | — | backlog | — |
| F4 | Modelo de certificado que permite preencher o nome do curso | — | backlog | — |
| F5 | Banco de dados (PostgreSQL + JPA): eventos, participantes, histórico; ativar `FonteDadosBancoStrategy` | F1 | backlog | — |
| F6 | Check-in via QR Code com painel de aprovação e emissão em massa | F5 | backlog | — |
| F7 | Qualidade e segurança: testes com mocks, validação de DTOs, tratamento de erros, autenticação | contínuo | backlog | — |

## Ordem sugerida
F1 → F2 → F3 → F4 → F7 (em paralelo, em pequenas fatias) → F5 → F6

## Pontos em aberto (resolver na spec, perguntando ao dono do projeto)
- **F4:** "modelo de certificado" significa (a) um campo `curso` injetado no template atual, (b) upload de template ou fundo por evento, ou (c) escolher entre modelos fixos?
- **F3:** como o certificado exibe o período? A carga horária continua sendo total? Em qual semestre ou pasta do Drive fica um evento que cruza semestres?
- **F5:** onde hospedar o PostgreSQL? Precisa de ferramenta de migração (Flyway)?
