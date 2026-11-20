---
name: testes
description: Como escrever e rodar testes no CASIS sem tocar nas APIs reais do Google (JUnit 5, Mockito, MockMultipartFile, testes de controller). Use ao escrever testes, quando um teste falhar, ou quando o usuário pedir "testa", "cobre com testes" ou rodar /testes.
argument-hint: "[classe ou feature]"
---

# Testes

## Rodar
```bash
cd backend && ./mvnw test                              # suíte completa
cd backend && ./mvnw test -Dtest=CsvServiceTest        # uma classe
cd backend && ./mvnw test -Dtest=CsvServiceTest#deveExtrairAlunosDeCsvSeparadoPorVirgula
cd frontend && npm run lint && npm run build           # o frontend não tem testes automatizados hoje
```

## Pirâmide recomendada
1. **Unitário puro (a maioria):** formatadores, validadores, parser de CSV. Sem Spring, instanciando a classe diretamente. Modelos: `CargaHorariaFormatterTest`, `CsvServiceTest` (usa `MockMultipartFile` com text block `"""`).
2. **Serviço orquestrador com mocks:** `EmissaoService` com Mockito.
   ```java
   @ExtendWith(MockitoExtension.class)
   class EmissaoServiceTest {
       @Mock CsvService csvService;
       @Mock PdfService pdfService;
       @Mock EmailService emailService;
       @Mock GoogleDriveService driveService;
       @Mock GoogleSheetsService sheetsService;
       @InjectMocks EmissaoService emissaoService;

       @Test
       void naoDeveEnviarEmailQuandoCertificadoJaExiste() {
           when(driveService.obterOuCriarPastaEvento(any(), any())).thenReturn("pasta");
           when(driveService.listarArquivosNaPasta("pasta")).thenReturn(Set.of("Certificado_Ana_Silva.pdf"));
           // ... arrange CSV ...
           // act + assert status EXISTENTE
           verify(emailService, never()).enviarCertificado(any(), any(), any(), any());
       }
   }
   ```
3. **Controller (poucos):** teste de fatia web com o serviço mockado, cobrindo 403 sem `X-API-KEY`, 400 sem CSV e 200 feliz. No Spring Boot 4, use `@MockitoBean` (não `@MockBean`) e **confira o pacote do `@WebMvcTest`** nas dependências antes de importar, porque ele mudou de módulo no Boot 4.
4. **PdfService:** pode rodar de verdade, já que só gera bytes em memória. Verifique se o array não está vazio e se começa com `%PDF`.

## Regras
- **Proibido** em teste: credenciais reais, `@SpringBootTest` que chame serviços Google, envio de e-mail.
- Testes que sobem o contexto Spring devem definir propriedades falsas: `@TestPropertySource(properties = {"GOOGLE_CLIENT_ID=x", "GOOGLE_CLIENT_SECRET=x", "GOOGLE_REFRESH_TOKEN=x", "GOOGLE_DRIVE_PASTA_RAIZ_ID=x", "GOOGLE_SHEETS_PLANILHA_ID=x", "email.casis=teste@exemplo.com"})`.
- Nomes de teste em português, no formato `deve<Comportamento>Quando<Condição>`.
- Um teste por critério de aceitação da spec, no mínimo. Cite o CA no nome ou em comentário.
- Dados de teste fictícios (`Ana Silva`, `@alunos.utfpr.edu.br`). Nunca dados reais de alunos.

## Quando um teste falhar
1. Leia a mensagem inteira e o primeiro `Caused by`.
2. Diferencie entre o teste estar errado e o código estar errado. Na dúvida, pergunte qual é o comportamento esperado.
3. Não "conserte" um teste existente afrouxando a asserção sem autorização.
