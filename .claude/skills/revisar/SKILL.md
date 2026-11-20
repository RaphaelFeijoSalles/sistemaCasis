---
name: revisar
description: Revisa as mudanças da branch atual contra a spec e o checklist do CASIS (regras críticas de emissão, contrato front/back, segurança, testes, convenções). Use antes de abrir PR ou fazer merge, quando o usuário pedir "revisa", "está pronto?" ou rodar /revisar.
argument-hint: "[ID da spec]"
---

# Revisar mudança

## Passos

1. Descubra a base: `git merge-base HEAD v2.0` (ou `main`, se `v2.0` não existir). Rode `git diff --stat <base>...HEAD` e leia o diff.
2. Abra a spec correspondente e confira **cada critério de aceitação**: está coberto por teste ou por verificação descrita?
3. Para revisões grandes, delegue ao subagente `revisor`, que é somente leitura.
4. Passe pelo checklist:

### Regras críticas (bloqueiam merge)
- [ ] Formato do nome do PDF (`Certificado_<Nome_Com_Underscores>.pdf`) inalterado ou com migração explícita na spec.
- [ ] Regra de pastas `Raiz/AAAA-S/Evento` inalterada.
- [ ] Nenhum teste ou código novo chama Gmail, Drive ou Sheets de verdade.
- [ ] Nenhum segredo, ID real de pasta ou planilha ou e-mail pessoal no diff.
- [ ] A verificação de `X-API-KEY` continua em todo endpoint de emissão.

### Contrato
- [ ] Campos do frontend (`FormData` ou JSON) iguais aos componentes dos records.
- [ ] Mudança de response refletida em `RelatorioEmissao.jsx` e nas mensagens de erro de `App.jsx`.
- [ ] CSV no formato antigo continua aceito, ou a quebra está documentada na spec.

### Qualidade
- [ ] Testes novos para a lógica nova; `./mvnw test` verde; `npm run lint` e `npm run build` sem erros.
- [ ] Javadoc em português nos métodos públicos novos; logs úteis (sem dados sensíveis além do já registrado hoje).
- [ ] Sem código morto, `System.out` ou TODO sem referência a spec.
- [ ] Commits no padrão Conventional Commits.

5. Entregue o resultado em três grupos: **🚫 Bloqueia**, **⚠️ Recomendo**, **💡 Opcional**, cada item com `arquivo:linha`.
6. Não corrija nada sem pedir. Pergunte quais itens o desenvolvedor quer resolver.

Dica: para uma caça a bugs mais profunda, o desenvolvedor pode rodar também `/code-review`.
