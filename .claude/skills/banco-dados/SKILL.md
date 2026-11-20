---
name: banco-dados
description: Guia para a Fase 2 do CASIS, a introdução de banco de dados (PostgreSQL + Spring Data JPA) e do check-in por QR Code (F5, F6), sem quebrar a emissão por CSV. Use ao especificar ou implementar F5/F6, entidades JPA, repositórios, migrações ou FonteDadosBancoStrategy.
---

# Banco de dados e check-in (Fase 2)

## Decisões que a spec F5 DEVE tomar (pergunte, não assuma)
- Onde o PostgreSQL vai rodar em produção (Render Postgres, Supabase, Neon...) e localmente (Docker?).
- Ferramenta de migração: Flyway (recomendado) ou `ddl-auto`.
- Banco de testes: H2 em modo PostgreSQL (simples) ou Testcontainers (fiel, mas exige Docker).
- O CSV continua existindo como fonte? (Recomendado: sim, o Strategy permite as duas fontes.)
- Retroatividade: importar o histórico da planilha do Sheets para o banco?

## Modelo inicial sugerido (validar na spec)
```
Evento(id, nome, dataInicio, dataFim, cargaHoraria, curso?, criadoEm)
Participante(id, nome, email UNIQUE, ra?)
Participacao(id, evento_id, participante_id, tipo: TipoParticipacao, horas?, status: PENDENTE|APROVADA|REJEITADA, checkinEm, certificadoEmitidoEm?)
```
- `TipoParticipacao` já existe. Reaproveite-o.
- `Participacao.horas` cobre F2; `Evento.dataInicio/dataFim` cobre F3. Alinhe com essas specs para não modelar duas vezes.
- `AlunoCertificado*` continuam sendo **modelos de emissão**. Mapeie entidade → `AlunoCertificado` em vez de anotar as classes atuais com `@Entity`, a menos que a spec decida o contrário.

## Passos típicos (fatiar no /planejar)
1. Dependências (`spring-boot-starter-data-jpa`, driver `postgresql`, `flyway-core`) e perfil de configuração com `${DB_URL}`. ⚠️ Altera `pom.xml` e properties: confirme.
2. Migração `V1__schema_inicial.sql` + entidades + repositórios + teste de repositório.
3. `FonteDadosBancoStrategy` como `@Component` com repositório injetado; `definirEstrategia` passa a escolher pela requisição (ex.: `idEvento`).
4. Endpoints CRUD de evento (protegidos pela mesma chave ou por uma autenticação nova definida em F7).
5. F6: geração de QR (link para a página pública de check-in com token do evento) → endpoint público de check-in → painel de aprovação → emissão em massa reaproveitando o pipeline (`fluxo-emissao`).

## Regras
- Endpoint **público** de check-in é superfície de ataque: exija validação de token de evento, janela de tempo e limite contra spam. Registre na spec F6.
- Nunca commite URL ou senha de banco. Use variáveis de ambiente, como os demais segredos.
- O CSV continua funcionando a cada passo (rode os testes existentes).
