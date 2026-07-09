import { useState } from 'react';
import axios from 'axios';
import toast, { Toaster } from 'react-hot-toast';
import CertificadoForm from './components/CertificadoForm';
import RelatorioEmissao from './components/RelatorioEmissao';
import AjudaEmissao from './components/AjudaEmissao';
import './App.css';

function App() {
  const [formData, setFormData] = useState({
    nomeEvento: '', dataRealizacao: '', cargaHoraria: '',
    nomeParticipante: '', emailParticipante: '', raParticipante: ''
  });
  const [arquivoCsv, setArquivoCsv] = useState(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [resultados, setResultados] = useState(null);
  const [apiKey, setApiKey] = useState(localStorage.getItem('casis_api_key') || '');
  const [emissaoTipo, setEmissaoTipo] = useState('lote');

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (emissaoTipo === 'lote' && !arquivoCsv) return toast.error('Anexe a planilha CSV!');
    if (!apiKey) return toast.error('Informe a senha de autorização!');

    setIsSubmitting(true);
    const toastId = toast.loading(emissaoTipo === 'lote' ? 'Processando lote...' : 'Gerando certificado único...');

    try {
      let response;
      if (emissaoTipo === 'lote') {
        const payload = new FormData();
        payload.append('nomeEvento', formData.nomeEvento);
        payload.append('dataRealizacao', formData.dataRealizacao);
        payload.append('cargaHoraria', formData.cargaHoraria);
        payload.append('arquivoCsv', arquivoCsv);

        response = await axios.post(`${import.meta.env.VITE_API_URL}/api/certificados/emitir-lote`, payload, {
          headers: { 'X-API-KEY': apiKey }
        });
        setResultados(response.data);
      } else {
        response = await axios.post(`${import.meta.env.VITE_API_URL}/api/certificados/emitir-unitario`, formData, {
          headers: { 'X-API-KEY': apiKey }
        });
        // Transforma o objeto único num Array de 1 item para a tabela RelatorioEmissao conseguir ler
        setResultados([response.data]);
      }

      toast.success('Emissão concluída!', { id: toastId, duration: 5000 });
      setArquivoCsv(null);
    } catch (error) {
      toast.error(error.response?.data || 'Erro de conexão com o servidor.', { id: toastId, duration: 6000 });
    } finally {
      setIsSubmitting(false);
    }
  };

  const resetForm = () => {
    setResultados(null);
    setArquivoCsv(null);
    setFormData({ nomeEvento: '', dataRealizacao: '', cargaHoraria: '' });
  };

  return (
      <>
        <div className="main-wrapper">
          <div className="casis-container">
          <Toaster position="top-right" />

          <div className="header">
            <h1>Emissão de Certificados</h1>
            <p>Sistema Oficial do CASIS - Eventos</p>
          </div>

          {!resultados ? (
              <CertificadoForm
                  formData={formData} setFormData={setFormData}
                  arquivoCsv={arquivoCsv} setArquivoCsv={setArquivoCsv}
                  apiKey={apiKey} setApiKey={setApiKey}
                  isSubmitting={isSubmitting} onSubmit={handleSubmit}
                  emissaoTipo={emissaoTipo} setEmissaoTipo={setEmissaoTipo}
              />
          ) : (
              <RelatorioEmissao resultados={resultados} onReset={resetForm} />
          )}
          </div>
          {!resultados && <AjudaEmissao emissaoTipo={emissaoTipo} />}
        </div>

        <footer className="casis-footer">
          &copy; {new Date().getFullYear()} Centro Acadêmico de Sistemas de Informação UTFPR-Londrina<br/>
          Desenvolvido por <a href="https://linktr.ee/raphaelfeijosalles" target="_blank" rel="noopener noreferrer">Raphael Salles</a>
        </footer>
      </>
  );
}

export default App;