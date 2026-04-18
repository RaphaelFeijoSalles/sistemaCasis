import { ChevronLeft, MailCheck, MailWarning, AlertTriangle } from 'lucide-react';

export default function RelatorioEmissao({ resultados, onReset }) {
    return (
        <div className="results-area">
            <div className="results-summary">
                <div className="stat">
                    <span className="label">Sucessos</span>
                    <span className="value success">{resultados.filter(r => r.status === 'SUCESSO').length}</span>
                </div>
                <div className="stat">
                    <span className="label">Existente</span>
                    <span className="value warning">{resultados.filter(r => r.status === 'EXISTENTE').length}</span>
                </div>
                <div className="stat">
                    <span className="label">Falhas</span>
                    <span className="value error">{resultados.filter(r => r.status === 'ERRO').length}</span>
                </div>
            </div>

            <div className="table-wrapper">
                <table className="results-table">
                    <thead><tr><th>Participante</th><th>Status</th></tr></thead>
                    <tbody>
                    {resultados.map((res, index) => {
                        let rowClass = 'row-error';
                        if (res.status === 'SUCESSO') rowClass = 'row-success';
                        if (res.status === 'EXISTENTE') rowClass = 'row-warning';

                        return (
                            <tr key={index} className={rowClass}>
                                <td>
                                    <div className="name">{res.nome}</div>
                                    <div className="email">{res.email}</div>
                                </td>
                                <td className="status-cell">
                                    {res.status === 'SUCESSO' && <MailCheck size={18} className="icon-success" />}

                                    {/* Ícone atualizado e texto corrigido */}
                                    {res.status === 'EXISTENTE' && (
                                        <div className="status-warning" title="Já existe no Drive">
                                            <AlertTriangle size={18} />
                                            <span>Emitido</span>
                                        </div>
                                    )}

                                    {res.status === 'ERRO' && (
                                        <div className="error-hint" title={res.mensagemErro}>
                                            <MailWarning size={18} className="icon-error" />
                                            <span>Erro</span>
                                        </div>
                                    )}
                                </td>
                            </tr>
                        );
                    })}
                    </tbody>
                </table>
            </div>

            <button onClick={onReset} className="btn-secondary">
                <ChevronLeft size={18} /> Novo Lote
            </button>
        </div>
    );
}