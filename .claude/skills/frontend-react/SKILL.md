---
name: frontend-react
description: Padrões do frontend React 19 + Vite do CASIS (estado em App.jsx, validação, toasts de erro, chamadas axios, CSS puro). Use ao criar ou alterar arquivos em frontend/src.
---

# Frontend React — padrões do projeto

## Mapa
- `App.jsx`: estado global (`formData`, `arquivoCsv`, `apiKey`, `emissaoTipo`, `resultados`), `validarFormulario`, `montarMensagemErro`, chamadas axios e escolha entre formulário e relatório.
- `components/CertificadoForm.jsx`: campos, toggle lote/unitário, dropzone de CSV, prévia da carga horária.
- `components/RelatorioEmissao.jsx`: contadores e tabela por status (`SUCESSO`, `EXISTENTE`, `ERRO`).
- `components/AjudaEmissao.jsx` + `.css`: guia lateral (desktop) ou modal (mobile, ≤ 850px).
- `App.css` / `index.css`: design system CASIS em CSS puro.

## Regras
- **Mensagens ao usuário** seguem `{ titulo, detalhe }` e são exibidas com `toast.error(<ToastMensagem .../>)`. O texto é em português, explica o que fazer e não fala só do erro.
- **Validação no cliente** fica em funções puras no topo de `App.jsx` (ou em um módulo `src/validacao.js`, se crescer), retornando `null` ou `{ titulo, detalhe }`.
- **Toda validação do cliente tem equivalente no backend.** O cliente é só conveniência.
- **Nomes de campos** enviados (`FormData` / JSON) são **idênticos** aos componentes dos records Java.
- **URL da API** sempre via `import.meta.env.VITE_API_URL`. Nunca fixe a URL no código.
- **Ícones:** `lucide-react`. Notificações: `react-hot-toast`. Não adicione bibliotecas de UI ou CSS sem aprovação na spec.
- **Novo fluxo com etapas** (ex.: pré-visualização → confirmação em F1): adicione um estado explícito (ex.: `etapa: 'formulario' | 'previa' | 'relatorio'`) em vez de vários booleanos.
- **Ajuda contextual:** mudou o formato do CSV ou um campo? Atualize o texto de `AjudaEmissao.jsx` e as dicas do formulário no mesmo passo.
- Componentes vazios existentes (`DropzoneCsv.jsx`) podem ser usados se a spec decidir extrair a dropzone. Não apague sem perguntar.

## Verificação
`cd frontend && npm run lint && npm run build`. Para testar visualmente: `npm run dev` (porta 5173), **sem** um backend com credenciais reais por trás.
