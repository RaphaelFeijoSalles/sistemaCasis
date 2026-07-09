import { useDropzone } from 'react-dropzone';
import { useCallback } from 'react';
import { UploadCloud, Loader2, FileSpreadsheet, KeyRound, User } from 'lucide-react';
import toast from 'react-hot-toast';

export default function CertificadoForm({
                                            formData, setFormData, arquivoCsv, setArquivoCsv, apiKey, setApiKey, isSubmitting, onSubmit,
                                            emissaoTipo, setEmissaoTipo // Novas propriedades
                                        }) {

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

    const formatarTempoTexto = (valor) => {
        const num = parseFloat(valor);
        if (isNaN(num) || num <= 0) return "";
        const minutosTotais = Math.round(num * 60);
        const h = Math.floor(minutosTotais / 60);
        const m = minutosTotais % 60;
        let texto = "";
        if (h > 0) texto += `${h} hora${h > 1 ? 's' : ''}`;
        if (m > 0) texto += `${h > 0 ? ' e ' : ''}${m} minuto${m > 1 ? 's' : ''}`;
        return texto;
    };

    return (
        <form onSubmit={onSubmit}>
            {/* O Toggle de Seleção */}
            <div className="toggle-container">
                <button type="button" className={`toggle-btn ${emissaoTipo === 'lote' ? 'active' : ''}`} onClick={() => setEmissaoTipo('lote')}>
                    Lote (Planilha)
                </button>
                <button type="button" className={`toggle-btn ${emissaoTipo === 'individual' ? 'active' : ''}`} onClick={() => setEmissaoTipo('individual')}>
                    Participante Único
                </button>
            </div>

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
                    <input type="number" name="cargaHoraria" step="0.5" required min="0.5" placeholder="Ex: 1.5 para 1h30m" value={formData.cargaHoraria} onChange={handleInputChange}/>
                    {formData.cargaHoraria && (
                        <small style={{ color: 'var(--success)', display: 'block', marginTop: '6px' }}>
                            Sairá como: <strong>{formatarTempoTexto(formData.cargaHoraria)}</strong>
                        </small>
                    )}
                </div>
            </div>

            {/* Renderização Condicional: Dropzone vs Campos Manuais */}
            {emissaoTipo === 'lote' ? (
                <div {...getRootProps()} className={`dropzone ${isDragActive ? 'active' : ''}`}>
                    <input {...getInputProps()} />
                    {arquivoCsv ? (
                        <div className="file-info"><FileSpreadsheet size={32} /><span>{arquivoCsv.name}</span></div>
                    ) : (
                        <div><UploadCloud size={32} style={{ margin: '0 auto', marginBottom: '10px' }} /><p>Clique ou arraste a planilha CSV aqui</p></div>
                    )}
                </div>
            ) : (
                <div className="unit-fields">
                    <div className="form-group">
                        <label>Nome do Participante</label>
                        <input type="text" name="nomeParticipante" required placeholder="Nome Completo" value={formData.nomeParticipante} onChange={handleInputChange} />
                    </div>
                    <div className="responsive-row">
                        <div className="form-group" style={{ flex: 2 }}>
                            <label>E-mail Institucional</label>
                            <input 
                                type="email" 
                                name="emailParticipante" 
                                required 
                                placeholder="exemplo@email.com"
                                value={formData.emailParticipante} 
                                onChange={handleInputChange}
                            />
                        </div>
                        <div className="form-group" style={{ flex: 1 }}>
                            <label>RA</label>
                            <input type="text" name="raParticipante" required placeholder="Ex: 2345678" value={formData.raParticipante} onChange={handleInputChange} />
                        </div>
                    </div>
                </div>
            )}

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

            <button type="submit" className="btn-submit" disabled={isSubmitting || (emissaoTipo === 'lote' && !arquivoCsv)}>
                {isSubmitting ? <Loader2 className="spin" size={20} /> : (emissaoTipo === 'lote' ? <FileSpreadsheet size={20} /> : <User size={20} />)}
                {isSubmitting ? 'Processando...' : 'Disparar Certificado' + (emissaoTipo === 'lote' ? 's' : '')}
            </button>
        </form>
    );
}
