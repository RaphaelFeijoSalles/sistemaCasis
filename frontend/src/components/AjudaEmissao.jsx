import { useState, useEffect } from 'react';
import './AjudaEmissao.css';

export default function AjudaEmissao({ emissaoTipo }) {
  const [modalAberto, setModalAberto] = useState(false);
  const [isMobile, setIsMobile] = useState(window.innerWidth <= 850);

  useEffect(() => {
    const handleResize = () => setIsMobile(window.innerWidth <= 850);
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  const conteudoAjuda = (
    <div className="conteudo-ajuda">
      <h3>📌 Guia de Preenchimento</h3>

      {emissaoTipo === 'lote' && (
        <section>
          <h4>Como formatar a Planilha CSV?</h4>
          <ul>
            <li><strong>Formato:</strong> Salve sua planilha como <code>.csv</code> (Valores Separados por Vírgula).</li>
            <li><strong>Cabeçalho:</strong> A primeira linha deve conter os cabeçalhos.</li>
            <li><strong>Ordem das Colunas (Da esquerda para a direita):</strong>
              <ol>
                <li>Nome Completo</li>
                <li>RA (Registro Acadêmico)</li>
                <li>E-mail</li>
              </ol>
            </li>
            <li><strong>Atenção:</strong> Revise se não há linhas em branco no final do arquivo.</li>
          </ul>
        </section>
      )}

      <section>
        <h4>Como o Certificado é Montado?</h4>
        <ul>
          <li><strong>Nome do Evento:</strong> Vai no assunto do e-mail, título do arquivo no Drive e impresso em destaque no corpo do certificado.</li>
          <li><strong>Carga Horária:</strong> Suporta horas quebradas! Se digitar <code>1.5</code>, o certificado sairá com <i>"1 hora e 30 minutos"</i> impresso por extenso.</li>
          <li><strong>Data:</strong> Será automaticamente formatada para o padrão brasileiro extenso (ex: <i>22 de Junho de 2026</i>).</li>
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
        >
          (?) Dúvidas sobre o preenchimento
        </button>

        {modalAberto && (
          <div className="modal-overlay" onClick={() => setModalAberto(false)}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
              <button className="btn-fechar" onClick={() => setModalAberto(false)}>X</button>
              {conteudoAjuda}
            </div>
          </div>
        )}
      </>
    );
  }

  return (
    <aside className="caixa-ajuda-desktop">
      {conteudoAjuda}
    </aside>
  );
}
