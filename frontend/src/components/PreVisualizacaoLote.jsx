import { AlertCircle, AlertTriangle, CheckCircle2, ChevronLeft, Loader2, Send, XCircle } from 'lucide-react';

const SITUACOES = {
    VALIDA: { rowClass: 'row-success', icone: CheckCircle2, rotulo: 'Válida', classe: 'previa-situacao valida' },
    AVISO: { rowClass: 'row-warning', icone: AlertTriangle, rotulo: 'Aviso', classe: 'previa-situacao aviso' },
    ERRO: { rowClass: 'row-error', icone: XCircle, rotulo: 'Erro', classe: 'previa-situacao erro' }
};

export default function PreVisualizacaoLote({ previa, isSubmitting, onConfirmar, onVoltar }) {
    const emitiveis = previa.validas + previa.comAviso;
    const rotuloConfirmar = `Confirmar emissão de ${emitiveis} certificado${emitiveis === 1 ? '' : 's'}`;

    return (
        <div className="results-area">
            <div className="results-summary">
                <div className="stat">
                    <span className="label">Linhas</span>
                    <span className="value">{previa.totalLinhas}</span>
                </div>
                <div className="stat">
                    <span className="label">Válidas</span>
                    <span className="value success">{previa.validas}</span>
                </div>
                <div className="stat">
                    <span className="label">Com aviso</span>
                    <span className="value warning">{previa.comAviso}</span>
                </div>
                <div className="stat">
                    <span className="label">Com erro</span>
                    <span className="value error">{previa.comErro}</span>
                </div>
            </div>

            <div className="table-wrapper">
                <table className="results-table">
                    <thead><tr><th>Linha</th><th>Participante</th><th>Horas</th><th>Situação</th></tr></thead>
                    <tbody>
                    {previa.linhas.map((linha) => {
                        const situacao = SITUACOES[linha.situacao] ?? SITUACOES.ERRO;
                        const Icone = situacao.icone;

                        return (
                            <tr key={linha.linha} className={situacao.rowClass}>
                                <td className="previa-numero">{linha.linha}</td>
                                <td>
                                    <div className="name">{linha.nome || '(sem nome)'}</div>
                                    <div className="email">{linha.email || '(sem e-mail)'}</div>
                                    {linha.ra && <div className="email">RA: {linha.ra}</div>}
                                    {linha.mensagens.length > 0 && (
                                        <ul className="previa-mensagens">
                                            {linha.mensagens.map((mensagem) => <li key={mensagem}>{mensagem}</li>)}
                                        </ul>
                                    )}
                                </td>
                                <td className="previa-horas">
                                    {linha.horasTexto ? (
                                        <>
                                            <span>{linha.horasTexto}</span>
                                            {linha.horasDoFormulario && <span className="previa-horas-origem">(formulário)</span>}
                                        </>
                                    ) : '—'}
                                </td>
                                <td className="status-cell">
                                    <div className={situacao.classe}>
                                        <Icone size={18} />
                                        <span>{situacao.rotulo}</span>
                                    </div>
                                </td>
                            </tr>
                        );
                    })}
                    </tbody>
                </table>
            </div>

            {previa.comErro > 0 && (
                <div className="pending-warning">
                    <AlertCircle size={16} />
                    <span>
                        {previa.comErro} linha{previa.comErro === 1 ? '' : 's'} com erro não {previa.comErro === 1 ? 'será emitida' : 'serão emitidas'}.
                        Corrija o CSV e pré-visualize de novo se quiser incluí-las.
                    </span>
                </div>
            )}

            <div className="previa-acoes">
                <button type="button" className="btn-submit" onClick={onConfirmar} disabled={isSubmitting || emitiveis === 0}>
                    {isSubmitting ? <Loader2 className="spin" size={20} /> : <Send size={20} />}
                    {isSubmitting ? 'Emitindo...' : (emitiveis === 0 ? 'Nenhuma linha pode ser emitida' : rotuloConfirmar)}
                </button>

                <button type="button" onClick={onVoltar} className="btn-secondary" disabled={isSubmitting}>
                    <ChevronLeft size={18} /> Voltar e editar
                </button>
            </div>
        </div>
    );
}
