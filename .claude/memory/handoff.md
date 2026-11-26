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
2. Deploy próprio para teste fora do localhost: backend no Render (conta própria, Docker, `SPRING_PROFILES_ACTIVE=prod`) e frontend na Vercel (conta própria, root `frontend`, `VITE_API_URL`). **Bloqueio:** CORS fixo no controller (ver abaixo).
3. Depois: `/nova-spec F3` (eventos longos), começando pelos pontos em aberto de `progresso.md`.

## Pendências / bloqueios
- Conferência visual depois de `23bd293`: o campo Carga Horária deve aceitar `1.25` e recusar `0` e `1001` com toast; conferir também a ajuda lateral. Pendente (desenvolvedor vai testar em localhost).
- Deploy da F2 na produção oficial (Render antes da Vercel): não confirmado.
- **CORS bloqueia o deploy próprio:** `CertificadoController` tem `@CrossOrigin` fixo em `https://sistema-casis.vercel.app/` e `http://localhost:5173`. Um frontend em outra URL da Vercel é recusado pelo navegador. Precisa de mudança de código (ex.: origens vindas de propriedade/variável de ambiente), a combinar com o desenvolvedor.
