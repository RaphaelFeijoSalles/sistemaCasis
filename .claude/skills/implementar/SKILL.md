---
name: implementar
description: Executa exatamente UM passo do plano de uma spec em andamento, com teste primeiro, verificação e sugestão de commit. Use quando o usuário disser "próximo passo", "implementa o P3" ou rodar /implementar.
argument-hint: "[ID da spec] [número do passo]"
disable-model-invocation: true
---

# Implementar um passo

Entrada: `$ARGUMENTS`. Se vazio, pegue a spec `em andamento` e o primeiro passo `[ ]`.

## Ciclo (não pule etapas)

1. **Anuncie** o passo: "Vou fazer P<n>: <descrição>. Arquivos: <...>." Se o passo estiver ambíguo, pare e pergunte.
2. **Carregue a skill de domínio** correspondente: `backend-spring`, `frontend-react`, `fluxo-emissao`, `banco-dados` ou `testes`.
3. **Teste primeiro** (se o passo tiver lógica): escreva o teste e rode-o para vê-lo **falhar** pelo motivo certo.
4. **Implemente o mínimo** para o teste passar. Respeite o escopo do passo e não refatore código vizinho "de brinde".
5. **Verifique:**
   - Backend: `cd backend && ./mvnw test` (ou `-Dtest=<Classe>` durante o ciclo e a suíte completa no final).
   - Frontend: `cd frontend && npm run lint && npm run build`.
   - Se algo falhar, corrija. Depois de 2 tentativas sem sucesso, pare e mostre o erro ao desenvolvedor.
6. **Atualize a spec:** marque `[x]` no passo e escreva uma linha na seção 10 (Registro), principalmente se houver desvio do plano.
7. **Mostre o diff resumido** (`git diff --stat` e as partes relevantes) e proponha a mensagem de commit do plano.
8. **Pare.** Pergunte: "Commito e sigo para o P<n+1>?" Só faça o commit com autorização.

## Regras
- ⚠️ Passos marcados como críticos: releia `.claude/memory/armadilhas.md` antes de tocar no código e mencione no resumo o que foi preservado.
- Nunca rode o backend contra credenciais reais nem chame endpoints de emissão.
- Descobriu algo não óbvio? Adicione em `armadilhas.md`.
