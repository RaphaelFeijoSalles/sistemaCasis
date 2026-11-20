---
name: iniciar-sessao
description: Carrega o contexto do projeto CASIS no início de uma sessão (handoff, roadmap, git) e combina o objetivo da sessão com o desenvolvedor. Use quando o usuário disser "vamos começar", "onde paramos", "retomar" ou rodar /iniciar-sessao.
---

# Iniciar sessão

Objetivo: em menos de um minuto, deixar claro **onde paramos** e **o que faremos agora**.

## Passos

1. Leia `.claude/memory/handoff.md` e `.claude/memory/progresso.md`. Eles já estão importados no CLAUDE.md, mas releia se a sessão for longa.
2. Rode `git status`, `git branch --show-current` e `git log --oneline -5`.
3. Se houver spec "em andamento", abra-a em `.claude/specs/` e identifique o primeiro passo `[ ]` do plano.
4. Verifique os alertas:
   - Branch atual é `main`? Avise: trabalho da v2.0 deve ficar em `v2.0` ou `feature/*`.
   - Mudanças não commitadas que não vieram do handoff? Pergunte o que são antes de seguir.
5. Apresente um resumo **curto** neste formato:

```
📍 Onde paramos: <1 frase>
🌿 Branch: <nome> | alterações pendentes: <sim/não>
▶️ Próximo passo sugerido: <passo do plano ou do handoff>
❓ Qual o objetivo desta sessão?
```

6. **Pare e espere** o desenvolvedor confirmar ou redefinir o objetivo. Não comece a implementar.

## Regras
- Não leia `secrets.properties` nem `.env`.
- Se o handoff contradisser o estado do git, mostre a divergência e pergunte.
