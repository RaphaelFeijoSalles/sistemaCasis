import { useState, useCallback } from 'react';
import { useDropzone } from 'react-dropzone';
import axios from 'axios';
import { UploadCloud, CheckCircle, Loader2, FileSpreadsheet } from 'lucide-react';
import toast, { Toaster } from 'react-hot-toast';
import './App.css';

function App() {
  const [formData, setFormData] = useState({
    nomeEvento: '',
    dataRealizacao: '',
    cargaHoraria: ''
  });
  const [arquivoCsv, setArquivoCsv] = useState(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Manipulador de input de texto
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  // Configuração da área de Drag and Drop (aceita apenas .csv)
  const onDrop = useCallback(acceptedFiles => {
    if (acceptedFiles.length > 0) {
      setArquivoCsv(acceptedFiles[0]);
      toast.success('Planilha carregada com sucesso!');
    }
  }, []);

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: { 'text/csv': ['.csv'] },
    maxFiles: 1
  });

  // Função de disparo para a API Spring Boot
  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!arquivoCsv) {
      toast.error('Você esqueceu de anexar a planilha CSV!');
      return;
    }

    setIsSubmitting(true);
    const toastId = toast.loading('Processando lote e disparando e-mails...');

    // O FormData permite empacotar arquivos binários junto com strings
    const payload = new FormData();
    payload.append('nomeEvento', formData.nomeEvento);
    payload.append('dataRealizacao', formData.dataRealizacao);
    payload.append('cargaHoraria', formData.cargaHoraria);
    payload.append('arquivoCsv', arquivoCsv);

    try {
      const response = await axios.post('http://localhost:8080/api/certificados/emitir-lote', payload, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });

      toast.success(response.data, { id: toastId, duration: 5000 });

      // Limpa o formulário após o sucesso
      setFormData({ nomeEvento: '', dataRealizacao: '', cargaHoraria: '' });
      setArquivoCsv(null);

    } catch (error) {
      const msgErro = error.response?.data || 'Erro de conexão com o servidor.';
      toast.error(msgErro, { id: toastId, duration: 6000 });
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
      <div className="casis-container">
        <Toaster position="top-right" />

        <div className="header">
          <h1>Emissão de Certificados</h1>
          <p>Sistema Oficial do CASIS - Eventos</p>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Nome Oficial do Evento</label>
            <input
                type="text"
                name="nomeEvento"
                required
                placeholder="Ex: I Semana de Tecnologia"
                value={formData.nomeEvento}
                onChange={handleInputChange}
            />
          </div>

          <div style={{ display: 'flex', gap: '20px' }}>
            <div className="form-group" style={{ flex: 1 }}>
              <label>Data de Realização</label>
              <input
                  type="date"
                  name="dataRealizacao"
                  required
                  value={formData.dataRealizacao}
                  onChange={handleInputChange}
              />
            </div>

            <div className="form-group" style={{ flex: 1 }}>
              <label>Carga Horária (Horas)</label>
              <input
                  type="number"
                  name="cargaHoraria"
                  required
                  min="1"
                  placeholder="Ex: 10"
                  value={formData.cargaHoraria}
                  onChange={handleInputChange}
              />
            </div>
          </div>

          <div {...getRootProps()} className={`dropzone ${isDragActive ? 'active' : ''}`}>
            <input {...getInputProps()} />
            {arquivoCsv ? (
                <div>
                  <CheckCircle size={40} color="#4CAF50" style={{ margin: '0 auto' }} />
                  <p style={{ color: '#E0E0E0', fontWeight: 'bold' }}>{arquivoCsv.name}</p>
                  <p style={{ fontSize: '12px' }}>Clique para trocar de arquivo</p>
                </div>
            ) : (
                <div>
                  <UploadCloud size={40} color="#888" style={{ margin: '0 auto' }} />
                  {isDragActive ? (
                      <p style={{ color: '#4CAF50' }}>Solte a planilha aqui...</p>
                  ) : (
                      <p>Arraste a planilha CSV do Forms aqui,<br/>ou clique para selecionar</p>
                  )}
                </div>
            )}
          </div>

          <button type="submit" className="btn-submit" disabled={isSubmitting}>
            {isSubmitting ? (
                <><Loader2 className="spin" size={20} /> Processando...</>
            ) : (
                <><FileSpreadsheet size={20} /> Emitir e Enviar E-mails</>
            )}
          </button>
        </form>
      </div>
  );
}

export default App;