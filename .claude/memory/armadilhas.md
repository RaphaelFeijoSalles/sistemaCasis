# Armadilhas e comportamentos não óbvios

> Leia antes de mexer no fluxo de emissão. Adicione aqui tudo que custou tempo para descobrir.

## Críticas (quebram produção ou geram efeito real)
- **Efeitos reais:** cada emissão manda e-mail (Gmail API), cria pastas e arquivos no Drive e adiciona linhas no Sheets. Não há ambiente "sandbox" no código. A separação depende só das credenciais em `secrets.properties`.
- **Anti-duplicação depende do nome do arquivo:** `"Certificado_" + nome.trim().replaceAll("\\s+", "_") + ".pdf"`, montado em **dois lugares** de `EmissaoService` (lote e unitário). Mudar esse formato faz o sistema reemitir certificados já enviados.
- **O anexo do e-mail usa outra fórmula** (`EmailService`: sem `trim()`). Hoje não afeta a anti-duplicação, mas não confie que as duas sejam iguais.
- **Pastas do Drive:** `Raiz/AAAA-S/NomeEvento`, buscadas por nome exato. Semestre 1 = meses 1 a 6. Renomear a pasta no Drive ou mudar a regra cria pastas duplicadas.
- **Evento longo (F3):** a pasta segue a **data de início** (`dataRealizacao`), nunca a `dataFim`. Um evento de 28/06 a 03/07 fica em `AAAA-1`. Testes em `EmissaoServiceTest` protegem a regra.
- **Período errado não se corrige reemitindo (F3):** a anti-duplicação só olha o nome do arquivo. Reemitir com a `dataFim` corrigida devolve EXISTENTE para todos e os PDFs já enviados ficam com o período errado. Com a **data de início** errada é pior: o evento cai em outra pasta (se o semestre mudar) e a reemissão envia e-mail de novo para todos. Correção manual: apagar os PDFs da pasta do evento no Drive (e as linhas no Sheets) antes de reemitir. O guia lateral avisa a diretoria.
- **Falhas silenciosas:** `fazerUploadCertificado` e `registrarEmissao` engolem exceções. O participante pode receber o e-mail sem backup no Drive. Nesse caso, a próxima emissão envia o e-mail de novo.
- **`listarArquivosNaPasta` retorna um conjunto vazio em caso de erro** e não pagina (a API do Drive devolve até 100 por página por padrão). Em pastas grandes ou com erro, a anti-duplicação falha.
- **Participante externo (F8):** RA = `EXTERNO` (sem diferenciar maiúsculas, sem espaços nas bordas) é o marcador de quem não tem RA. Não há campo próprio: a regra está em `RegistroAcademico` e é lida pelo `ValidadorParticipantes` (lote e unitário), `PdfService` (sem RA e sem curso no certificado), `GoogleSheetsService.montarLinha` (`EXTERNO` e curso vazio) e `EmissaoService` (RA normalizado). Um aluno cujo RA fosse literalmente "externo" seria tratado como externo. O frontend repete o marcador em `RA_EXTERNO` (`App.jsx`); mude os dois juntos.

## Contrato frontend ↔ backend
- O lote usa `@ModelAttribute` com um record: os nomes dos campos do `FormData` (`nomeEvento`, `dataRealizacao`, `dataFim`, `cargaHoraria`, `cursoPadrao`, `arquivoCsv`) têm de ser iguais aos componentes de `EmissaoLoteEventoRequestDTO`.
- A data trafega como `yyyy-MM-dd` (input date) e vira `LocalDate`.
- A carga horária é `Double` em horas decimais. O frontend troca `,` por `.` antes de enviar.
- Desde a F2, o limite da carga horária (formulário e coluna `Horas`) fica em `InterpretadorHoras.dentroDosLimites`: pelo menos 1 minuto depois do arredondamento e no máximo 1.000 h. O `validarCargaHoraria` do `App.jsx` repete a regra; mude os dois juntos. O `<input>` da carga usa `step="any" min="0" max="1000"` de propósito: um `min`/`step` nativo mais restritivo bloqueia o envio antes do `validarCargaHoraria` (o `<form>` não tem `noValidate`).
- **Deploy da F2:** backend (Render) antes do frontend (Vercel). Frontend novo com backend antigo mostra "—" na coluna Horas da prévia.
- **Cursos duplicados (F4):** a lista de cursos está em `certificado.cursos.*` (`application.properties`) **e** em `frontend/src/cursos.js`. Ao acrescentar ou remover um curso, mude os dois. `cursoPadrao`/`cursoParticipante` ausentes valem `SI` (compatibilidade com o frontend antigo); um código desconhecido dá 400.
- **Período do evento (F3):** `dataRealizacao` continua sendo o nome do campo e passou a significar a data de **início**; `dataFim` é opcional e só vai no FormData/JSON quando preenchido. O limite (fim ≥ início, até 366 dias) está em `ValidadorParticipantes.validarEvento` **e** em `validarPeriodo` (`App.jsx`); mude os dois juntos. O campo de fim não tem `min` nativo de propósito (mesma razão da carga horária).
- **Deploy da F3:** backend (Render) antes do frontend (Vercel). Frontend novo com backend antigo: o `dataFim` é ignorado sem erro e o certificado sai só com a data de início.
- **Sheets com `data_fim` (F3):** a linha agora tem 7 colunas (`... | carga_horaria | data_fim`). O cabeçalho `data_fim` **não** é criado pelo sistema: acrescente-o à mão na planilha oficial antes do deploy (na de teste já foi feito).
- **RA obrigatório (F8):** o lote recusa CSV sem a coluna RA (400) e marca RA vazio como ERRO; o unitário recusa `raParticipante` em branco. Externo com curso é erro no lote e 400 no unitário; por isso o frontend **não envia** `cursoParticipante` para externo (o padrão `SI` da F4 não vale para ele). O "N/A" do Sheets e o `th:if="${ra}"` do template ficaram como defesa.
- **Deploy da F8:** backend (Render) antes do frontend (Vercel). CSVs antigos **sem a coluna RA** passam a ser recusados: avise a diretoria antes. Frontend antigo com backend novo funciona (já exigia RA no unitário), mas o guia ainda diz "RA opcional" e não há checkbox de externo.
- O endpoint unitário retorna **um objeto**; o frontend o embrulha em array para reaproveitar `RelatorioEmissao`.
- O frontend diz "ordem esperada: Nome, RA, E-mail", mas o backend mapeia **por nome de cabeçalho**, não por posição.
- A senha da API fica em `localStorage` (`casis_api_key`) e vai no header `X-API-KEY`.

## Ambiente e testes
- `BackendApplicationTests.contextLoads` precisa das propriedades `GOOGLE_*`. Sem `secrets.properties`, o teste falha (o CI só gera Javadoc, não roda testes). Rodar manualmente, só com credenciais de teste: `cd backend && ./mvnw test -Dtest=BackendApplicationTests` (PowerShell: `.\mvnw.cmd test "-Dtest=BackendApplicationTests"`). Passou na F2 (2026-11-20) e na F3 (2026-11-27). O Claude não roda esse teste.
- **Java 8 no PATH (máquina do desenvolvedor):** sem `JAVA_HOME`, o Maven pega `C:\Program Files (x86)\Common Files\Oracle\Java\java8path` e falha com "No compiler is provided" (testes) ou "class file version 61.0 … up to 52.0" (`spring-boot:run`). Use o JDK 25 em `C:\Program Files\Eclipse Adoptium\jdk-25.0.5.8-hotspot` (`$env:JAVA_HOME = ...` e `%JAVA_HOME%\bin` no início do `Path`). Para o Claude rodar a suíte sem o `contextLoads`: `.\mvnw.cmd test "-Dtest=!BackendApplicationTests" "-Dsurefire.failIfNoSpecifiedTests=false"`.
- Testes de controller: use `@WebMvcTest` (Boot 4: `org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest`) + `@MockitoBean EmissaoService` + `@ActiveProfiles("test")`. Sem esse perfil, o `dev` importa o `secrets.properties` (veja `CertificadoControllerTest`).
- `GOOGLE_SHEETS_PLANILHA_ID` deve ser **só o ID**, sem `/edit?gid=...`.
- `GOOGLE_SHEETS_NOME_ABA` tem padrão `Página1`.
- O CORS está fixo no controller: `https://sistema-casis.vercel.app/` e `http://localhost:5173`.
- **Acentos em `.properties`:** o Spring lê `application.properties` como ISO-8859-1. Textos com acento (nome da gestão, cursos) usam escapes `\uXXXX`; acento digitado direto vira lixo no certificado. `GestaoPropertiesTest` protege os padrões.
- **Gestão configurável (F4):** nome, sigla, cidade, fundo e cursos ficam em `certificado.*` (`GestaoProperties`), sobrescrevíveis por variável de ambiente (ex.: `CERTIFICADO_SIGLA`). Códigos de curso são convertidos para maiúsculas. A imagem de fundo é **sempre** embutida como `data:image/png`: um JPG configurado em `imagem-fundo` sai quebrado.
- **Escape `\uFEFF` em Java:** as ferramentas de edição do Claude (Edit, `sed`, heredoc) convertem `\uFEFF` no caractere BOM literal (invisível) ou comem a barra. Monte o texto com `chr(92) + "uFEFF"` num script Python e confira que o arquivo não tem os bytes `EF BB BF` (`od -c` mostra `357 273 277`). Há teste cobrindo em `InterpretadorCursoTest`.
- Backend em `server.port=8081` no perfil dev; o Dockerfile expõe 8080.

## Git e GitHub
- **PR a partir do fork aponta para o upstream por padrão** (web e `gh pr create`). Para ficar só no fork, troque o *base repository* para `GuilhermeSMathias/EmissorCertificadoDacomp`, ou use `gh pr create --repo GuilhermeSMathias/EmissorCertificadoDacomp --base main`. PR aberto no upstream notifica o dono original.
- O `gh` (GitHub CLI) **não está instalado** na máquina do desenvolvedor; PRs são abertos manualmente pela web.

## Código morto ou placeholder
- `frontend/src/components/DropzoneCsv.jsx`, `templates/certificado_gestao1.html` e `certificado_gestao2.html` estão vazios.
- Desde a F1 (P6), o lote usa `CsvService.lerLinhas` + `ValidadorParticipantes`. `EmissaoService.definirEstrategia`, `FonteDadosCsvStrategy` e `CsvService.extrairAlunos` ficaram **sem uso**, mantidos como ponto de extensão para a F5.
- **A coluna `Horas` (F2) só é lida por `CsvService.lerLinhas`.** `extrairAlunos` e `FonteDadosCsvStrategy` a ignoram, e `AlunoCertificado` não tem campo de horas. Reativar a Strategy na F5 sem levar as horas faz todo o lote voltar para a carga do formulário, sem erro. O mesmo vale para a coluna `Curso` (F4): sem ela, todos voltam ao curso padrão.
- **A regra do RA (F8) só vale em `CsvService.lerLinhas` + `ValidadorParticipantes`.** `extrairAlunos` (`HeaderMap.isValido`) ainda aceita CSV sem RA e não conhece `EXTERNO`. Reativar a Strategy na F5 sem portar a regra volta a emitir sem RA.
- `FonteDadosBancoStrategy` retorna lista vazia. `EmissaoGestaoRequestDTO` e `AlunoCertificadoGestao` ainda não são usados.
- ~~`certificado_evento.html` tem o bloco CSS `html, body` duplicado.~~ Removido na F4 (P6).
- Restos de CASIS mantidos de propósito na F4: classes `casis-*`, chave `casis_api_key`, pacote `br.edu.utfpr.casis`, `setApplicationName("CASIS Certificados")` e o "© 2026" fixo no rodapé do e-mail.
