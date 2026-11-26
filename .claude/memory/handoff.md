# Handoff — estado da última sessão

> Sobrescrito a cada `/encerrar-sessao`. Lido no início de toda sessão.

**Data:** 2026-11-26
**Branch:** `feature/F3-eventos-longos` (local, saiu de `main` do fork em `4f51ad9`, sem push)
**Remotos:** `origin` = fork (GuilhermeSMathias/EmissorCertificadoDacomp), `upstream` = original
**Spec em andamento:** F3 (eventos longos), ainda não escrita.

## O que foi feito
- F1 mergeada em `main` do fork (PR #1, `b8ec006`). D-05: features saem de `main` do fork, sem `v2.0`.
- F2 (horas por participante) mergeada em `main` do fork (PR #2, `4f51ad9`), aberto manualmente pela web. 123 testes verdes; `contextLoads` verde.
- `main` local sincronizada com `origin/main`; branch da F3 criada.

## Próximo passo
1. `/nova-spec F3` (eventos longos), começando pelos pontos em aberto de `progresso.md`.

## Pendências / bloqueios
- Deploy da F2: backend (Render) antes do frontend (Vercel). Confirmar com o desenvolvedor se já foi feito.
- Conferência visual depois de `23bd293`: o campo Carga Horária deve aceitar `1.25` e recusar `0` e `1001` com toast; conferir também a ajuda lateral. Confirmar com o desenvolvedor.
