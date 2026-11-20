---
name: planejar
description: Quebra uma spec aprovada em passos pequenos, ordenados e testáveis (cada um vira um commit) e escreve o plano na seção 9 da spec. Use depois de /nova-spec, quando o usuário pedir "planejar", "quebrar em tarefas" ou rodar /planejar.
argument-hint: "<arquivo ou ID da spec>"
---

# Planejar implementação

Entrada: `$ARGUMENTS` (ex.: `F2` ou `.claude/specs/F2-horas-por-participante.md`).

## Pré-condição
A spec tem `status: aprovada`. Se não tiver, pare e sugira `/nova-spec`.

## Passos

1. Releia a spec e **todos** os arquivos da seção "Impacto no código".
2. Monte o plano seguindo estas regras de fatiamento:
   - **De dentro para fora:** modelo, DTO e utilitário puro → serviço → controller → frontend.
   - **Um passo = uma mudança coesa** que compila e passa nos testes sozinha. Em geral, de 1 a 3 arquivos.
   - **Teste primeiro** quando houver lógica (parser, validação, formatação). O passo diz qual teste será escrito.
   - **Contrato antes da UI:** endpoint e DTO prontos e testados antes de mexer no React.
   - **Compatibilidade:** se o contrato mudar, inclua um passo para manter o formato antigo funcionando ou um passo explícito de migração.
   - Passos que tocam regras críticas (anti-duplicação, pastas do Drive, envio de e-mail) ficam isolados e marcados com ⚠️.
3. Escreva na seção 9 da spec:
   ```
   - [ ] P1 — <o que muda> · arquivos: <...> · teste: <nome do teste ou verificação> · commit: `tipo(escopo): mensagem`
   ```
4. Sugira qual subagente executa cada passo (`dev-backend`, `dev-frontend`, `testador`) quando fizer sentido.
5. Mostre o plano e pergunte se aprova. Depois do "sim", mude a spec para `status: em andamento` e atualize `progresso.md`.
6. Sugira criar a branch: `git switch -c feature/<ID>-<slug>` (a partir de `v2.0`).

## Regras
- Não implemente nada aqui.
- Um plano com mais de ~10 passos indica uma spec grande demais. Proponha dividir.
