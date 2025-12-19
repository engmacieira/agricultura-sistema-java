let listaHistorico;
let historicoPaginationInfo;
let historicoBtnAnterior;
let historicoBtnProximo;
let historicoSearchInput;
let historicoBtnSearch;
let historicoBtnClear;

let handlers = {};

function _formatarData(dataISO) {
    if (!dataISO) return 'N/A';
    try {
        const [ano, mes, dia] = dataISO.split('-');
        return `${dia}/${mes}/${ano}`;
    } catch (e) {
        console.error("Erro ao formatar data:", dataISO, e);
        return dataISO; 
    }
}

function _inicializarDOM() {
    listaHistorico = document.getElementById('lista-historico');
    historicoPaginationInfo = document.getElementById('historico-pagination-info');
    historicoBtnAnterior = document.getElementById('historico-btn-anterior');
    historicoBtnProximo = document.getElementById('historico-btn-proximo');
    historicoSearchInput = document.getElementById('historico-search-input');
    historicoBtnSearch = document.getElementById('historico-btn-search');
    historicoBtnClear = document.getElementById('historico-btn-clear');
    console.log("HistoricoUI: Elemento DOM 'cacheado'.");
}

function _vincularEventos() {
    if (historicoBtnAnterior) {
        historicoBtnAnterior.addEventListener('click', handlers.onHistoricoPaginaAnterior);
    }
    if (historicoBtnProximo) {
        historicoBtnProximo.addEventListener('click', handlers.onHistoricoPaginaProxima);
    }
    if (historicoBtnSearch) {
    historicoBtnSearch.addEventListener('click', handlers.onHistoricoSearch);
    }
    if (historicoBtnClear) {
    historicoBtnClear.addEventListener('click', handlers.onHistoricoClearSearch);
    }
    if (historicoSearchInput) {
    historicoSearchInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault(); 
            handlers.onHistoricoSearch();
        }
    });
}
    console.log("HistoricoUI: Eventos de paginação vinculados.");
}

/**
 * Inicializa o módulo UI de Histórico.
 * @param {object} externalHandlers - Handlers vindos do renderer.js (onEditExecucao, onDeleteExecucao)
 */
export function inicializar(externalHandlers) {
    handlers = externalHandlers;
    _inicializarDOM();
    _vincularEventos();
}

/**
 * Desenha a lista de execuções (histórico) na tela.
 * @param {Array} execucoes - Lista de objetos de execução vindos da API.
 * @param {Object} [produtoresMap] - Mapa {id: nome}.
 * @param {Object} [servicosMap] - Mapa {id: nome}.
 * // Os handlers agora vêm do objeto 'handlers' do módulo
 */
export function desenharListaExecucoes(paginatedData) {
     if (!listaHistorico || !historicoPaginationInfo || !historicoBtnAnterior || !historicoBtnProximo) {
        console.error("HistoricoUI: Elemento 'lista-historico' ou paginação não encontrado para desenhar.");
        return; 
    }

    const { execucoes, total_pages, current_page } = paginatedData;
    listaHistorico.innerHTML = '';

    if (!execucoes || execucoes.length === 0) {
        listaHistorico.innerHTML = '<li>Nenhum agendamento encontrado.</li>';
    } else {
        execucoes.forEach(exec => {
            const item = document.createElement('li');
            
            let nomeDisplay = exec.produtor_nome;
            if (exec.produtor_apelido) {
                nomeDisplay += ` (${exec.produtor_apelido})`;
            }

            let statusPagamentoHTML = '';
            if (exec.saldo_devedor <= 0) {
                statusPagamentoHTML = `<span style="color: green; font-weight: bold;">(Pago)</span>`;
            } else if (exec.saldo_devedor > 0) {
                statusPagamentoHTML = `<span style="color: red; font-weight: bold;">(R$ ${exec.saldo_devedor.toFixed(2)} pendente)</span>`;
            }

            const infoContainer = document.createElement('div');
            infoContainer.style.flexGrow = '1';
            infoContainer.innerHTML = `
                <div>
                    <span><strong>Data:</strong> ${_formatarData(exec.data_execucao)}</span>
                    <span><strong>Produtor:</strong> ${nomeDisplay}</span>
                    <span><strong>Serviço:</strong> ${exec.servico_nome}</span>
                </div>
                <div style="margin-top: 5px; font-size: 0.9em;">
                    <span>Valor Total: R$ ${exec.valor_total.toFixed(2)}</span>
                    | <span>Total Pago: R$ ${exec.total_pago.toFixed(2)}</span>
                    ${statusPagamentoHTML}
                    <span style="font-size: 0.9em; color: grey; margin-left: 10px;">(ID: ${exec.id})</span>
                </div>
            `;

            const buttonsContainer = document.createElement('div');
            const btnEditar = document.createElement('button');
            btnEditar.textContent = 'Editar';
            btnEditar.onclick = () => {
                if (handlers.onEditExecucao) handlers.onEditExecucao(exec);
            };

            const btnExcluir = document.createElement('button');
            btnExcluir.textContent = 'Excluir';
            btnExcluir.onclick = () => {
                 if (handlers.onDeleteExecucao) handlers.onDeleteExecucao(exec.id);
            };

            buttonsContainer.appendChild(btnEditar);
            buttonsContainer.appendChild(btnExcluir);

            item.appendChild(infoContainer);
            item.appendChild(buttonsContainer);
            listaHistorico.appendChild(item);
        });
    }

    historicoPaginationInfo.textContent = `Página ${current_page} de ${total_pages || 1}`;
    historicoBtnAnterior.disabled = (current_page <= 1);
    historicoBtnProximo.disabled = (current_page >= total_pages);
}

export function getHistoricoSearchTerm() {
    return historicoSearchInput ? historicoSearchInput.value : null;
}

export function clearHistoricoSearchTerm() {
    if (historicoSearchInput) {
        historicoSearchInput.value = '';
    }
}