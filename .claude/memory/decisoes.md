# Registro de decisões (ADR resumido)

> Uma entrada por decisão de arquitetura ou processo. Nunca apague entradas: marque como "Substituída por D-xx".

## D-01 — Sem LangChain; usar skills e subagentes nativos do Claude Code (2026-11-19)
- **Contexto:** pensou-se em orquestrar agentes via API com LangChain para acelerar a v2.0.
- **Decisão:** não criar a infraestrutura LangChain. Usar `.claude/skills/` (fluxo) e `.claude/agents/` (especialistas).
- **Motivo:** o monorepo é pequeno (~30 arquivos) e tem um desenvolvedor. LangChain exigiria construir e manter ferramentas próprias (ler, editar e testar arquivos), lidar com custo de API e revisar saídas sem checkpoints. O Claude Code já fornece tudo isso.
- **Revisitar se:** surgir a necessidade de automação sem humano (CI que gera PRs) ou de rodar agentes em massa sobre muitos repositórios.

## D-02 — Validação de dados em Java, não pandas (2026-11-19)
- **Contexto:** pediu-se validação "com pandas" antes de gerar os PDFs.
- **Decisão:** implementar a validação equivalente em Java/Spring, dentro da etapa de pré-visualização (F1).
- **Motivo:** evita uma segunda linguagem, um segundo deploy no Render e um contrato extra entre serviços.

## D-03 — Spec-driven + memória versionada em `.claude/` (2026-11-19)
- **Decisão:** toda feature passa pelo ciclo spec → plano → passos pequenos → revisão → commit. Memória e specs são commitadas no fork. Configurações pessoais ficam em `.claude/settings.local.json` (ignorado via `.git/info/exclude`).
