# CLAUDE.md — Sistema de Certificados CASIS (v2.0)

Contexto carregado automaticamente em toda sessão do Claude Code. Mantenha curto e verdadeiro.
O manual de uso deste diretório está em [MANUAL.md](MANUAL.md).

## Regras de ouro (sempre valem)

1. **Na dúvida, pare e pergunte.** Nunca invente requisito, nome de campo, regra de negócio ou layout.
2. **Spec antes de código.** Mudança que não seja trivial exige uma spec aprovada em `.claude/specs/`. Use `/nova-spec`.
3. **Passos pequenos.** Um passo do plano por vez. Cada passo compila, passa nos testes e vira um commit.
4. **Nunca dispare emissão real.** Não chame `/emitir-lote` ou `/emitir-unitario`, não suba o backend apontando para credenciais reais e não rode código que chame Gmail, Drive ou Sheets. As credenciais em `secrets.properties` podem ser as **oficiais do CASIS**. Teste sempre com mocks.
5. **Não leia nem exiba segredos.** `secrets.properties` e `.env` estão fora dos limites.
6. **Não altere a documentação existente** (README.MD, Javadoc já escrito) a menos que a spec peça explicitamente.
7. **Ao terminar a sessão**, rode `/encerrar-sessao` para atualizar a memória.

## Memória do projeto (importada)

@memory/handoff.md
@memory/progresso.md
@memory/decisoes.md
@memory/armadilhas.md

## Stack

| Camada | Tecnologia | Onde |
|---|---|---|
| Backend | Java 25, Spring Boot 4.0.6, Lombok, Thymeleaf, Flying Saucer (OpenPDF), OpenCSV, Google APIs (Gmail/Drive/Sheets), springdoc | `backend/` |
| Frontend | React 19, Vite 8, axios, react-dropzone, react-hot-toast, lucide-react, CSS puro | `frontend/` |
| Deploy | Render (Docker, backend), Vercel (frontend), GitHub Pages (Javadoc via `.github/workflows/javadoc.yml`) | — |

## Arquitetura (backend `br.edu.utfpr.casis.backend`)

```
controller/CertificadoController   POST /api/certificados/emitir-lote (multipart) | /emitir-unitario (JSON)
                                   auth: header X-API-KEY == API_SECRET
service/EmissaoService             orquestra: fonte de dados -> Drive (pasta + anti-dup) -> PDF -> e-mail -> upload -> Sheets
service/strategy/*                 Strategy de fonte de dados (CSV ativo; Banco é placeholder da Fase 2)
service/CsvService                 detecta ';' ou ',', mapeia cabeçalho por nome (Nome/E-mail obrigatórios, RA opcional)
service/PdfService                 Thymeleaf (templates/certificado_evento.html) -> PDF; imagem de fundo em Base64
service/EmailService               Thymeleaf (certificado-email.html) -> MIME -> Gmail API
service/GoogleDriveService         pastas Raiz/AAAA-S/Evento, lista arquivos, upload
service/GoogleSheetsService        append: nome | ra | curso | data | evento | carga_horaria
service/CargaHorariaFormatter      horas decimais -> "1 hora e 30 minutos" / "01:30:00"
dto/  (records)                    contrato HTTP; nomes dos componentes = nomes dos campos do frontend
model/                             AlunoCertificado (abstrata) -> Evento | Gestao; TipoParticipacao
```

Frontend: `App.jsx` guarda estado, validação e chamadas axios; `components/CertificadoForm.jsx` é o formulário e a dropzone; `RelatorioEmissao.jsx` é a tabela de resultado; `AjudaEmissao.jsx` é o guia lateral.

## Comandos

```bash
# Backend (Git Bash). No PowerShell use .\mvnw.cmd
cd backend && ./mvnw test                 # todos os testes
cd backend && ./mvnw test -Dtest=CsvServiceTest
cd backend && ./mvnw -q compile           # checagem rápida de compilação

# Frontend
cd frontend && npm run lint
cd frontend && npm run build
```

## Convenções

- **Idioma:** código, logs, Javadoc, mensagens de erro e commits em **português**.
- **Commits:** Conventional Commits (`feat:`, `fix:`, `test:`, `refactor:`, `docs:`, `chore:`), com escopo opcional (`feat(csv): ...`).
- **Branches:** `feature/<id-spec>-<slug>` saindo da branch `v2.0`.
- **Backend:** DTOs como `record`, injeção por construtor (`@RequiredArgsConstructor`), `@Slf4j` com logs informativos, Javadoc em todo método público, regras de formatação em classes utilitárias testáveis (como `CargaHorariaFormatter`).
- **Testes:** JUnit 5. Serviços Google sempre mockados. Classes puras testadas sem Spring (veja `CsvServiceTest`).
- **Frontend:** componentes funcionais, mensagens de erro com `{ titulo, detalhe }` via toast, CSS em arquivos `.css` (sem framework).
