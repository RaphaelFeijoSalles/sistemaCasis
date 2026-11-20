---
name: backend-spring
description: Padrões do backend Java 25 / Spring Boot 4 do CASIS (DTOs record, serviços, Strategy, Lombok, logs, Javadoc, erros). Use ao criar ou alterar qualquer arquivo em backend/src/main/java.
---

# Backend Spring — padrões do projeto

## Onde colocar cada coisa
| Tipo | Pacote | Padrão |
|---|---|---|
| Endpoint REST | `controller` | Fino: valida a `X-API-KEY`, valida a presença dos dados, delega ao serviço e mapeia o erro para HTTP |
| Contrato HTTP | `dto` | `record` imutável com Javadoc `@param` por componente |
| Domínio | `model` | Classes Lombok (`@Data @SuperBuilder @NoArgsConstructor`) herdando de `AlunoCertificado` |
| Orquestração | `service/EmissaoService` | Coordena os serviços; não fala com APIs externas diretamente |
| Integração externa | `service/Google*Service`, `EmailService` | Uma classe por API; credenciais via `@Value` |
| Fonte de participantes | `service/strategy` | Implementa `FonteDadosCertificadoStrategy`; escolhida em `definirEstrategia` |
| Regra pura / formatação | `service/*Formatter` ou `*Validator` | `final class`, métodos estáticos package-private, 100% testável sem Spring |

## Regras
- **Injeção por construtor** com `@RequiredArgsConstructor` e campos `private final`. Não use `@Autowired` em campo.
- **Logs** com `@Slf4j`: `info` para marcos do fluxo, `warn` para entrada inválida, `error` com a exceção. Não registre CPF nem senhas.
- **Javadoc em português** em toda classe e método público (o CI publica o Javadoc no GitHub Pages).
- **Imports:** use import normal. Não escreva nomes totalmente qualificados no meio do código (o `EmissaoService` atual faz isso; não replique).
- **Lógica nova de regra de negócio** (validação, cálculo de horas, datas) vai para uma classe pura testável, não para dentro do serviço que chama APIs.
- **Novas propriedades** vão em `application-dev.properties` / `application-prod.properties` com `${VAR}` e são documentadas em `secrets.properties.example`. Isso vale como mudança de "documentação": confirme com o desenvolvedor antes.
- **Erros:** hoje o controller captura `Exception` e devolve 500 com a mensagem. Em código novo, prefira exceções específicas (`IllegalArgumentException` → 400). Se a spec pedir padronização, proponha um `@RestControllerAdvice` como passo separado.
- **Datas:** `LocalDate`; formatação pt-BR via `DateTimeFormatter` com `Locale.of("pt", "BR")`.
- **Carga horária:** `Double` em horas decimais. Toda conversão passa por `CargaHorariaFormatter`.

## Ao mudar um DTO
1. Atualize o record e o Javadoc.
2. Atualize quem consome o DTO (`EmissaoService`, `PdfService`).
3. Atualize o frontend (campo com **o mesmo nome**). Veja a skill `frontend-react`.
4. Novos campos devem ser opcionais (aceitar `null`) para não quebrar o frontend em produção, salvo decisão contrária na spec.

## Verificação
`cd backend && ./mvnw test`. Para checar só a compilação: `./mvnw -q compile`.
