let agendamentoForm;
let agendamentoIdInput;
let agendamentoProdutorSelect;
let agendamentoServicoSelect;
let agendamentoDataInput;
let agendamentoHorasHInput;
let agendamentoHorasMInput
let agendamentoValorInput;
let agendamentoBtnLimpar;

let handlers = {};

function _inicializarDOM() {
    agendamentoForm = document.getElementById('agendamento-form');
    agendamentoIdInput = document.getElementById('agendamento-id');
    agendamentoProdutorSelect = document.getElementById('agendamento-produtor');
    agendamentoServicoSelect = document.getElementById('agendamento-servico');
    agendamentoDataInput = document.getElementById('agendamento-data');
    agendamentoHorasHInput = document.getElementById('agendamento-horas-h');
    agendamentoHorasMInput = document.getElementById('agendamento-horas-m');
    agendamentoValorInput = document.getElementById('agendamento-valor');
    agendamentoBtnLimpar = document.getElementById('agendamento-btn-limpar');
    console.log("AgendamentoUI: Elementos DOM 'cacheados'.");
}

function _vincularEventos() {
    if (agendamentoForm) {
        agendamentoForm.addEventListener('submit', handlers.onSaveExecucao);
    } else {
        console.error("AgendamentoUI: Elemento 'agendamento-form' não encontrado.");
    }

    if (agendamentoBtnLimpar) {
        agendamentoBtnLimpar.addEventListener('click', handlers.onClearAgendamento);
    } else {
         console.error("AgendamentoUI: Elemento 'agendamento-btn-limpar' não encontrado.");
    }

    if (agendamentoServicoSelect) {
        agendamentoServicoSelect.addEventListener('change', _atualizarValorTotal);
    }
    
    if (agendamentoHorasHInput) {
        agendamentoHorasHInput.addEventListener('input', _atualizarValorTotal);
    }
    if (agendamentoHorasMInput) {
        agendamentoHorasMInput.addEventListener('input', _atualizarValorTotal);
    }

    console.log("AgendamentoUI: Eventos vinculados (se encontrados).");
}

function _atualizarValorTotal() {
    if (!agendamentoServicoSelect || !agendamentoHorasHInput || !agendamentoHorasMInput || !agendamentoValorInput) return;

    const selectedOption = agendamentoServicoSelect.options[agendamentoServicoSelect.selectedIndex];
    
    const valorUnitario = parseFloat(selectedOption.dataset.valor || 0.0);
    
    const horas = parseInt(agendamentoHorasHInput.value, 10) || 0;
    const minutos = parseInt(agendamentoHorasMInput.value, 10) || 0;

    const horasDecimais = horas + (minutos / 60);
    
    const valorTotal = valorUnitario * horasDecimais;
    
    agendamentoValorInput.value = parseFloat(valorTotal.toFixed(2));
}

/**
 * Inicializa o módulo UI de Agendamento.
 * @param {object} externalHandlers - Handlers vindos do renderer.js
 */
export function inicializar(externalHandlers) {
    handlers = externalHandlers;
    _inicializarDOM();
    _vincularEventos();
}

export function popularDropdownProdutores(produtores) {
    if (!agendamentoProdutorSelect) return;
    agendamentoProdutorSelect.innerHTML = '<option value="">Selecione um Produtor...</option>';
    if (produtores && produtores.length > 0) {
        produtores.forEach(produtor => {
            const option = document.createElement('option');
            option.value = produtor.id;
            option.textContent = produtor.nome;
            agendamentoProdutorSelect.appendChild(option);
        });
    } else {
         agendamentoProdutorSelect.innerHTML = '<option value="">Nenhum produtor cadastrado</option>';
    }
}

export function popularDropdownServicos(servicos) {
    if (!agendamentoServicoSelect) return;
    agendamentoServicoSelect.innerHTML = '<option value="">Selecione um Serviço...</option>';
    if (servicos && servicos.length > 0) {
        servicos.forEach(servico => {
            const option = document.createElement('option');
            option.value = servico.id;
            option.textContent = `${servico.nome} (R$ ${servico.valor_unitario.toFixed(2)})`;
            
            option.dataset.valor = servico.valor_unitario; 
            
            agendamentoServicoSelect.appendChild(option);
        });
    } else {
        agendamentoServicoSelect.innerHTML = '<option value="">Nenhum serviço cadastrado</option>';
    }
}

export function limparFormularioAgendamento() {
    if (!agendamentoForm) return;
    agendamentoIdInput.value = '';
    agendamentoProdutorSelect.value = '';
    agendamentoServicoSelect.value = '';
    agendamentoDataInput.value = '';
    agendamentoHorasHInput.value = '0';
    agendamentoHorasMInput.value = '0';
    agendamentoValorInput.value = '0.00';
    agendamentoProdutorSelect.focus();
}

export function preencherFormularioAgendamento(execucao) {
    if (!agendamentoForm) return;
    agendamentoIdInput.value = execucao.id;
    agendamentoProdutorSelect.value = execucao.produtor_id;
    agendamentoServicoSelect.value = execucao.servico_id;
    agendamentoDataInput.value = execucao.data_execucao;
    const horasDecimal = parseFloat(execucao.horas_prestadas) || 0.0;

    const horasInteiras = Math.floor(horasDecimal); 
    const minutosFracao = (horasDecimal - horasInteiras) * 60; 

    agendamentoHorasHInput.value = horasInteiras;
    agendamentoHorasMInput.value = Math.round(minutosFracao); 
    agendamentoValorInput.value = execucao.valor_total;
    agendamentoProdutorSelect.focus();
}

export function getIdAgendamento() {
    return agendamentoIdInput ? agendamentoIdInput.value || null : null;
}

export function coletarDadosAgendamento() {
    if (!agendamentoForm) return null;
    const produtorId = parseInt(agendamentoProdutorSelect.value, 10);
    const servicoId = parseInt(agendamentoServicoSelect.value, 10);
    const dataExecucao = agendamentoDataInput.value;
    const horas = parseInt(agendamentoHorasHInput.value, 10) || 0;
    const minutos = parseInt(agendamentoHorasMInput.value, 10) || 0;

    const horasPrestadas = horas + (minutos / 60);
    const valorTotal = parseFloat(agendamentoValorInput.value) || 0.00;

    if (isNaN(produtorId) || isNaN(servicoId) || !dataExecucao) {
        alert("Por favor, selecione Produtor, Serviço e Data.");
        return null;
    }

    return {
        produtor_id: produtorId,
        servico_id: servicoId,
        data_execucao: dataExecucao,
        horas_prestadas: horasPrestadas,
        valor_total: valorTotal
    };
}