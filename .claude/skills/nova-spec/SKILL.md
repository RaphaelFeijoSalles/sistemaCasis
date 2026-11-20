---
name: nova-spec
description: Transforma uma ideia ou feature do roadmap (F1..F7) em uma especificação verificável em .claude/specs/, fazendo perguntas até eliminar ambiguidades. Use antes de qualquer mudança não trivial, ou quando o usuário pedir "nova feature", "especificar", "spec" ou rodar /nova-spec.
argument-hint: "<ID do roadmap ou descrição da ideia>"
---

# Nova spec (spec-driven development)

Entrada: `$ARGUMENTS` (ex.: `F2`, ou "permitir horas diferentes por aluno").

## Passos

1. **Localize a feature** em `.claude/memory/progresso.md`. Se for uma ideia nova, proponha um ID novo (F8, F9...).
2. **Explore antes de escrever.** Leia os arquivos que a feature toca. Use o fluxo em `.claude/CLAUDE.md` e consulte `.claude/memory/armadilhas.md`. Anote quais regras críticas são afetadas.
3. **Entreviste o desenvolvedor.** Faça de 3 a 6 perguntas objetivas, de preferência com opções e uma recomendação, cobrindo:
   - comportamento esperado do ponto de vista da diretoria do CASIS;
   - formato dos dados (colunas de CSV, campos, formatos de data e hora);
   - compatibilidade com o fluxo atual (CSV antigo, frontend em produção);
   - o que fica fora do escopo.
   Comece pelos "Pontos em aberto" já listados no progresso.md para essa feature.
4. **Escreva a spec** copiando `.claude/specs/_TEMPLATE.md` para `.claude/specs/<ID>-<slug>.md`, com `status: rascunho`.
   - Critérios de aceitação no formato **Dado / Quando / Então**, cada um testável.
   - Preencha "Contrato" com rotas, records e campos **exatos**.
   - Deixe a seção 9 (Plano) vazia.
5. **Atualize** `progresso.md`: status `spec` e link para o arquivo.
6. **Apresente a spec** e pergunte: "Aprova a spec ou quer ajustar algo?" Só mude para `status: aprovada` com um "sim" explícito.

## Regras
- Não escreva código nesta skill.
- Se uma resposta abrir nova dúvida, pergunte de novo. Não preencha lacunas por conta própria.
- Spec boa cabe em uma tela. Se passar disso, proponha dividir em duas features.
