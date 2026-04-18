import { useState } from 'react';
import axios from 'axios';
import toast, { Toaster } from 'react-hot-toast';
import CertificadoForm from './components/CertificadoForm';
import RelatorioEmissao from './components/RelatorioEmissao';
import './App.css';

function App() {
  const [formData, setFormData] = useState({ nomeEvento: '', dataRealizacao: '', cargaHoraria: '' });
  const [arquivoCsv, setArquivoCsv] = useState(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [resultados, setResultados] = useState(null);
  const [apiKey, setApiKey] = useState(localStorage.getItem('casis_api_key') || '');

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!arquivoCsv) return toast.error('Anexe a planilha CSV!');
    if (!apiKey) return toast.error('Informe a senha de autorização!');

    setIsSubmitting(true);
    const toastId = toast.loading('Processando lote e salvando no Drive...');

    const payload = new FormData();
    payload.append('nomeEvento', formData.nomeEvento);
    payload.append('dataRealizacao', formData.dataRealizacao);
    payload.append('cargaHoraria', formData.cargaHoraria);
    payload.append('arquivoCsv', arquivoCsv);

    try {
      const response = await axios.post(`${import.meta.env.VITE_API_URL}/api/certificados/emitir-lote`, payload, {
        headers: { 'X-API-KEY': apiKey }
      });

      setResultados(response.data);
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
              />
          ) : (
              <RelatorioEmissao resultados={resultados} onReset={resetForm} />
          )}
        </div>

        <footer className="casis-footer">
          &copy; {new Date().getFullYear()} Centro Acadêmico de Sistemas de Informação UTFPR-Londrina<br/>
          Desenvolvido por <a href="https://linktr.ee/raphaelfeijosalles" target="_blank" rel="noopener noreferrer">Raphael Feijó</a>
        </footer>
      </>
  );
}

export default App;