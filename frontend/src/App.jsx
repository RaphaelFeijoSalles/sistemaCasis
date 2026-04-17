import { useState, useCallback } from 'react';
import { useDropzone } from 'react-dropzone';
import axios from 'axios';
import {
  UploadCloud, Loader2, FileSpreadsheet, KeyRound, ChevronLeft, MailCheck, MailWarning
} from 'lucide-react';
import toast, { Toaster } from 'react-hot-toast';
import './App.css';

function App() {
  const [formData, setFormData] = useState({ nomeEvento: '', dataRealizacao: '', cargaHoraria: '' });
  const [arquivoCsv, setArquivoCsv] = useState(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [resultados, setResultados] = useState(null); // Resgatado!
  const [apiKey, setApiKey] = useState(localStorage.getItem('casis_api_key') || '');

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

  const { getRootProps, getInputProps, isDragActive } = useDropzone({ onDrop, accept: { 'text/csv': ['.csv'] }, maxFiles: 1 });

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
        headers: {
          'X-API-KEY': apiKey
        }
      });

      setResultados(response.data); // Exibe a tabela de relatório
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
      <div className="casis-container">
        <Toaster position="top-right" />
        <div className="header">
          <h1>Emissão de Certificados</h1>
          <p>Sistema Oficial do CASIS - Eventos</p>
        </div>

        {!resultados ? (
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Nome Oficial do Evento</label>
                <input type="text" name="nomeEvento" required placeholder="Ex: Recepção de Calouros" value={formData.nomeEvento} onChange={handleInputChange} />
              </div>

              <div className="responsive-row">
                <div className="form-group" style={{ flex: 1 }}>
                  <label>Data</label>
                  <input type="date" name="dataRealizacao" required value={formData.dataRealizacao} onChange={handleInputChange}/>
                </div>
                <div className="form-group" style={{ flex: 1 }}>
                  <label>Carga Horária</label>
                  <input type="number" name="cargaHoraria" required min="1" placeholder="Ex: 2" value={formData.cargaHoraria} onChange={handleInputChange}/>
                </div>
              </div>

              <div {...getRootProps()} className={`dropzone ${isDragActive ? 'active' : ''}`}>
                <input {...getInputProps()} />
                {arquivoCsv ? (
                    <div className="file-info"><FileSpreadsheet size={32} /><span>{arquivoCsv.name}</span></div>
                ) : (
                    <div><UploadCloud size={32} style={{ margin: '0 auto' }} /><p>Clieque ou arraste a planilha CSV aqui</p></div>
                )}
              </div>

              <div className="form-group">
                <label>Senha de Autorização (Diretoria)</label>
                <div style={{ position: 'relative' }}>
                  <input
                      type="password" placeholder="••••••••" required value={apiKey}
                      onChange={(e) => { setApiKey(e.target.value); localStorage.setItem('casis_api_key', e.target.value); }}
                      style={{ width: '100%', paddingLeft: '2.5rem' }}
                  />
                  <KeyRound size={18} color="#888" style={{ position: 'absolute', left: '0.8rem', top: '50%', transform: 'translateY(-50%)' }} />
                </div>
              </div>

              <button type="submit" className="btn-submit" disabled={isSubmitting}>
                {isSubmitting ? <Loader2 className="spin" size={20} /> : <FileSpreadsheet size={20} />}
                {isSubmitting ? 'Processando Lote...' : 'Disparar Certificados'}
              </button>
            </form>
        ) : (
            <div className="results-area">
              <div className="results-summary">
                <div className="stat"><span className="label">Sucessos</span><span className="value success">{resultados.filter(r => r.sucesso).length}</span></div>
                <div className="stat"><span className="label">Falhas</span><span className="value error">{resultados.filter(r => !r.sucesso).length}</span></div>
              </div>
              <div className="table-wrapper">
                <table className="results-table">
                  <thead><tr><th>Participante</th><th>Status</th></tr></thead>
                  <tbody>
                  {resultados.map((res, index) => (
                      <tr key={index} className={res.sucesso ? 'row-success' : 'row-error'}>
                        <td><div className="name">{res.nome}</div><div className="email">{res.email}</div></td>
                        <td className="status-cell">
                          {res.sucesso ? <MailCheck size={18} className="icon-success" /> :
                              <div className="error-hint" title={res.mensagemErro}><MailWarning size={18} className="icon-error" /><span>Erro</span></div>}
                        </td>
                      </tr>
                  ))}
                  </tbody>
                </table>
              </div>
              <button onClick={resetForm} className="btn-secondary"><ChevronLeft size={18} /> Novo Lote</button>
            </div>
        )}
      </div>
  );
}

export default App;