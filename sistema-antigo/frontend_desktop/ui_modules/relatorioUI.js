let relatorioSelectProdutor;
let listaRelatorioDividas;

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
    relatorioSelectProdutor = document.getElementById('relatorio-select-produtor');
    listaRelatorioDividas = document.getElementById('lista-relatorio-dividas');
    console.log("RelatorioUI: Elementos DOM 'cacheados'.");
}

function _vincularEventos() {
    if (relatorioSelectProdutor) {
        relatorioSelectProdutor.addEventListener('change', handlers.onRelatorioProdutorSelecionado);
    } else {
        console.error("RelatorioUI: Elemento 'relatorio-select-produtor' não encontrado.");
    }
    console.log("RelatorioUI: Eventos vinculados (se encontrados).");
}

/**
 * Inicializa o módulo UI de Relatórios.
 * @param {object} externalHandlers - Handlers vindos do renderer.js
 */
export function inicializar(externalHandlers) {
    handlers = externalHandlers;
    _inicializarDOM();
    _vincularEventos();
}

export function popularDropdownRelatorioProdutores(produtores) {
    if (!relatorioSelectProdutor) {
         console.error("RelatorioUI: Elemento 'relatorio-select-produtor' não encontrado para popular.");
         return;
    }
    relatorioSelectProdutor.innerHTML = '<option value="">Selecione um Produtor...</option>';
    if (produtores && produtores.length > 0) {
        produtores.sort((a, b) => a.nome.localeCompare(b.nome));
        produtores.forEach(produtor => {
            const option = document.createElement('option');
            option.value = produtor.id;
            option.textContent = produtor.nome;
            relatorioSelectProdutor.appendChild(option);
        });
    } else {
         relatorioSelectProdutor.innerHTML = '<option value="">Nenhum produtor cadastrado</option>';
    }
}

export function desenharRelatorioDividas(dividas) {
     if (!listaRelatorioDividas) {
        console.error("RelatorioUI: Elemento 'lista-relatorio-dividas' não encontrado para desenhar.");
        return;
     }
    listaRelatorioDividas.innerHTML = '';

    if (!dividas) {
        listaRelatorioDividas.innerHTML = '<li>Erro ao carregar relatório. Verifique o console.</li>';
        return;
    }

    if (dividas.length === 0) {
        listaRelatorioDividas.innerHTML = '<li>Nenhuma dívida encontrada para este produtor.</li>';
        return;
    }

    dividas.sort((a, b) => new Date(b.data_execucao) - new Date(a.data_execucao));

    dividas.forEach(divida => {
        const item = document.createElement('li');

        const valorTotal = divida.valor_total.toFixed(2);
        const totalPago = divida.total_pago.toFixed(2);
        const saldoDevedor = divida.saldo_devedor.toFixed(2);

        item.innerHTML = `
            <span><strong>Data:</strong> ${_formatarData(divida.data_execucao)}</span>
            <span><strong>Serviço:</strong> ${divida.servico_nome}</span>
            <span><strong>Valor Total:</strong> R$ ${valorTotal}</span>
            <span><strong>Valor Pago:</strong> R$ ${totalPago}</span>
            <span style="color: red;"><strong>Saldo Devedor: R$ ${saldoDevedor}</strong></span>
            <span style="font-size: 0.8em; color: grey;">(Exec. ID: ${divida.execucao_id})</span>
        `;
        listaRelatorioDividas.appendChild(item);
    });
}