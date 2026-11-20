# Handoff — estado da última sessão

> Sobrescrito a cada `/encerrar-sessao`. Lido no início de toda sessão.

**Data:** 2026-11-19
**Branch:** main (clone do repositório original; o fork ainda não foi configurado)
**Spec em andamento:** nenhuma

## O que foi feito
- Criada a infraestrutura de vibe coding em `.claude/` (CLAUDE.md, memória, specs, skills, agentes, manual).
- Nenhum código ou documentação do projeto foi alterado.

## Próximo passo
1. Fazer o fork no GitHub, renomear `origin` para `upstream` e criar a branch `v2.0`.
2. Trocar `secrets.properties` por credenciais de **teste** (Perfil B do README).
3. Rodar `/nova-spec F1` (validação pré-disparo), que é a base das demais features.

## Pendências / bloqueios
- `secrets.properties` local parece conter credenciais oficiais do CASIS e `GOOGLE_SHEETS_PLANILHA_ID` inclui `/edit?gid=...` (deveria ser só o ID).
