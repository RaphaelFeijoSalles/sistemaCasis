# Manual do Desenvolvedor: Vibe Coding no Sistema CASIS v2.0

Este manual explica como usar o Claude Code, com as skills, os subagentes e a memória desta pasta, para evoluir o sistema de certificados com rapidez **sem perder o controle**.

---

## 1. A metodologia em uma página

"Vibe coding" funciona bem quando a IA escreve o código e **você continua dono das decisões**. Esta estrutura combina quatro práticas consolidadas:

| Prática | O que significa aqui | Onde mora |
|---|---|---|
| **Engenharia de contexto** | A IA começa toda sessão sabendo a arquitetura, as regras críticas e onde paramos | `CLAUDE.md` + `memory/` |
| **Desenvolvimento guiado por spec** | Nada não trivial é codificado sem uma especificação com critérios de aceitação aprovada por você | `specs/` + `/nova-spec` |
| **Passos pequenos com testes como trilho** | Cada passo compila, passa nos testes e vira um commit. Se algo der errado, você volta um commit, não um dia | `/planejar` + `/implementar` |
| **Humano no circuito** | A IA para e pergunta nas ambiguidades e pede autorização para commitar | regra nº 1 do `CLAUDE.md` |

### O ciclo

```
/iniciar-sessao ─► /nova-spec ─► /planejar ─► /implementar (repete por passo) ─► /revisar ─► merge ─► /encerrar-sessao
      ▲                                                                                                     │
      └──────────────────────────────── memória (handoff, progresso, decisões) ◄─────────────────────────────┘
```

---

## 2. O que tem nesta pasta

```
.claude/
├── CLAUDE.md              ← contexto carregado AUTOMATICAMENTE em toda sessão (regras, stack, arquitetura, comandos)
├── MANUAL.md              ← este arquivo
├── settings.json          ← permissões do projeto (bloqueia leitura de secrets.properties e .env)
├── memory/                ← memória entre sessões (importada pelo CLAUDE.md)
│   ├── handoff.md         ← "onde paramos / próximo passo" (sobrescrito a cada sessão)
│   ├── progresso.md       ← roadmap F1..F7 com status e pontos em aberto
│   ├── decisoes.md        ← registro de decisões (por que sem LangChain, por que validação em Java...)
│   └── armadilhas.md      ← comportamentos não óbvios do código (anti-duplicação, contrato, falhas silenciosas)
├── specs/
│   └── _TEMPLATE.md       ← modelo de especificação
├── skills/                ← comandos /nome
│   ├── iniciar-sessao/    ← fluxo
│   ├── nova-spec/
│   ├── planejar/
│   ├── implementar/
│   ├── revisar/
│   ├── encerrar-sessao/
│   ├── backend-spring/    ← conhecimento de domínio (carregadas automaticamente quando relevantes)
│   ├── frontend-react/
│   ├── fluxo-emissao/
│   ├── testes/
│   └── banco-dados/
└── agents/                ← subagentes especialistas
    ├── dev-backend.md
    ├── dev-frontend.md
    ├── testador.md
    └── revisor.md
```

**Versionamento:** tudo aqui é commitado no seu fork, **exceto** `.claude/settings.local.json` (preferências pessoais, ignorado via `.git/info/exclude`).

---

## 3. Preparação (uma vez)

1. **Fork e branch:**
   ```powershell
   git remote rename origin upstream
   git remote add origin https://github.com/<SEU_USUARIO>/sistemaCasis.git
   git switch -c v2.0
   git push -u origin v2.0
   ```
2. **Credenciais de TESTE.** Configure `backend/src/main/resources/secrets.properties` com uma conta Google sua (Perfil B do README). Com credenciais oficiais, qualquer emissão manda e-mail para alunos reais e escreve no Drive do CASIS.
3. **Primeiro commit da estrutura:**
   ```powershell
   git add .claude
   git commit -m "chore(claude): estrutura de vibe coding para a v2.0"
   ```
4. Abra o Claude Code na raiz do projeto e rode `/iniciar-sessao`.

---

## 4. As skills, uma a uma

### Fluxo (você chama)

| Comando | Quando usar | O que você recebe | O que a IA **não** faz |
|---|---|---|---|
| `/iniciar-sessao` | Primeira coisa de toda sessão | Resumo: onde paramos, branch, próximo passo | Não começa a codar |
| `/nova-spec F2` | Antes de qualquer feature ou mudança não trivial | Perguntas sobre os pontos em aberto, depois `specs/F2-<slug>.md` em rascunho | Não escreve código; não aprova sozinha |
| `/planejar F2` | Spec aprovada | Plano de passos pequenos (P1, P2...) na seção 9 da spec | Não implementa |
| `/implementar` | Um passo por vez | Teste falhando → código → testes verdes → diff → mensagem de commit proposta | Não commita sem seu "sim"; não faz dois passos |
| `/revisar F2` | Antes do merge ou PR | Achados em 🚫 / ⚠️ / 💡 com `arquivo:linha` e cobertura dos CAs | Não corrige sem pedir |
| `/encerrar-sessao` | Fim de toda sessão | Memória atualizada e commit `chore(claude)` proposto | Não descarta mudanças |

### Domínio (carregadas automaticamente)

Você não precisa chamá-las: o Claude as carrega quando a tarefa toca a área. Pode citá-las para reforçar ("siga a skill fluxo-emissao").

| Skill | Cobre |
|---|---|
| `backend-spring` | Onde colocar cada classe, padrões de DTO, logs, Javadoc, como mudar um contrato |
| `frontend-react` | Estado em `App.jsx`, mensagens `{titulo, detalhe}`, nomes de campos, etapas de fluxo |
| `fluxo-emissao` | **Invariantes** (nome do PDF, pastas, colunas do Sheets), o princípio "validar ≠ executar", onde mexer em F1–F4 |
| `testes` | Como mockar Google, modelos de teste, propriedades falsas para subir o contexto |
| `banco-dados` | Decisões obrigatórias da F5, modelo sugerido, fatiamento, segurança do check-in público (F6) |

---

## 5. Subagentes: quando delegar

Subagentes trabalham em contexto separado e devolvem só o resultado. Vale a pena quando:
- o passo é bem definido e isolado (só backend ou só frontend);
- você quer uma revisão "com olhos frescos";
- você quer rodar trabalhos independentes **em paralelo** (ex.: testador cobrindo F1 enquanto o dev-frontend faz a tela).

| Agente | Escreve em | Uso típico |
|---|---|---|
| `dev-backend` | `backend/` | "Use o dev-backend para executar o P2 da spec F1" |
| `dev-frontend` | `frontend/` | "Use o dev-frontend para o P5 da F1" |
| `testador` | só `backend/src/test/` | "Use o testador para cobrir os CAs da F2" |
| `revisor` | nada (somente leitura) | "Use o revisor na branch feature/F1-validacao" |

Para tarefas pequenas, **não delegue**: fazer direto na conversa é mais rápido e mais barato.

> **Por que não LangChain?** Veja `memory/decisoes.md` (D-01). Resumo: para um projeto deste porte, construir e manter um orquestrador próprio custaria mais tempo do que economizaria. Os subagentes acima cobrem a necessidade.

---

## 6. Como escrever bons prompts

A IA já conhece o projeto pelo `CLAUDE.md`. Seu prompt precisa trazer **o que mudou na sua cabeça**. Use esta estrutura:

```
[OBJETIVO]     O que quero alcançar e por quê (1–2 frases).
[CONTEXTO]     Spec/passo, arquivos ou comportamento relevante, exemplo real (anonimizado).
[RESTRIÇÕES]   O que não pode mudar, compatibilidade, o que está fora do escopo.
[PRONTO QUANDO] Critério objetivo: teste X passa, tela mostra Y, CSV Z é aceito.
[FORMATO]      O que quero de volta: plano, diff, só perguntas, tabela...
```

### Modelos prontos

**Ideia nova (antes de /nova-spec):**
```
/nova-spec Quero que a diretoria veja uma prévia do lote antes de disparar.
Hoje, se o CSV tiver e-mail errado, só descobrimos depois do envio.
Restrição: o CSV atual tem que continuar funcionando.
Pronto quando: a tela mostra válidos, inválidos e já emitidos, e só envia após confirmar.
```

**Bug:**
```
Bug: CSV exportado do Google Forms com coluna "Endereço de e-mail" e "Nome completo"
dá "Cabeçalho inválido". Exemplo de cabeçalho (sem dados reais): <cole aqui>.
Antes de corrigir: escreva um teste em CsvServiceTest que reproduza, rode e me mostre falhando.
Não mude a regra de nome do PDF.
```

**Entender antes de mudar:**
```
Sem alterar nada: explique como a anti-duplicação funciona do CSV até o Drive,
com arquivo:linha, e liste o que quebraria se o nome do evento tiver barra "/".
```

**Refatoração segura:**
```
Refatoração sem mudança de comportamento: centralize a montagem do nome do PDF
(hoje duplicada em EmissaoService) em um único método testado.
Pronto quando: testes existentes + novo teste do método passam; diff mínimo.
```

### Boas práticas
- **Uma intenção por mensagem.** "Faça F2 e já arrume o CSS" gera um diff que ninguém revisa.
- **Mostre exemplos** de CSV ou de entrada, anonimizados. Valem mais que parágrafos.
- **Peça o plano antes do código** quando estiver inseguro: "só o plano, não implemente".
- **Diga o que não mudar.** A IA tende a "melhorar" código vizinho.
- **Corrija cedo.** Se o primeiro passo saiu torto, pare e corrija a spec ou o plano, em vez de remendar.
- **Quando a conversa ficar longa** ou mudar de assunto, rode `/encerrar-sessao`, depois `/clear` e `/iniciar-sessao`. Contexto limpo dá respostas melhores.

### Antipadrões
| Evite | Porque | Faça |
|---|---|---|
| "Implementa a v2.0" | Grande demais, decisões implícitas | `/nova-spec F1` |
| Aceitar diff sem ler | Bugs na anti-duplicação mandam e-mails duplicados | Leia o diff de cada passo; use `/revisar` |
| Testar emitindo de verdade | Efeito real e irreversível (e-mails) | Testes com mocks; credenciais de teste |
| Pular `/encerrar-sessao` | A próxima sessão começa às cegas | Sempre encerrar |
| Colar dados reais de alunos | Privacidade (LGPD) | Dados fictícios |

---

## 7. Exemplo completo: F1 (validação antes do disparo)

1. `/iniciar-sessao` → "Próximo passo sugerido: /nova-spec F1".
2. `/nova-spec F1` → a IA pergunta, por exemplo: a prévia consulta o Drive para marcar "já emitidos"? Como a confirmação volta ao backend (reenviar o CSV ou um token)? Duplicados no próprio CSV bloqueiam ou só avisam? Você responde e ela gera `specs/F1-validacao-pre-disparo.md`.
3. Você lê, ajusta e diz "aprovo".
4. `/planejar F1` → plano como:
   - P1 `ValidadorParticipantes` puro + testes (e-mail, vazio, duplicado)
   - P2 endpoint `POST /pre-visualizar-lote` sem efeitos + teste de controller
   - P3 ⚠️ centralizar o nome do PDF (usado na prévia e na emissão)
   - P4 emissão aceita só a lista confirmada
   - P5 frontend: etapa "prévia" com tabela de válidos e inválidos
   - P6 texto de ajuda atualizado
5. `git switch -c feature/F1-validacao-pre-disparo`
6. `/implementar` → P1 → você lê o diff → "commita e segue" → `/implementar` → P2 ...
7. Opcional: "use o testador para cobrir todos os CAs da F1".
8. `/revisar F1` → resolve os 🚫 → merge na `v2.0`.
9. `/encerrar-sessao`.

---

## 8. Manutenção da memória

- **`handoff.md`**: sempre via `/encerrar-sessao`.
- **`progresso.md`**: o status muda nas skills `/nova-spec` e `/planejar` e ao concluir. Ideia nova? Adicione uma linha como `backlog`.
- **`decisoes.md`**: só acrescente; nunca apague. Mudou de ideia? Nova entrada "Substitui D-xx".
- **`armadilhas.md`**: toda vez que algo custar mais de 15 minutos para entender.
- **`CLAUDE.md`**: mantenha enxuto. Mudou a stack, os comandos ou a arquitetura? Atualize. Detalhe de feature vai na spec, não aqui.
- Peça a qualquer momento: "lembre que..." e a IA registra no arquivo certo.

---

## 9. Problemas comuns

| Sintoma | Causa provável | Solução |
|---|---|---|
| Skill não aparece no `/` | Pasta ou `SKILL.md` com nome errado, ou Claude Code aberto fora da raiz | Abra na raiz do repositório; confira `.claude/skills/<nome>/SKILL.md` |
| `contextLoads` falha | Falta `secrets.properties` | Crie a partir do `.example` (credenciais de teste) ou use `@TestPropertySource` nos testes novos |
| A IA "esqueceu" a decisão de ontem | Sessão anterior sem `/encerrar-sessao` | Conte a decisão e peça para registrar em `decisoes.md` |
| Diff enorme num passo | Plano mal fatiado | Peça para parar, rode `git restore` nos arquivos e refaça `/planejar` com passos menores |
| A IA quer ler `secrets.properties` | — | É bloqueado em `settings.json`. Não libere |
