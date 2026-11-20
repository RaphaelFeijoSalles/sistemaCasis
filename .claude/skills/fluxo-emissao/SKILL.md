---
name: fluxo-emissao
description: Regras críticas e guia de extensão do pipeline de emissão de certificados (CSV → anti-duplicação no Drive → PDF → e-mail → upload → Sheets). Use SEMPRE que a mudança tocar EmissaoService, CsvService, PdfService, EmailService, GoogleDriveService, GoogleSheetsService, os templates Thymeleaf ou as features F1–F4.
---

# Fluxo de emissão — regras e extensões

Leia primeiro `.claude/memory/armadilhas.md`, seção "Críticas".

## Invariantes (não quebre sem uma spec que diga como migrar)
1. **Nome do PDF** = `"Certificado_" + nome.trim().replaceAll("\\s+", "_") + ".pdf"`. É a chave da anti-duplicação. Se for preciso mudar, centralize em **um único método** antes de qualquer outra coisa.
2. **Pasta** = `Raiz / AAAA-S / NomeEvento`, com S = 1 para meses 1 a 6 e 2 para 7 a 12.
3. **Ordem dos efeitos:** verificar duplicado → gerar PDF → enviar e-mail → upload → Sheets. Um efeito só ocorre se o anterior não lançou exceção (exceto upload e Sheets, que hoje falham em silêncio).
4. **Colunas do Sheets:** `nome | ra | curso | data_evento | evento | carga_horaria`. Mudar a ordem corrompe a planilha de controle existente.
5. **Relatório:** uma `ResultadoEmissaoDTO` por participante, com status `SUCESSO`, `EXISTENTE` ou `ERRO`.

## Princípio para as features da v2.0: separar "validar" de "executar"
Hoje o lote valida e executa na mesma chamada. Para F1 (validação antes do disparo) e para todas as features seguintes:
- **Validar sem efeitos colaterais:** ler o CSV, normalizar e validar, e devolver uma pré-visualização (linhas válidas, avisos, erros e quais já existem no Drive). **Nunca** gerar PDF, enviar e-mail ou escrever no Drive ou no Sheets nessa etapa. Ler a lista de arquivos do Drive é aceitável, mas a spec deve decidir isso.
- **Executar só depois da confirmação explícita** da diretoria.
- A spec de F1 decide **como** a confirmação chega ao backend (reenvio do CSV, token de pré-visualização ou lista confirmada). Não decida sozinho: pergunte.
- A validação fica em uma classe pura (ex.: `ValidadorParticipantes`) com testes unitários: e-mail inválido, nome vazio, duplicado no próprio CSV (mesmo e-mail ou mesmo nome → mesmo PDF!), horas inválidas (F2).

## Pontos de extensão por feature
| Feature | Onde mexer | Atenção |
|---|---|---|
| F2 horas por participante | `CsvService.HeaderMap` (+ coluna horas), `AlunoCertificadoEvento` (campo horas), `PdfService` / `GoogleSheetsService` usam a hora do aluno com fallback para a do evento | Formato de hora e regra de fallback vêm da spec |
| F3 início/fim | DTOs (`dataInicio`/`dataFim` ou manter `dataRealizacao` + `dataFim` opcional), `PdfService` (texto do período), `GoogleDriveService` (qual data define o semestre) | Compatibilidade com o frontend que envia só `dataRealizacao` |
| F4 curso no certificado | `PdfService` (variável `curso`), `certificado_evento.html`, DTO e formulário; `GoogleSheetsService` já recebe `curso` (hoje sempre `null` → "Sistemas de Informação") | O significado exato de "modelo" está em aberto em `progresso.md` |
| Templates | `resources/templates/*.html` + `static/images/` | Flying Saucer suporta CSS 2.1, sem flexbox ou grid. Valide o layout gerando PDF em teste local com dados falsos |

## Testar o fluxo sem efeitos reais
Use a skill `testes`: `EmissaoService` com `@Mock` de `PdfService`, `EmailService`, `GoogleDriveService` e `GoogleSheetsService`, verificando chamadas com `verify(...)` e `never()`.
