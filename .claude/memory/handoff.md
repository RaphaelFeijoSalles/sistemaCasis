# Handoff — estado da última sessão

> Sobrescrito a cada `/encerrar-sessao`. Lido no início de toda sessão.

**Data:** 2026-11-26
**Branch:** F4 mergeada em `main` do fork (PR #3, `0321ca0`). Próxima feature ainda sem branch.
**Remotos:** `origin` = fork (GuilhermeSMathias/EmissorCertificadoDacomp), `upstream` = original
**Spec em andamento:** nenhuma.

## O que foi feito
- F4 (gestão configurável DACOMP + curso por participante) concluída: P1–P11, de `4edd5de` a `e5e1ec7`. 177 testes verdes; `contextLoads` verde (rodado pelo desenvolvedor).
- Teste manual em localhost com credenciais de teste: e-mails, PDFs e Sheets corretos (CSVs de teste com plus-address do desenvolvedor, fora do repo).
- `/revisar` sem bloqueios; item 1 corrigido (`a3f99ab`), itens 2–5 no "Backlog da F7" de `progresso.md` (`4933b24`).

## Próximo passo
1. `git switch main && git pull` (sincronizar com o merge da F4).
2. Escolher a próxima feature com o desenvolvedor: **F3** (eventos longos, ordem sugerida) ou **F8** (RA obrigatório, pequena, depende da F4). Rodar `/nova-spec <ID>` começando pelos pontos em aberto de `progresso.md`.

## Pendências / bloqueios
- **Deploy da F4 (e da F2) na produção oficial** não confirmado. Ordem: Render antes da Vercel. Dados da gestão podem ser sobrescritos por `CERTIFICADO_*` no Render.
- **Deploy próprio adiado** (testes só em localhost). Quando retomar: backend no Render (Docker, root `backend`, `SPRING_PROFILES_ACTIVE=prod`, `PORT=8080`, variáveis do `application-prod.properties`; pasta raiz = `DRIVE_PASTA_RAIZ_ID`) e frontend na Vercel (root `frontend`, `VITE_API_URL` sem barra final).
- **CORS bloqueia o deploy próprio:** `@CrossOrigin` fixo em `https://sistema-casis.vercel.app/` e `http://localhost:5173` no `CertificadoController`. Precisa de origens configuráveis (a combinar).
