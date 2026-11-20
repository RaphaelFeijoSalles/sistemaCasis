---
id: FX
titulo: <título curto>
status: rascunho        # rascunho | aprovada | em andamento | concluída
branch: feature/FX-<slug>
criada: AAAA-MM-DD
---

# FX — <título>

## 1. Problema
Qual dor do usuário (diretoria do CASIS ou participante) isto resolve? 2 a 4 frases.

## 2. Comportamento esperado
Descreva o fluxo do ponto de vista de quem usa, passo a passo.

## 3. Critérios de aceitação
Cada item deve ser verificável por teste ou por observação objetiva.
- [ ] CA1: Dado ..., quando ..., então ...
- [ ] CA2: ...

## 4. Fora do escopo
O que **não** será feito agora (evita que a feature cresça sem controle).

## 5. Contrato
- **Endpoints novos ou alterados:** método, rota, request, response, códigos de erro
- **DTOs / records:**
- **Formato de CSV / template:**
- **Compatibilidade:** o frontend atual continua funcionando? O formato antigo de CSV continua aceito?

## 6. Impacto no código
| Arquivo | Mudança |
|---|---|
| `backend/.../X.java` | ... |

## 7. Riscos e regras críticas tocadas
Consulte `.claude/memory/armadilhas.md`. Anti-duplicação? Pastas do Drive? Efeitos reais?

## 8. Perguntas em aberto
Tudo que precisa de resposta do dono do projeto **antes** de aprovar.
- [ ] P1:

## 9. Plano de implementação
Preenchido por `/planejar`. Cada passo é pequeno, testável e vira um commit.
- [ ] Passo 1 — <descrição> · teste: <qual> · commit: `feat(...): ...`

## 10. Registro de execução
Notas curtas do que aconteceu em cada passo (desvios, descobertas).
