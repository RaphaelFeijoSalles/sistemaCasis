import { useLayoutEffect, useRef, useState } from 'react';
import axios from 'axios';
import toast, { Toaster } from 'react-hot-toast';
import CertificadoForm from './components/CertificadoForm';
import RelatorioEmissao from './components/RelatorioEmissao';
import PreVisualizacaoLote from './components/PreVisualizacaoLote';
import AjudaEmissao from './components/AjudaEmissao';
import './App.css';
import { CURSO_PADRAO } from './cursos';

const API_BASE_URL = import.meta.env.VITE_API_URL;

const campoVazio = (valor) => !String(valor ?? '').trim();

const emailValido = (email) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(String(email ?? '').trim());

const validarCargaHoraria = (valor) => {
  const numero = Number.parseFloat(String(valor).replace(',', '.'));
  // Mesmos limites do backend: pelo menos 1 minuto depois do arredondamento e no máximo 1.000 horas
  return Number.isFinite(numero) && numero <= 1000 && Math.round(numero * 60) >= 1;
};

const validarCsv = (arquivo) => {
  if (!arquivo) {
    return {
      titulo: 'Planilha CSV não anexada',
      detalhe: 'No modo lote, selecione um arquivo .csv com as colunas Nome, RA e E-mail antes de pré-visualizar.'
    };
  }

  if (arquivo.size === 0) {
    return {
      titulo: 'Planilha vazia',
      detalhe: 'O arquivo selecionado não tem conteúdo. Exporte a planilha novamente em CSV e tente de novo.'
    };
  }

  if (!arquivo.name.toLowerCase().endsWith('.csv')) {
    return {
      titulo: 'Formato de planilha inválido',
      detalhe: `O arquivo "${arquivo.name}" não parece ser .csv. Exporte pelo Google Sheets em Arquivo > Fazer download > CSV.`
    };
  }

  return null;
};

const validarFormulario = ({ formData, arquivoCsv, apiKey, emissaoTipo }) => {
  if (!API_BASE_URL) {
    return {
      titulo: 'API não configurada',
      detalhe: 'A variável VITE_API_URL não está definida no frontend. Sem ela, o site não sabe para qual servidor enviar a emissão.'
    };
  }

  if (campoVazio(apiKey)) {
    return {
      titulo: 'Senha de autorização ausente',
      detalhe: 'Informe a senha da diretoria no campo final do formulário. Ela é enviada no cabeçalho X-API-KEY.'
    };
  }

  if (campoVazio(formData.nomeEvento)) {
    return {
      titulo: 'Nome do evento obrigatório',
      detalhe: 'Preencha o nome oficial do evento. Ele aparece no certificado, no e-mail e no nome do arquivo.'
    };
  }

  if (campoVazio(formData.dataRealizacao)) {
    return {
      titulo: 'Data de realização obrigatória',
      detalhe: 'Selecione a data do evento para que o certificado seja emitido com a data correta.'
    };
  }

  if (!validarCargaHoraria(formData.cargaHoraria)) {
    return {
      titulo: 'Carga horária inválida',
      detalhe: 'Use um número maior que zero e de no máximo 1000 horas. Exemplos aceitos: 2 para duas horas, 1.5 para uma hora e meia.'
    };
  }

  if (emissaoTipo === 'lote') {
    return validarCsv(arquivoCsv);
  }

  if (campoVazio(formData.nomeParticipante)) {
    return {
      titulo: 'Nome do participante obrigatório',
      detalhe: 'Na emissão unitária, informe o nome completo exatamente como deve aparecer no certificado.'
    };
  }

  if (!emailValido(formData.emailParticipante)) {
    return {
      titulo: 'E-mail do participante inválido',
      detalhe: 'Digite um e-mail completo, com usuário, @ e domínio. Exemplo: nome@alunos.utfpr.edu.br.'
    };
  }

  if (campoVazio(formData.raParticipante)) {
    return {
      titulo: 'RA obrigatório',
      detalhe: 'Informe o Registro Acadêmico do participante. Caso ele não tenha RA, confirme o procedimento antes de emitir.'
    };
  }

  return null;
};

const extrairDetalheServidor = (data) => {
  if (!data) return '';
  if (typeof data === 'string') return data;
  if (typeof data === 'object') {
    return data.message || data.error || data.detail || JSON.stringify(data);
  }
  return String(data);
};

const montarMensagemErro = (error, emissaoTipo, isPrevia = false) => {
  const modo = isPrevia ? 'a pré-visualização' : (emissaoTipo === 'lote' ? 'o lote' : 'a emissão unitária');

  if (axios.isAxiosError(error)) {
    if (error.code === 'ECONNABORTED') {
      return {
        titulo: 'Tempo de resposta esgotado',
        detalhe: isPrevia
          ? 'O servidor demorou demais para validar a planilha. Nenhum certificado foi emitido nesta etapa; tente novamente.'
          : `O servidor demorou demais para responder ${modo}. Verifique se alguns e-mails chegaram antes de tentar reenviar.`
      };
    }

    if (!error.response) {
      return {
        titulo: 'Não foi possível conectar ao servidor',
        detalhe: 'Confira sua conexão, se o backend está no ar e se a URL da API está correta. Nenhuma confirmação de emissão foi recebida.'
      };
    }

    const detalheServidor = extrairDetalheServidor(error.response.data);

    if (error.response.status === 400) {
      return {
        titulo: 'Dados recusados pelo servidor',
        detalhe: detalheServidor || (emissaoTipo === 'lote'
          ? 'Confira se a primeira linha do CSV é o cabeçalho e tem as colunas Nome e E-mail (RA é opcional; a ordem não importa).'
          : 'Confira nome, RA, e-mail, data e carga horária do participante.')
      };
    }

    if (error.response.status === 403) {
      return {
        titulo: 'Senha de autorização inválida',
        detalhe: detalheServidor || 'A senha informada não confere com a senha configurada no servidor. Revise o campo Senha de Autorização.'
      };
    }

    if (error.response.status === 404) {
      return {
        titulo: 'Rota da API não encontrada',
        detalhe: 'O frontend conseguiu acessar o servidor, mas o endpoint de emissão não foi encontrado. Confira a URL da API usada no deploy.'
      };
    }

    if (error.response.status === 413) {
      return {
        titulo: 'Arquivo muito grande',
        detalhe: 'A planilha enviada passou do limite aceito pelo servidor. Remova linhas desnecessárias ou divida a emissão em mais de um lote.'
      };
    }

    if (error.response.status === 415) {
      return {
        titulo: 'Tipo de arquivo não aceito',
        detalhe: 'Envie a planilha no formato CSV. Arquivos XLSX, ODS ou PDFs precisam ser exportados como .csv antes.'
      };
    }

    if (error.response.status >= 500) {
      return {
        titulo: isPrevia ? 'Erro interno durante a pré-visualização' : 'Erro interno durante a emissão',
        detalhe: detalheServidor || (isPrevia
          ? 'O servidor falhou ao validar a planilha. Nenhum certificado foi emitido nesta etapa.'
          : 'O servidor encontrou uma falha ao processar certificados, Drive, Sheets ou envio de e-mail. Verifique o relatório antes de reenviar.')
      };
    }

    return {
      titulo: `Falha na emissão (${error.response.status})`,
      detalhe: detalheServidor || `O servidor retornou um erro inesperado ao processar ${modo}.`
    };
  }

  return {
    titulo: 'Erro inesperado no navegador',
    detalhe: error?.message || 'Ocorreu uma falha antes de concluir o envio. Recarregue a página e tente novamente.'
  };
};

const ToastMensagem = ({ titulo, detalhe }) => (
  <div className="toast-message">
    <strong>{titulo}</strong>
    {detalhe && <span>{detalhe}</span>}
  </div>
);

// Mesmos nomes de campos de EmissaoLoteEventoRequestDTO; usado na prévia e na confirmação
const montarPayloadLote = (formData, arquivoCsv) => {
  const payload = new FormData();
  payload.append('nomeEvento', formData.nomeEvento.trim());
  payload.append('dataRealizacao', formData.dataRealizacao);
  payload.append('cargaHoraria', String(formData.cargaHoraria).replace(',', '.'));
  payload.append('cursoPadrao', formData.cursoPadrao);
  payload.append('arquivoCsv', arquivoCsv);
  return payload;
};

const contarStatus = (resultados) => ({
  sucessos: resultados.filter(r => r.status === 'SUCESSO').length,
  existentes: resultados.filter(r => r.status === 'EXISTENTE').length,
  falhas: resultados.filter(r => r.status === 'ERRO').length
});

function App() {
  const mainWrapperRef = useRef(null);
  const casisContainerRef = useRef(null);
  const [formData, setFormData] = useState({
    nomeEvento: '', dataRealizacao: '', cargaHoraria: '',
    nomeParticipante: '', emailParticipante: '', raParticipante: '',
    cursoPadrao: CURSO_PADRAO, cursoParticipante: CURSO_PADRAO
  });
  const [arquivoCsv, setArquivoCsv] = useState(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [resultados, setResultados] = useState(null);
  const [previa, setPrevia] = useState(null);
  // Etapas do lote: formulario -> previa -> relatorio (unitário vai direto para relatorio)
  const [etapa, setEtapa] = useState('formulario');
  const [apiKey, setApiKey] = useState(localStorage.getItem('casis_api_key') || '');
  const [emissaoTipo, setEmissaoTipo] = useState('lote');
  const [helpButtonTop, setHelpButtonTop] = useState(0);

  useLayoutEffect(() => {
    const updateHelpButtonTop = () => {
      if (!mainWrapperRef.current || !casisContainerRef.current) return;

      const wrapperTop = mainWrapperRef.current.getBoundingClientRect().top;
      const containerTop = casisContainerRef.current.getBoundingClientRect().top;
      setHelpButtonTop(Math.max(0, Math.round(containerTop - wrapperTop)));
    };

    updateHelpButtonTop();

    const resizeObserver = new ResizeObserver(updateHelpButtonTop);
    if (mainWrapperRef.current) resizeObserver.observe(mainWrapperRef.current);
    if (casisContainerRef.current) resizeObserver.observe(casisContainerRef.current);

    window.addEventListener('resize', updateHelpButtonTop);
    return () => {
      resizeObserver.disconnect();
      window.removeEventListener('resize', updateHelpButtonTop);
    };
  }, [arquivoCsv, emissaoTipo, formData.cargaHoraria, resultados, etapa]);

  const handleSubmit = async (e) => {
    e.preventDefault();

    const erroValidacao = validarFormulario({ formData, arquivoCsv, apiKey, emissaoTipo });
    if (erroValidacao) {
      toast.error(<ToastMensagem {...erroValidacao} />, { duration: 8000 });
      return;
    }

    // Lote: primeiro só valida (sem efeitos). A emissão acontece em confirmarLote.
    if (emissaoTipo === 'lote') {
      await preVisualizarLote();
    } else {
      await emitirUnitario();
    }
  };

  const exibirResultadoEmissao = (relatorio, toastId) => {
    const { sucessos, existentes, falhas } = contarStatus(relatorio);
    const titulo = falhas > 0 ? 'Emissão concluída com falhas' : 'Emissão concluída';
    const detalhe = emissaoTipo === 'lote'
      ? `Resultado do lote: ${sucessos} sucesso(s), ${existentes} já emitido(s), ${falhas} falha(s). Confira a tabela antes de reenviar.`
      : (falhas > 0 ? 'O participante ficou com erro. Veja o relatório para entender o motivo.' : 'Certificado unitário processado e registrado.');

    toast(falhas > 0 ? <ToastMensagem titulo={titulo} detalhe={detalhe} /> : detalhe, {
      id: toastId,
      duration: falhas > 0 ? 9000 : 5000,
      icon: falhas > 0 ? '!' : '✓'
    });
  };

  const preVisualizarLote = async () => {
    setIsSubmitting(true);
    const toastId = toast.loading('Validando a planilha. Nenhum certificado ou e-mail é enviado nesta etapa...');

    try {
      const response = await axios.post(`${API_BASE_URL}/api/certificados/pre-visualizar-lote`, montarPayloadLote(formData, arquivoCsv), {
        headers: { 'X-API-KEY': apiKey.trim() },
        timeout: 60000
      });
      const { validas, comAviso, comErro } = response.data;
      setPrevia(response.data);
      setEtapa('previa');
      toast(`Pré-visualização pronta: ${validas} válida(s), ${comAviso} com aviso, ${comErro} com erro. Confira antes de confirmar.`, {
        id: toastId,
        duration: 6000,
        icon: comErro > 0 ? '!' : '✓'
      });
    } catch (error) {
      const mensagem = montarMensagemErro(error, emissaoTipo, true);
      toast.error(<ToastMensagem {...mensagem} />, { id: toastId, duration: 9000 });
    } finally {
      setIsSubmitting(false);
    }
  };

  const confirmarLote = async () => {
    setIsSubmitting(true);
    const toastId = toast.loading('Processando lote: gerando PDFs, enviando e-mails e registrando backup...');

    try {
      // Reenvia o mesmo CSV e os mesmos campos; o backend revalida antes de emitir
      const response = await axios.post(`${API_BASE_URL}/api/certificados/emitir-lote`, montarPayloadLote(formData, arquivoCsv), {
        headers: { 'X-API-KEY': apiKey.trim() },
        timeout: 120000
      });
      setResultados(response.data);
      setPrevia(null);
      setEtapa('relatorio');
      exibirResultadoEmissao(response.data, toastId);
      setArquivoCsv(null);
    } catch (error) {
      const mensagem = montarMensagemErro(error, emissaoTipo);
      toast.error(<ToastMensagem {...mensagem} />, { id: toastId, duration: 9000 });
    } finally {
      setIsSubmitting(false);
    }
  };

  const emitirUnitario = async () => {
    setIsSubmitting(true);
    const toastId = toast.loading('Gerando certificado unitário, enviando e-mail e registrando backup...');

    try {
      const payload = {
        ...formData,
        nomeEvento: formData.nomeEvento.trim(),
        cargaHoraria: String(formData.cargaHoraria).replace(',', '.'),
        nomeParticipante: formData.nomeParticipante.trim(),
        emailParticipante: formData.emailParticipante.trim(),
        raParticipante: formData.raParticipante.trim()
      };

      const response = await axios.post(`${API_BASE_URL}/api/certificados/emitir-unitario`, payload, {
        headers: { 'X-API-KEY': apiKey.trim() },
        timeout: 120000
      });
      // Transforma o objeto único num Array de 1 item para a tabela RelatorioEmissao conseguir ler
      setResultados([response.data]);
      setEtapa('relatorio');
      exibirResultadoEmissao([response.data], toastId);
      setArquivoCsv(null);
    } catch (error) {
      const mensagem = montarMensagemErro(error, emissaoTipo);
      toast.error(<ToastMensagem {...mensagem} />, { id: toastId, duration: 9000 });
    } finally {
      setIsSubmitting(false);
    }
  };

  // Volta da prévia ao formulário mantendo os campos e o CSV para edição
  const voltarParaFormulario = () => {
    setPrevia(null);
    setEtapa('formulario');
  };

  const resetForm = () => {
    setResultados(null);
    setPrevia(null);
    setEtapa('formulario');
    setArquivoCsv(null);
    setFormData({
      nomeEvento: '', dataRealizacao: '', cargaHoraria: '',
      nomeParticipante: '', emailParticipante: '', raParticipante: '',
      cursoPadrao: CURSO_PADRAO, cursoParticipante: CURSO_PADRAO
    });
  };

  return (
      <>
        <div
            className="main-wrapper"
            ref={mainWrapperRef}
            style={{ '--help-button-top': `${helpButtonTop}px` }}
        >
          <div className="casis-container" ref={casisContainerRef}>
          <Toaster position="top-right" />

          <div className="header">
            <h1>Emissão de Certificados</h1>
            <p>Sistema Oficial do DACOMP - Eventos</p>
          </div>

          {etapa === 'formulario' && (
              <CertificadoForm
                  formData={formData} setFormData={setFormData}
                  arquivoCsv={arquivoCsv} setArquivoCsv={setArquivoCsv}
                  apiKey={apiKey} setApiKey={setApiKey}
                  isSubmitting={isSubmitting} onSubmit={handleSubmit}
                  emissaoTipo={emissaoTipo} setEmissaoTipo={setEmissaoTipo}
              />
          )}
          {etapa === 'previa' && previa && (
              <PreVisualizacaoLote
                  previa={previa} isSubmitting={isSubmitting}
                  onConfirmar={confirmarLote} onVoltar={voltarParaFormulario}
              />
          )}
          {etapa === 'relatorio' && resultados && (
              <RelatorioEmissao resultados={resultados} onReset={resetForm} />
          )}
          </div>
          {etapa === 'formulario' && <AjudaEmissao emissaoTipo={emissaoTipo} />}
        </div>

        <footer className="casis-footer">
          &copy; {new Date().getFullYear()} Diretório Acadêmico de Computação (DACOMP) UTFPR-Londrina<br/>
          Desenvolvido por <a href="https://linktr.ee/raphaelfeijosalles" target="_blank" rel="noopener noreferrer">Raphael Salles</a>
        </footer>
      </>
  );
}

export default App;
