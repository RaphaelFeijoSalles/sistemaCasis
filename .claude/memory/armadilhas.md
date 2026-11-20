# Armadilhas e comportamentos não óbvios

> Leia antes de mexer no fluxo de emissão. Adicione aqui tudo que custou tempo para descobrir.

## Críticas (quebram produção ou geram efeito real)
- **Efeitos reais:** cada emissão manda e-mail (Gmail API), cria pastas e arquivos no Drive e adiciona linhas no Sheets. Não há ambiente "sandbox" no código. A separação depende só das credenciais em `secrets.properties`.
- **Anti-duplicação depende do nome do arquivo:** `"Certificado_" + nome.trim().replaceAll("\\s+", "_") + ".pdf"`, montado em **dois lugares** de `EmissaoService` (lote e unitário). Mudar esse formato faz o sistema reemitir certificados já enviados.
- **O anexo do e-mail usa outra fórmula** (`EmailService`: sem `trim()`). Hoje não afeta a anti-duplicação, mas não confie que as duas sejam iguais.
- **Pastas do Drive:** `Raiz/AAAA-S/NomeEvento`, buscadas por nome exato. Semestre 1 = meses 1 a 6. Renomear a pasta no Drive ou mudar a regra cria pastas duplicadas.
- **Falhas silenciosas:** `fazerUploadCertificado` e `registrarEmissao` engolem exceções. O participante pode receber o e-mail sem backup no Drive. Nesse caso, a próxima emissão envia o e-mail de novo.
- **`listarArquivosNaPasta` retorna um conjunto vazio em caso de erro** e não pagina (a API do Drive devolve até 100 por página por padrão). Em pastas grandes ou com erro, a anti-duplicação falha.

## Contrato frontend ↔ backend
- O lote usa `@ModelAttribute` com um record: os nomes dos campos do `FormData` (`nomeEvento`, `dataRealizacao`, `cargaHoraria`, `arquivoCsv`) têm de ser iguais aos componentes de `EmissaoLoteEventoRequestDTO`.
- A data trafega como `yyyy-MM-dd` (input date) e vira `LocalDate`.
- A carga horária é `Double` em horas decimais. O frontend troca `,` por `.` antes de enviar.
- Desde a F2, o limite da carga horária (formulário e coluna `Horas`) fica em `InterpretadorHoras.dentroDosLimites`: pelo menos 1 minuto depois do arredondamento e no máximo 1.000 h. O `validarCargaHoraria` do `App.jsx` repete a regra; mude os dois juntos.
- **Deploy da F2:** backend (Render) antes do frontend (Vercel). Frontend novo com backend antigo mostra "—" na coluna Horas da prévia.
- O endpoint unitário retorna **um objeto**; o frontend o embrulha em array para reaproveitar `RelatorioEmissao`.
- O frontend diz "ordem esperada: Nome, RA, E-mail", mas o backend mapeia **por nome de cabeçalho**, não por posição.
- A senha da API fica em `localStorage` (`casis_api_key`) e vai no header `X-API-KEY`.

## Ambiente e testes
- `BackendApplicationTests.contextLoads` precisa das propriedades `GOOGLE_*`. Sem `secrets.properties`, o teste falha (o CI só gera Javadoc, não roda testes).
- Testes de controller: use `@WebMvcTest` (Boot 4: `org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest`) + `@MockitoBean EmissaoService` + `@ActiveProfiles("test")`. Sem esse perfil, o `dev` importa o `secrets.properties` (veja `CertificadoControllerTest`).
- `GOOGLE_SHEETS_PLANILHA_ID` deve ser **só o ID**, sem `/edit?gid=...`.
- `GOOGLE_SHEETS_NOME_ABA` tem padrão `Página1`.
- O CORS está fixo no controller: `https://sistema-casis.vercel.app/` e `http://localhost:5173`.
- Backend em `server.port=8081` no perfil dev; o Dockerfile expõe 8080.

## Git e GitHub
- **PR a partir do fork aponta para o upstream por padrão** (web e `gh pr create`). Para ficar só no fork, troque o *base repository* para `GuilhermeSMathias/EmissorCertificadoDacomp`, ou use `gh pr create --repo GuilhermeSMathias/EmissorCertificadoDacomp --base main`. PR aberto no upstream notifica o dono original.
- O `gh` (GitHub CLI) **não está instalado** na máquina do desenvolvedor; PRs são abertos manualmente pela web.

## Código morto ou placeholder
- `frontend/src/components/DropzoneCsv.jsx`, `templates/certificado_gestao1.html` e `certificado_gestao2.html` estão vazios.
- Desde a F1 (P6), o lote usa `CsvService.lerLinhas` + `ValidadorParticipantes`. `EmissaoService.definirEstrategia`, `FonteDadosCsvStrategy` e `CsvService.extrairAlunos` ficaram **sem uso**, mantidos como ponto de extensão para a F5.
- **A coluna `Horas` (F2) só é lida por `CsvService.lerLinhas`.** `extrairAlunos` e `FonteDadosCsvStrategy` a ignoram, e `AlunoCertificado` não tem campo de horas. Reativar a Strategy na F5 sem levar as horas faz todo o lote voltar para a carga do formulário, sem erro.
- `FonteDadosBancoStrategy` retorna lista vazia. `EmissaoGestaoRequestDTO` e `AlunoCertificadoGestao` ainda não são usados.
- `certificado_evento.html` tem o bloco CSS `html, body` duplicado.
