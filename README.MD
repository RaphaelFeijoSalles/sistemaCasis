🐍 Sistema de Emissão de Certificados - CASIS
Sistema oficial desenvolvido para o Centro Acadêmico de Sistemas de Informação (CASIS) da UTFPR - Campus Londrina.

Esta aplicação automatiza a geração e envio de certificados em lote para eventos e gestões do CA, utilizando injeção de dados em templates dinâmicos.

🚀 Funcionalidades
Processamento em Lote: Leitura de arquivos .csv para emissão massiva de documentos.

Geração Dinâmica: Uso de templates Thymeleaf convertidos em PDF via OpenPDF/FlyingSaucer.

Relatório em Tempo Real: Interface que detalha sucessos e falhas de envio para cada participante.

Segurança de Lote: Bloqueio de e-mails inválidos e proteção via CORS em nível de produção.

Suporte Híbrido: Preparado para certificados de Eventos e Conclusão de Gestão.

🏗️ Arquitetura e Deploy
O projeto utiliza uma estrutura de monorepo hospedada na nuvem:

Backend (/backend): API Java 25 e Spring Boot dockerizada. Hospedado no Render.

Frontend (/frontend): Aplicação React + Vite. Hospedado na Vercel.

Envio de E-mail: Integração com Gmail via JavaMailSender e autenticação por variáveis de ambiente.

🛠️ Como Executar Localmente
1. Variáveis de Ambiente
   Crie um arquivo .env na pasta /frontend:

Snippet de código
VITE_API_URL=http://localhost:8080
Configure as credenciais do Gmail no application.properties ou como variáveis de sistema:

EMAIL_CASIS: E-mail oficial do CASIS.

SENHA_CASIS: Senha de aplicativo gerada na Conta Google.

2. Rodando o Backend
   Bash
   cd backend
   ./mvnw spring-boot:run
3. Rodando o Frontend
   Bash
   cd frontend
   npm install
   npm run dev
   🐳 Docker (Produção)
   Para gerar a imagem de produção da API:

Bash
docker build -t casis-backend ./backend
Aviso de Segurança: Este projeto não versiona senhas ou chaves. Todas as credenciais sensíveis devem ser configuradas exclusivamente via painel de ambiente do Render/Vercel.

Desenvolvido por Raphael Feijó Salles - Gestão CASIS