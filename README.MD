# 🐍 Sistema de Emissão de Certificados - CASIS

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![React](https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)
![Vite](https://img.shields.io/badge/Vite-B73BFE?style=for-the-badge&logo=vite&logoColor=FFD62E)

Sistema oficial desenvolvido para o **Centro Acadêmico de Sistemas de Informação (CASIS)** da UTFPR - Campus Londrina.

O objetivo desta aplicação é automatizar o fluxo de geração e envio de certificados para eventos e conclusões de gestão, substituindo o trabalho manual de design por um fluxo escalável em lote.

## 🚀 Funcionalidades

- **Geração Dinâmica de PDFs:** Conversão de templates HTML + Imagens de fundo em documentos PDF.
- **Processamento em Lote:** Leitura de planilhas (`.csv`) exportadas diretamente do Google Forms de presença.
- **Disparo de E-mails Automático:** Envio do certificado gerado diretamente para a caixa de entrada do aluno.
- **Suporte a Múltiplos Modelos:** Preparado para emitir certificados de Eventos e de Conclusão de Gestão do CA.
- **Interface Dark/Python:** UI voltada para uso interno da diretoria, tematizada com a identidade visual do CASIS.

---

## 📂 Arquitetura do Projeto (Monorepo)

O sistema está dividido em duas frentes independentes:

- `/backend`: API REST construída em **Java 17+ e Spring Boot**. Responsável pela leitura do CSV, injeção de dados no template (Thymeleaf), geração do PDF (OpenPDF) e envio de e-mails (JavaMailSender).
- `/frontend`: Aplicação SPA construída em **React e Vite**. Interface para upload rápido dos dados pela gestão do CA.

---

## 🛠️ Como executar localmente

### 1. Pré-requisitos
- [Java JDK 17+](https://adoptium.net/)
- [Node.js 18+](https://nodejs.org/)
- Maven (Opcional, o projeto usa o wrapper embutido)

### 2. Rodando o Backend (API)
Abra um terminal e navegue até a pasta do backend:
```bash
cd backend
Compile e rode a aplicação:

Bash
# Se usar Windows:
./mvnw.cmd spring-boot:run

# Se usar Linux/Mac:
./mvnw spring-boot:run
A API estará rodando em http://localhost:8080.

3. Rodando o Frontend (Interface)
Abra outro terminal e navegue até a pasta do frontend:

Bash
cd frontend
Instale as dependências e inicie o servidor de desenvolvimento:

Bash
npm install
npm run dev
O sistema estará acessível em http://localhost:5173.

👨‍💻 Desenvolvido por
Raphael Feijó Salles - Gestão CASIS
