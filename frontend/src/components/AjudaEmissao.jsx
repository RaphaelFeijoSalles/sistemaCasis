import { useState, useEffect } from 'react';
import { HelpCircle, PanelRightClose, PanelRightOpen, X } from 'lucide-react';
import './AjudaEmissao.css';

export default function AjudaEmissao({ emissaoTipo }) {
  const [modalAberto, setModalAberto] = useState(false);
  const [isMobile, setIsMobile] = useState(() => window.innerWidth <= 850);
  const [painelAberto, setPainelAberto] = useState(true);

  useEffect(() => {
    const handleResize = () => setIsMobile(window.innerWidth <= 850);
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  useEffect(() => {
    if (!modalAberto) return;

    document.body.classList.add('modal-open');

    const handleKeyDown = (event) => {
      if (event.key === 'Escape') {
        setModalAberto(false);
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => {
      document.body.classList.remove('modal-open');
      window.removeEventListener('keydown', handleKeyDown);
    };
  }, [modalAberto]);

  const conteudoAjuda = (
    <div className="conteudo-ajuda">
      <h3><HelpCircle size={18} /> Guia de preenchimento</h3>

      {emissaoTipo === 'lote' && (
        <section>
          <h4>Antes de disparar um lote</h4>
          <ul>
            <li><strong>Arquivo:</strong> envie apenas <code>.csv</code>. Arquivos <code>.xlsx</code> precisam ser baixados como CSV antes.</li>
            <li><strong>Cabeçalho:</strong> a primeira linha pode conter os nomes das colunas. O sistema lê os participantes a partir das linhas seguintes.</li>
            <li><strong>Ordem das colunas:</strong>
              <ol>
                <li>Nome completo</li>
                <li>RA</li>
                <li>E-mail</li>
              </ol>
            </li>
            <li><strong>Revise:</strong> exclua linhas em branco, duplicados, campos vazios, e-mails inválidos e espaços extras. Não basta limpar o conteúdo das células: apague a linha inteira para ela não ir vazia no CSV.
              <ol>
                <li>Google Sheets: clique no número da linha, use botão direito e escolha <strong>Excluir linha</strong>.</li>
                <li>Excel: clique no número da linha, use botão direito e escolha <strong>Excluir</strong> ou <strong>Excluir linhas da planilha</strong>.</li>
              </ol>
            </li>
            <li><strong>Resultado:</strong> o lote pode terminar com sucessos, já emitidos e falhas. Confira a tabela antes de reenviar.</li>
          </ul>
        </section>
      )}

      {emissaoTipo === 'individual' && (
        <section>
          <h4>Antes de emitir para uma pessoa</h4>
          <ul>
            <li><strong>Nome:</strong> escreva o nome completo exatamente como deve aparecer no certificado.</li>
            <li><strong>E-mail e RA:</strong> confira antes de enviar; ambos entram no fluxo de emissão e relatório.</li>
            <li><strong>Reenvio:</strong> se já existir certificado no Drive, o relatório pode indicar que ele já foi emitido.</li>
          </ul>
        </section>
      )}

      <section>
        <h4>Campos do evento</h4>
        <ul>
          <li><strong>Nome do evento:</strong> aparece no certificado, no assunto do e-mail, no Drive e no registro de backup.</li>
          <li><strong>Data:</strong> é formatada automaticamente por extenso no certificado.</li>
          <li><strong>Carga horária:</strong> use horas em decimal. <code>2</code> vira <i>2 horas</i>; <code>1.5</code> vira <i>1 hora e 30 minutos</i>.</li>
          <li><strong>Senha:</strong> se estiver incorreta, a API bloqueia a emissão antes de gerar certificados.</li>
        </ul>
      </section>

      <section>
        <h4>Quando aparecer erro</h4>
        <ul>
          <li><strong>Senha inválida:</strong> revise com a diretoria.</li>
          <li><strong>Servidor indisponível:</strong> confira conexão, backend e URL da API.</li>
          <li><strong>Dados recusados:</strong> revise campos obrigatórios ou formato da planilha.</li>
          <li><strong>Erro interno/timeout:</strong> pode envolver PDF, Drive, Sheets ou e-mail. Confira relatório, e-mail e Drive antes de reenviar.</li>
        </ul>
      </section>
    </div>
  );

  if (isMobile) {
    return (
      <>
        <button
          type="button"
          className="btn-ajuda-mobile"
          onClick={() => setModalAberto(true)}
          aria-haspopup="dialog"
          aria-expanded={modalAberto}
        >
          (?) Dúvidas sobre o preenchimento
        </button>

        {modalAberto && (
          <div className="modal-overlay" onClick={() => setModalAberto(false)}>
            <div className="modal-content" role="dialog" aria-modal="true" aria-label="Dúvidas sobre o preenchimento" onClick={(e) => e.stopPropagation()}>
              <button className="btn-fechar" type="button" aria-label="Fechar ajuda" onClick={() => setModalAberto(false)}>
                <X size={18} />
              </button>
              {conteudoAjuda}
            </div>
          </div>
        )}
      </>
    );
  }

  return (
    <>
      <button
        type="button"
        className={`btn-ajuda-desktop-floating ${painelAberto ? 'is-hidden' : ''}`}
        onClick={() => setPainelAberto(true)}
        aria-expanded={painelAberto}
        aria-controls="painel-ajuda-desktop"
      >
        <PanelRightOpen size={18} />
        <span>Dicas</span>
      </button>

      <aside
        id="painel-ajuda-desktop"
        className={`caixa-ajuda-desktop ${painelAberto ? 'is-open' : 'is-closed'}`}
        aria-label="Dúvidas sobre o preenchimento"
        aria-hidden={!painelAberto}
      >
      <button
        type="button"
        className="btn-ajuda-desktop-toggle"
        onClick={() => setPainelAberto(false)}
        aria-expanded={painelAberto}
        aria-label="Recolher guia de preenchimento"
        title="Recolher guia"
      >
        <PanelRightClose size={18} />
      </button>

      <div className="ajuda-desktop-content">
        {conteudoAjuda}
      </div>
      </aside>
    </>
  );
}
