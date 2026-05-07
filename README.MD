# 🎓 Sistema de Emissão de Certificados - CASIS 🐍

Bem-vindo ao repositório oficial do Sistema de Certificados do **Centro Acadêmico de Sistemas de Informação (CASIS) da UTFPR Londrina**.

Este sistema foi desenvolvido para automatizar a geração em PDF, envio por e-mail e backup no Google Drive de certificados de eventos, operando tanto em lotes (via planilha CSV) quanto em envios unitários.

---

## 🛠️ Tecnologias Utilizadas

**Monorepo** dividido em duas camadas:
* **Frontend:** React + Vite, CSS puro (Design System CASIS).
* **Backend:** Java 25, Spring Boot 3, Thymeleaf (Templates), Flying Saucer (Geração de PDF), JavaMailSender e Google API Client (Integração com o Drive).

---

## 🚀 Como rodar o projeto localmente

O projeto possui uma arquitetura de propriedades focada em segurança. O arquivo `application-dev.properties` aponta para variáveis num arquivo chamado `secrets.properties`, que **não está no repositório**.

Para testar o fluxo de emissão, você precisará de credenciais do Google. Siga o caminho que se adequa ao seu perfil:

### Caminho A: Sou membro da Diretoria do CASIS
1. Solicite ao [mantenedor](https://linktr.ee/raphaelfeijosalles) do projeto o arquivo `secrets.properties`.
2. Cole o arquivo no diretório `/backend/src/main/resources/`.
3. Inicie o Backend (Spring Boot) e o Frontend (`npm run dev`). *Cuidado para não usar a pasta raiz de Produção para testes bobos.*

### Caminho B: Sou um Contribuinte Open-Source
Para garantir sua independência e não afetar os dados reais do Centro Acadêmico, você deve configurar o seu próprio ambiente de testes usando a sua conta pessoal do Google.

1. Crie o arquivo `/backend/src/main/resources/secrets.properties`.
2. Configure um projeto no [Google Cloud Console](https://console.cloud.google.com/), habilite a **Google Drive API** e gere um Client ID e Client Secret.
3. Gere um **Refresh Token** via [Google OAuth 2.0 Playground](https://developers.google.com/oauthplayground/) (use o escopo `https://www.googleapis.com/auth/drive`).
4. Preencha o seu `secrets.properties` com as variáveis exigidas (E-mail SMTP, Senha de App do E-mail, Credenciais do Google e o ID de uma pasta vazia do seu Google Drive).

---

## ⚠️ Regras de Negócio Importantes (Leia antes de mexer)

O sistema possui lógicas rígidas atreladas ao funcionamento do Google Drive. **Não altere estes padrões sem uma forte justificativa de arquitetura:**

* **Estrutura de Pastas (Ano-Semestre):** O sistema cria automaticamente subpastas baseadas na data (Ex: `2026-1/Nome do Evento`). **Nunca altere manualmente o nome destas pastas no Drive** (ex: mudar para "2026 1º Semestre"). O backend confia nesta string exata para não duplicar pastas.
* **Anti-Duplicação:** Antes de gerar um PDF, o sistema varre a pasta do evento no Drive. Se um arquivo com o nome `Certificado_Nome_da_Pessoa.pdf` já existir, ele acusa "EXISTENTE" e aborta o envio para poupar recursos. Alterar a lógica de geração do nome do arquivo quebrará o sistema de fallback.

---

## 🤝 Como Contribuir

Ficamos muito felizes com a sua intenção de ajudar! Para contribuir:

1. Faça um **Fork** do repositório.
2. Crie uma branch para a sua feature (`git checkout -b feature/minha-feature`).
3. Commit suas mudanças seguindo o padrão Conventional Commits (`feat: ...`, `fix: ...`).
4. Faça o Push para a sua branch (`git push origin feature/minha-feature`).
5. Abra um **Pull Request (PR)** explicando detalhadamente o que foi feito.

---

## 💡 Call for Contributors: O Desafio do Check-in via QR Code

Queremos revolucionar a forma como o CASIS gerencia eventos, substituindo a planilha CSV por um fluxo 100% automático. **Temos um desafio arquitetural e convidamos você a propor soluções!**

**A Ideia (O Fluxo Ideal):**
1. O secretariado cadastra um evento no sistema (Nome, Data, Carga Horária).
2. O sistema gera um **QR Code**.
3. Na porta do evento, os alunos escaneiam o QR Code e preenchem um form mobile (Nome, RA, E-mail).
4. O sistema gera os certificados automaticamente.

**O Desafio Técnico (Óbice):** Atualmente, a Fase 1 do nosso sistema **não possui Banco de Dados (ex: PostgreSQL)**.
Gostaríamos de ter um *delay* de 24 horas após o evento para uma interceptação humana (O famoso: *"Deixe-me conferir se esse aluno realmente estava no evento"*), antes do disparo dos e-mails.

**Possível Abordagem (Brainstorming):**
Poderíamos usar o Google Drive como nosso "Banco de Dados"?
* Salvar um arquivo `.json` na nuvem contendo a lista dos participantes capturados no QR Code.
* Criar uma tela de "Aprovação" no frontend que lê esse JSON do Drive.
* Após a aprovação da diretoria, o sistema itera esse JSON e roda a rotina de PDF e E-mail.

Se você acha essa ideia incrível, se tem experiência com arquiteturas *serverless* ou quer encabeçar a criação da nossa integração com um Banco de Dados real (Fase 2), **abra uma Issue com a sua proposta de arquitetura!**

<p align="center">
  Desenvolvido com ☕ e código limpo por <a href="https://github.com/RaphaelFeijoSalles" target="_blank">Raphael Salles</a>.
</p>