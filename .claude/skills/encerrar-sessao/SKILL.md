---
name: encerrar-sessao
description: Salva o contexto da sessão na memória do projeto (handoff, progresso, decisões, armadilhas) para a próxima sessão continuar sem perda. Use quando o usuário disser "vamos parar", "fim por hoje", "salva o contexto" ou rodar /encerrar-sessao.
disable-model-invocation: true
---

# Encerrar sessão

## Passos

1. Levante o estado: `git status`, `git branch --show-current`, `git log --oneline -10` e a spec em andamento.
2. **Sobrescreva** `.claude/memory/handoff.md` com:
   - data de hoje, branch, spec e passo atual;
   - "O que foi feito" (3 a 6 bullets objetivos, com commits);
   - "Próximo passo" (concreto o bastante para começar sem pensar);
   - "Pendências / bloqueios" (inclua perguntas que ficaram sem resposta).
3. Atualize `.claude/memory/progresso.md` se algum status mudou.
4. Se uma decisão de arquitetura ou processo foi tomada, **acrescente** uma entrada em `decisoes.md` (D-xx).
5. Se algo não óbvio foi descoberto, acrescente em `armadilhas.md`.
6. Se houver alterações não commitadas, liste-as e pergunte: commitar (WIP), guardar em stash ou deixar como está?
7. Mostre o diff da memória e proponha o commit: `chore(claude): atualiza memória da sessão`.

## Regras
- Memória é fato, não diário: corte o que não ajuda a próxima sessão.
- O handoff deve ser lido em menos de 30 segundos.
