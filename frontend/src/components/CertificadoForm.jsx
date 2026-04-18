import { useDropzone } from 'react-dropzone';
import { useCallback } from 'react';
import { UploadCloud, Loader2, FileSpreadsheet, KeyRound } from 'lucide-react';
import toast from 'react-hot-toast';

export default function CertificadoForm({ formData, setFormData, arquivoCsv, setArquivoCsv, apiKey, setApiKey, isSubmitting, onSubmit }) {

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const onDrop = useCallback(acceptedFiles => {
        if (acceptedFiles.length > 0) {
            setArquivoCsv(acceptedFiles[0]);
            toast.success('Planilha carregada!');
        }
    }, [setArquivoCsv]);

    const { getRootProps, getInputProps, isDragActive } = useDropzone({ onDrop, accept: { 'text/csv': ['.csv'] }, maxFiles: 1 });

    return (
        <form onSubmit={onSubmit}>
            <div className="form-group">
                <label>Nome Oficial do Evento</label>
                <input type="text" name="nomeEvento" required placeholder="Ex: Recepção de Calouros" value={formData.nomeEvento} onChange={handleInputChange} />
            </div>

            <div className="responsive-row">
                <div className="form-group" style={{ flex: 1 }}>
                    <label>Data de Realização</label>
                    <input type="date" name="dataRealizacao" required value={formData.dataRealizacao} onChange={handleInputChange}/>
                </div>
                <div className="form-group" style={{ flex: 1 }}>
                    <label>Carga Horária (Horas)</label>
                    <input type="number" name="cargaHoraria" required min="1" placeholder="Ex: 2" value={formData.cargaHoraria} onChange={handleInputChange}/>
                </div>
            </div>

            <div {...getRootProps()} className={`dropzone ${isDragActive ? 'active' : ''}`}>
                <input {...getInputProps()} />
                {arquivoCsv ? (
                    <div className="file-info"><FileSpreadsheet size={32} /><span>{arquivoCsv.name}</span></div>
                ) : (
                    <div><UploadCloud size={32} style={{ margin: '0 auto', marginBottom: '10px' }} /><p>Clique ou arraste a planilha CSV aqui</p></div>
                )}
            </div>

            <div className="form-group">
                <label>Senha de Autorização (Diretoria)</label>
                <div style={{ position: 'relative' }}>
                    <input
                        type="password" placeholder="••••••••" required value={apiKey}
                        onChange={(e) => { setApiKey(e.target.value); localStorage.setItem('casis_api_key', e.target.value); }}
                        style={{ paddingLeft: '2.5rem' }}
                    />
                    <KeyRound size={18} color="#888" style={{ position: 'absolute', left: '0.8rem', top: '50%', transform: 'translateY(-50%)' }} />
                </div>
            </div>

            <button type="submit" className="btn-submit" disabled={isSubmitting}>
                {isSubmitting ? <Loader2 className="spin" size={20} /> : <FileSpreadsheet size={20} />}
                {isSubmitting ? 'Gerando e Enviando...' : 'Disparar Certificados'}
            </button>
        </form>
    );
}