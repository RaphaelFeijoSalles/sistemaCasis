import { useState, useCallback } from 'react';
import { useDropzone } from 'react-dropzone';
import axios from 'axios';
import {
  UploadCloud, CheckCircle, Loader2, FileSpreadsheet,
  AlertCircle, ChevronLeft, MailCheck, MailWarning
} from 'lucide-react';
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

  // Novo estado para armazenar o relatório vindo do backend
  const [resultados, setResultados] = useState(null);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const onDrop = useCallback(acceptedFiles => {
    if (acceptedFiles.length > 0) {
      setArquivoCsv(acceptedFiles[0]);
      toast.success('Planilha carregada!');
    }
  }, []);

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: { 'text/csv': ['.csv'] },
    maxFiles: 1
  });

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!arquivoCsv) {
      toast.error('Anexe a planilha CSV!');
      return;
    }

    setIsSubmitting(true);
    const toastId = toast.loading('Emitindo certificados...');

    const payload = new FormData();
    payload.append('nomeEvento', formData.nomeEvento);
    payload.append('dataRealizacao', formData.dataRealizacao);
    payload.append('cargaHoraria', formData.cargaHoraria);
    payload.append('arquivoCsv', arquivoCsv);

    try {
      // O backend agora retorna List<ResultadoEmissaoDTO>
      const response = await axios.post('http://localhost:8080/api/certificados/emitir-lote', payload);

      setResultados(response.data);
      toast.success('Processamento concluído!', { id: toastId });

    } catch (error) {
      const msgErro = error.response?.data || 'Erro de conexão.';
      toast.error(msgErro, { id: toastId });
    } finally {
      setIsSubmitting(false);
    }
  };

  // Função para voltar ao formulário e limpar tudo
  const resetForm = () => {
    setResultados(null);
    setArquivoCsv(null);
    setFormData({ nomeEvento: '', dataRealizacao: '', cargaHoraria: '' });
  };

  return (
      <div className="casis-container">
        <Toaster position="top-right" />

        <div className="header">
          <h1>Emissão de Certificados</h1>
          <p>Sistema Oficial do CASIS - v1.0</p>
        </div>

        {!resultados ? (
            /* --- VISÃO DO FORMULÁRIO --- */
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Nome Oficial do Evento</label>
                <input type="text" name="nomeEvento" required value={formData.nomeEvento} onChange={handleInputChange} placeholder="Ex: Semana acadêmica"/>
              </div>

              <div style={{ display: 'flex', gap: '20px' }}>
                <div className="form-group" style={{ flex: 1 }}>
                  <label>Data</label>
                  <input type="date" name="dataRealizacao" required value={formData.dataRealizacao} onChange={handleInputChange}/>
                </div>
                <div className="form-group" style={{ flex: 1 }}>
                  <label>Horas</label>
                  <input type="number" name="cargaHoraria" required value={formData.cargaHoraria} onChange={handleInputChange} placeholder="Ex: 2"/>
                </div>
              </div>

              <div {...getRootProps()} className={`dropzone ${isDragActive ? 'active' : ''}`}>
                <input {...getInputProps()} />
                {arquivoCsv ? (
                    <div className="file-info">
                      <FileSpreadsheet size={32} color="#4CAF50" />
                      <span>{arquivoCsv.name}</span>
                    </div>
                ) : (
                    <div className="drop-info">
                      <UploadCloud size={32} />
                      <p>Arraste o CSV de presença aqui</p>
                    </div>
                )}
              </div>

              <button type="submit" className="btn-submit" disabled={isSubmitting}>
                {isSubmitting ? <Loader2 className="spin" /> : 'Disparar Certificados'}
              </button>
            </form>
        ) : (
            /* --- VISÃO DO RELATÓRIO (BLINDAGEM) --- */
            <div className="results-area">
              <div className="results-summary">
                <div className="stat">
                  <span className="label">Sucessos</span>
                  <span className="value success">{resultados.filter(r => r.sucesso).length}</span>
                </div>
                <div className="stat">
                  <span className="label">Falhas</span>
                  <span className="value error">{resultados.filter(r => !r.sucesso).length}</span>
                </div>
              </div>

              <div className="table-wrapper">
                <table className="results-table">
                  <thead>
                  <tr>
                    <th>Participante</th>
                    <th>Status</th>
                  </tr>
                  </thead>
                  <tbody>
                  {resultados.map((res, index) => (
                      <tr key={index} className={res.sucesso ? 'row-success' : 'row-error'}>
                        <td>
                          <div className="name">{res.nome}</div>
                          <div className="email">{res.email}</div>
                        </td>
                        <td className="status-cell">
                          {res.sucesso ? (
                              <MailCheck size={18} className="icon-success" />
                          ) : (
                              <div className="error-hint" title={res.mensagemErro}>
                                <MailWarning size={18} className="icon-error" />
                                <span>Erro</span>
                              </div>
                          )}
                        </td>
                      </tr>
                  ))}
                  </tbody>
                </table>
              </div>

              <button onClick={resetForm} className="btn-secondary">
                <ChevronLeft size={18} /> Novo Lote
              </button>
            </div>
        )}
      </div>
  );
}

export default App;