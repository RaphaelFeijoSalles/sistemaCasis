<div align="center">
  <h1>🎓 Sistema de Emissão de Certificados - CASIS 🐍</h1>
  <p>
    <i>Bem-vindo ao repositório oficial do Sistema de Certificados do <b>Centro Acadêmico de Sistemas de Informação (CASIS) da UTFPR Londrina</b>.</i>
  </p>

  <a href="https://sistema-casis.vercel.app/" target="_blank">
    <img src="https://img.shields.io/badge/Acessar_Aplicação-Produção-success?style=for-the-badge&logo=vercel" alt="Acessar Aplicação" />
  </a>
  <br><br>

  <p>
    Este sistema foi desenvolvido para automatizar a geração em PDF, envio por e-mail e backup no Google Drive de certificados de eventos, operando tanto em lotes (via planilha CSV) quanto em envios unitários.
  </p>

  <h3>📌 Sumário</h3>
  <a href="#%EF%B8%8F-tecnologias-utilizadas">🛠️ Tecnologias Utilizadas</a> •
  <a href="#-entendendo-o-fluxo-completo-de-geração">🔄 Fluxo Completo</a> •
  <a href="#-como-configurar-e-rodar-o-projeto-step-by-step">🚀 Como Configurar</a> •
  <a href="#%EF%B8%8F-regras-de-negócio-importantes-google-drive">⚠️ Regras de Negócio (Drive)</a> •
  <a href="#-como-contribuir">🤝 Como Contribuir</a> •
  <a href="#-o-futuro-do-sistema-banco-de-dados-e-gestão-completa">💡 O Futuro da Aplicação</a>
</div>

<br>

## 🛠️ Tecnologias Utilizadas

**Monorepo** dividido em duas camadas:

* **Frontend:** 
  * ![React](https://img.shields.io/badge/react-%2320232a.svg?style=flat&logo=react&logoColor=%2361DAFB) ![Vite](https://img.shields.io/badge/vite-%23646CFF.svg?style=flat&logo=vite&logoColor=white) Para uma interface rápida, reativa e moderna.
  * ![CSS3](https://img.shields.io/badge/css3-%231572B6.svg?style=flat&logo=css3&logoColor=white) CSS puro focado na performance e seguindo o Design System CASIS.
  * ![Vercel](https://img.shields.io/badge/vercel-%23000000.svg?style=flat&logo=vercel&logoColor=white) Hospedagem de Produção.
* **Backend:** 
  * ![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=flat&logo=openjdk&logoColor=white) **25** + ![Spring Boot](https://img.shields.io/badge/spring_boot-%236DB33F.svg?style=flat&logo=spring&logoColor=white) **3**: Base sólida, segura e padronizada para a API REST.
  * ![Thymeleaf](https://img.shields.io/badge/Thymeleaf-%23005C0F.svg?style=flat&logo=Thymeleaf&logoColor=white) Motor de templates para renderizar HTML (certificados e e-mails) injetando as variáveis dinâmicas.
  * **Flying Saucer:** Conversão precisa do HTML (gerado pelo Thymeleaf) para PDF.
  * **JavaMailSender:** Envio de e-mails via protocolo SMTP.
  * ![Google Drive](https://img.shields.io/badge/Google%20Drive-4285F4?style=flat&logo=googledrive&logoColor=white) Integração via API Client para criação de pastas e backup dos certificados gerados.
  * ![Render](https://img.shields.io/badge/Render-%2346E3B7.svg?style=flat&logo=render&logoColor=white) Hospedagem de Produção.

---

## 🔄 Entendendo o Fluxo Completo de Geração

1. **Fonte de Dados:**
   * **Via Planilha (Lote):** O usuário faz upload de um CSV com os dados dos participantes (Nome, E-mail, etc.).
   * **Unitário:** Preenchimento de um formulário individual no Frontend.
2. **Renderização do Template:** O Backend recebe os dados, processa regras de negócio (cálculos de horas, tratamento de nomes) e injeta essas variáveis no template HTML usando **Thymeleaf**.
3. **Geração do PDF:** O HTML renderizado é transformado em PDF usando o **Flying Saucer**.
4. **Verificação Anti-Duplicação:** O sistema consulta o Google Drive na pasta específica do evento para verificar se o PDF de um participante já existe. Se existir, pula a geração/envio.
5. **Backup na Nuvem:** O novo PDF é upado para o Google Drive na estrutura de pastas correta.
6. **Envio de E-mail:** O PDF é anexado e enviado por e-mail (usando outro template Thymeleaf para o corpo da mensagem) via **JavaMailSender** personalizado para cada participante.

---

## 🚀 Como Configurar e Rodar o Projeto (Step-by-Step)

O projeto possui uma arquitetura de propriedades focada em segurança. O arquivo principal `application.properties` define regras gerais, mas delega senhas e credenciais para um arquivo `secrets.properties`, que **NÃO VAI para o repositório** por questões de segurança (ele está no `.gitignore`).

Para facilitar a configuração, criamos um arquivo `secrets.properties.example` na pasta `backend/src/main/resources/`. Nele, você encontrará as variáveis que devem ser configuradas.

**Siga os passos de acordo com seu perfil:**

### 👨‍💼 Perfil A: Membros e Mantenedores Oficiais do CASIS (Gestão/Diretório de Dev)
Se você vai rodar o sistema para emitir certificados reais ou gerenciar o ambiente de produção:
1. Solicite ao [mantenedor atual](https://github.com/RaphaelFeijoSalles) do projeto ou à presidência o arquivo `secrets.properties` oficial (com as credenciais do Google Workspace e SMTP da entidade).
2. Cole o arquivo no diretório `/backend/src/main/resources/`.
3. Inicie o Backend (Spring Boot) na sua IDE.
4. Inicie o Frontend executando `npm run dev` na pasta do frontend.
5. *⚠️ Cuidado: Qualquer teste gerará pastas e arquivos no Google Drive oficial do CASIS, assim como o sistema hospedado na [Vercel](https://sistema-casis.vercel.app/). Para desenvolvimento de features e testes que não devem refletir nos dados oficiais, utilize a configuração do Perfil B.*

### 👨‍💻 Perfil B: Aventureiros Open-Source e Novos Devs (Ambiente de Testes Pessoal)
Para testar livremente, desenvolver novas features ou estudar a arquitetura do projeto sem risco de sujar o Drive oficial ou disparar e-mails por engano:

**Passo 1: Criar o arquivo de secrets**
1. Acesse a pasta `/backend/src/main/resources/`.
2. Duplique o arquivo `secrets.properties.example` e renomeie a cópia para `secrets.properties`.

**Passo 2: Configurar o Google Cloud Console (Drive API)**
1. Acesse o [Google Cloud Console](https://console.cloud.google.com/) com a sua conta Google pessoal.
2. Crie um novo "Projeto".
3. Vá em "APIs e Serviços" > "Biblioteca" e ative a **Google Drive API**.
4. Vá em "Tela de consentimento OAuth" e configure como "Externo". Adicione seu e-mail como usuário de teste.
5. Vá em "Credenciais" > "Criar Credenciais" > "ID do cliente OAuth". Escolha tipo "Aplicativo da Web".
6. Copie o `Client ID` e o `Client Secret` gerados e cole nas chaves `GOOGLE_CLIENT_ID` e `GOOGLE_CLIENT_SECRET` do seu `secrets.properties`.

**Passo 3: Gerar o Refresh Token**
1. Acesse o [Google OAuth 2.0 Playground](https://developers.google.com/oauthplayground/).
2. Clique na engrenagem (Configurações), marque "Use your own OAuth credentials" e cole seu Client ID e Client Secret.
3. No Passo 1 (esquerda), cole este escopo: `https://www.googleapis.com/auth/drive` e clique em "Authorize APIs". Aceite os termos com sua conta Google.
4. No Passo 2, clique em "Exchange authorization code for tokens".
5. Copie o **Refresh Token** gerado e cole na chave `GOOGLE_REFRESH_TOKEN` do seu `secrets.properties`.

**Passo 4: Configurar a Pasta Raiz e E-mail**
1. Crie uma pasta vazia no seu Google Drive pessoal (ex: "Testes CASIS").
2. Abra a pasta e copie o ID que aparece na URL (após `folders/`). Cole na chave `GOOGLE_DRIVE_PASTA_RAIZ_ID` do `secrets.properties`.
3. Para envio de e-mails, você pode usar seu próprio Gmail. Nas configurações da sua conta Google, ative a verificação em duas etapas e gere uma **Senha de App**. Cole seu e-mail e essa senha de 16 dígitos nas chaves `spring.mail.username` e `spring.mail.password` no `secrets.properties`.
4. Defina um valor qualquer para `API_SECRET` no `secrets.properties` e no arquivo `.env` do seu frontend.

Pronto! Ao rodar a aplicação, todas as integrações ocorrerão no seu Drive e e-mail pessoais.

---

## ⚠️ Regras de Negócio Importantes (Google Drive)

O sistema possui uma lógica robusta em `GoogleDriveService.java` para a criação de pastas e análise de arquivos. **Leia antes de mexer:**

* **Estrutura de Pastas (Ano-Semestre/Evento):** O sistema cria automaticamente subpastas baseadas na data enviada. A lógica (vide `obterOuCriarPastaEvento`) calcula o semestre: se o mês do evento for <= 6, é semestre 1, senão é 2.
  * **Exemplo de caminho gerado:** `Pasta Raiz do CASIS / 2026-1 / Hackathon`
  * **Regra de Ouro:** O backend usa *queries* estritas (ex: `name='2026-1'`) para achar a pasta. **Nunca altere manualmente o nome destas pastas no Drive** (ex: mudar para "2026 1º Sem". O backend não achará a pasta, criará outra duplicada e perderá a referência de histórico.
* **Anti-Duplicação (Fallback):** O método `listarArquivosNaPasta` carrega um `Set<String>` com todos os PDFs da pasta do evento em memória. Antes de gerar um PDF, o sistema compara se o arquivo `Certificado_Nome_da_Pessoa.pdf` já está lá. Se a lógica de nomeclatura de arquivos for alterada no código, o sistema gerará PDFs duplicados, gastando processamento e limite do Drive à toa.

---

## 🤝 Como Contribuir

Ficamos muito felizes com a sua intenção de ajudar! Para contribuir:

1. Faça um **Fork** do repositório.
2. Crie uma branch para a sua feature (`git checkout -b feature/minha-feature`).
3. Siga os guias de configuração de ambiente descritos no Perfil B acima.
4. Commit suas mudanças seguindo o padrão Conventional Commits (`feat: ...`, `fix: ...`).
5. Faça o Push para a sua branch (`git push origin feature/minha-feature`).
6. Abra um **Pull Request (PR)** explicando detalhadamente o que foi feito.

---

## 💡 O Futuro do Sistema: Banco de Dados e Gestão Completa

A arquitetura atual foi planejada para receber um Banco de Dados no futuro (ex: PostgreSQL), transformando o sistema de um simples gerador em uma plataforma completa de gestão de alunos e eventos. 

**Pretensões com a Fase 2 (Banco de Dados):**
* Controle total sobre quem organizou, palestrou ou ouviu cada evento (veja o enum `TipoParticipacao.java`).
* Contabilização automática e acompanhamento de Horas Complementares dos alunos.
* Atribuição histórica de cargos de gestão do CA.
* Um **"Mural de Conquistas"** no Frontend onde alunos podem ver e exibir publicamente os eventos e cargos que participaram no curso de Sistemas de Informação.

O código já possui terreno preparado para essa evolução, como:
* O uso do enum `TipoParticipacao`.
* Padrões Strategy aplicados para diferentes fontes de dados.
* Estruturação de herança de tipos de usuários.

### Call for Contributors: O Desafio do Check-in via QR Code

Queremos revolucionar a forma como o CASIS gerencia eventos, abolindo a necessidade de planilhas CSV criadas manualmente.

**A Ideia (Fluxo Ideal):**
1. O secretariado cadastra o evento (Nome, Data, Carga Horária) no Banco de Dados.
2. O sistema gera um **QR Code**.
3. No evento, alunos escaneiam e dão "Check-in" no próprio celular.
4. Um painel permite à diretoria revisar as presenças (o famoso: *"Deixe-me conferir se esse aluno realmente estava no evento"*).
5. Após aprovação em massa com 1 clique, os certificados são gerados e disparados.

Se você acha essa ideia incrível e quer encabeçar a criação da Fase 2 e integração com Banco de Dados de verdade, **abra uma Issue com a sua proposta de arquitetura!**

---
<p align="center">
  Desenvolvido com ☕ e código limpo por <a href="https://github.com/RaphaelFeijoSalles" target="_blank">Raphael Salles</a>.<br>
  <i>Desenvolvedor de Software Freelancer. Quer construir um sistema assim ou melhorar sua aplicação? <a href="https://linktr.ee/raphaelfeijosalles" target="_blank">Entre em contato!</a></i>
</p>