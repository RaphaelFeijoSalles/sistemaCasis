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
1. Desenvolvedor sobe backend + frontend em localhost (credenciais de **teste**) e faz a conferência visual abaixo.
2. Depois: `/nova-spec F3` (eventos longos), começando pelos pontos em aberto de `progresso.md`.

## Pendências / bloqueios
- ~~Conferência visual depois de `23bd293`~~: feita pelo desenvolvedor em localhost (2026-11-26), tudo correto.
- Deploy da F2 na produção oficial (Render antes da Vercel): não confirmado.
- **Deploy próprio adiado** (2026-11-26, decisão do desenvolvedor: testes só em localhost por enquanto). Quando retomar: backend no Render (conta própria, Docker, root `backend`, `SPRING_PROFILES_ACTIVE=prod`, `PORT=8080`, variáveis do `application-prod.properties`; a pasta raiz é `DRIVE_PASTA_RAIZ_ID`) e frontend na Vercel (root `frontend`, `VITE_API_URL` sem barra final, redeploy ao mudar).
- **CORS bloqueia o deploy próprio (só quando ele for retomado):** `CertificadoController` tem `@CrossOrigin` fixo em `https://sistema-casis.vercel.app/` e `http://localhost:5173`. Um frontend em outra URL da Vercel é recusado pelo navegador. Precisa de mudança de código (ex.: origens vindas de propriedade/variável de ambiente), a combinar com o desenvolvedor.
