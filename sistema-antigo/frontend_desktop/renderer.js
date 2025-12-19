import * as ui from './ui.js';

const api = window.api;
const log = window.api.log;

let agendamentoDropdownsCarregados = false;
let historicoCarregadoPrimeiraVez = false;
let pagamentosDropdownCarregado = false; 
let relatorioProdutoresCarregado = false; 
let cacheProdutores = []; 
let cacheServicos = [];
let cacheExecucoes = [];
let cachePagamentosAtuais = [];
let produtoresPaginaAtual = 1;
let produtoresTotalPaginas = 1;
let servicosPaginaAtual = 1;
let servicosTotalPaginas = 1;
let historicoPaginaAtual = 1;
let historicoTotalPaginas = 1;
let historicoSearchTerm = null;
let produtorSearchTerm = null;
let cacheProdutoresCompleto = [];
let cacheServicosCompleto = [];
let cacheExecucaoSelecionada = null;

async function carregarProdutores(page = 1, search = produtorSearchTerm) {
    try {
        log.info(`Buscando produtores - Página ${page}`);
        const paginatedData = await api.getProdutores(page, search);
        
        produtoresPaginaAtual = paginatedData.current_page;
        produtoresTotalPaginas = paginatedData.total_pages || 1;
        produtorSearchTerm = search;

        ui.desenharListaProdutores(paginatedData);
        
    } catch (error) {
        log.error("Erro ao carregar produtores:", error);
        await api.dialog.alert("Falha grave ao buscar produtores.");
    }
}
async function handleSaveProdutor(event) {
    cacheProdutoresCompleto = [];
    event.preventDefault(); 
    const id = ui.getIdProdutor();
    const dadosProdutor = ui.coletarDadosProdutor();
    if (!dadosProdutor) return; 

    try {
        let produtorSalvo;
        if (id) {
            produtorSalvo = await api.updateProdutor(id, dadosProdutor);
            if (!produtorSalvo || produtorSalvo.error) throw new Error(produtorSalvo?.error || "API não retornou o produtor atualizado.");
            
            await api.dialog.alert(`Produtor "${produtorSalvo.nome}" atualizado!`);

            setTimeout(async () => {
                await carregarProdutores(produtoresPaginaAtual); 
            }, 0);

        } else {
            produtorSalvo = await api.createProdutor(dadosProdutor);
            if (!produtorSalvo || produtorSalvo.error) throw new Error(produtorSalvo?.error || "API não retornou o novo produtor.");
            
            await api.dialog.alert(`Produtor "${produtorSalvo.nome}" criado com ID: ${produtorSalvo.id}`);
            
            setTimeout(async () => {
                await carregarProdutores(1); 
            }, 0);
        }
        
        ui.limparFormularioProdutor();
        
        relatorioProdutoresCarregado = false;
        agendamentoDropdownsCarregados = false;
        
    } catch (error) {
        log.error("Erro ao salvar produtor:", error);
        await api.dialog.alert(`Falha ao salvar produtor: ${error.message}`);
        await carregarProdutores(produtoresPaginaAtual); 
    }
}
function handleClearProdutor() {
    ui.limparFormularioProdutor();
}
function handleEditProdutor(produtor) {
    ui.preencherFormularioProdutor(produtor);
}
async function handleDeleteProdutor(id) {
    cacheProdutoresCompleto = [];
    if (await api.dialog.confirm(`Tem certeza que deseja excluir o produtor ID ${id}?`)) {
        try {
            await api.deleteProdutor(id);
            await api.dialog.alert(`Produtor ID ${id} excluído com sucesso.`);
            setTimeout(async () => {
                await carregarProdutores(produtoresPaginaAtual);
            }, 0);
            ui.limparFormularioProdutor();
            relatorioProdutoresCarregado = false;
        } catch (error) {
            log.error(`Erro ao excluir produtor ${id}:`, error);
            await api.dialog.alert("Falha ao excluir. (Verifique se o produtor não possui execuções de serviço.)");
        }
    }
}

async function carregarServicos(page = 1) {
     try {
        log.info(`Buscando serviços - Página ${page}`);
        const paginatedData = await api.getServicos(page);

        servicosPaginaAtual = paginatedData.current_page;
        servicosTotalPaginas = paginatedData.total_pages || 1;

        ui.desenharListaServicos(paginatedData);

    } catch (error) {
        log.error("Erro ao carregar serviços:", error);
        await api.dialog.alert("Falha grave ao buscar serviços.");
    }
}
async function handleSaveServico(event) {
    cacheServicosCompleto = [];
    event.preventDefault();
    const id = ui.getIdServico();
    const dadosServico = ui.coletarDadosServico();
    if (!dadosServico) return; 

    try {
        let servicoSalvo;
        if (id) {
            servicoSalvo = await api.updateServico(id, dadosServico);
            if (!servicoSalvo || servicoSalvo.error) throw new Error(servicoSalvo?.error || "API não retornou o serviço atualizado.");
            
            await api.dialog.alert(`Serviço "${servicoSalvo.nome}" atualizado!`);

            setTimeout(async () => {
                await carregarServicos(servicosPaginaAtual);
            }, 0);
            
        } else {
            servicoSalvo = await api.createServico(dadosServico);
            if (!servicoSalvo || servicoSalvo.error) throw new Error(servicoSalvo?.error || "API não retornou o novo serviço.");
            
            await api.dialog.alert(`Serviço "${servicoSalvo.nome}" criado com ID: ${servicoSalvo.id}`);

            setTimeout(async () => {
                await carregarServicos(1);
            }, 0);
        }
        
        ui.limparFormularioServico();
        
        agendamentoDropdownsCarregados = false;
        
    } catch (error) {
        log.error("Erro ao salvar serviço:", error);
        await api.dialog.alert(`Falha ao salvar serviço: ${error.message}`);
        await carregarServicos(servicosPaginaAtual);
    }
}
function handleClearServico() {
    ui.limparFormularioServico();
}
function handleEditServico(servico) {
    ui.preencherFormularioServico(servico);
}
async function handleDeleteServico(id) {
    cacheServicosCompleto = []; 
    if (await api.dialog.confirm(`Tem certeza que deseja excluir o serviço ID ${id}?`)) {
        try {
            await api.deleteServico(id);
            await api.dialog.alert(`Serviço ID ${id} excluído com sucesso.`);
            setTimeout(async () => {
                await carregarServicos(servicosPaginaAtual); 
            }, 0);
            ui.limparFormularioServico();
        } catch (error) {
            log.error(`Erro ao excluir serviço ${id}:`, error);
            await api.dialog.alert("Falha ao excluir. (Verifique se o serviço não está sendo usado em uma execução.)");
        }
    }
}

async function handleSaveExecucao(event) {
    event.preventDefault();
    const id = ui.getIdAgendamento(); 
    const dadosExecucao = ui.coletarDadosAgendamento();
    if (!dadosExecucao) return;
    try {
        let resultado;
        if (id) {
            log.info(`Atualizando execução ID: ${id}`);
            resultado = await api.updateExecucao(id, dadosExecucao);
            if (resultado && resultado.error) throw new Error(resultado.error);
            if (!resultado) throw new Error("API não retornou a execução atualizada.");
            await api.dialog.alert(`Agendamento ID ${resultado.id} atualizado com sucesso!`);
        } else {
            log.info("Criando nova execução...");
            resultado = await api.createExecucao(dadosExecucao);
            if (resultado && resultado.error) throw new Error(resultado.error);
            if (!resultado) throw new Error("API não retornou a nova execução.");
            await api.dialog.alert(`Agendamento criado com sucesso! ID: ${resultado.id}`);
        }
        ui.limparFormularioAgendamento();
        historicoCarregadoPrimeiraVez = false; 
        pagamentosDropdownCarregado = false; 
    } catch (error) {
        log.error("Erro ao salvar agendamento:", error);
        await api.dialog.alert(`Falha ao salvar agendamento: ${error.message}`);
    }
}
function handleClearAgendamento() {
    ui.limparFormularioAgendamento();
}

async function carregarExecucoes(page = 1, search = historicoSearchTerm) {
    log.info(`Carregando Execuções - Página ${page}`);
    try {
        const paginatedData = await api.getExecucoes(page, 'todos', search);

        historicoPaginaAtual = paginatedData.current_page;
        historicoTotalPaginas = paginatedData.total_pages || 1;
        historicoSearchTerm = search;

        ui.desenharListaExecucoes(paginatedData);
        
        log.info("Execuções carregadas e lista desenhada.");

    } catch (error) {
        log.error("Erro ao carregar execuções:", error);
        await api.dialog.alert("Falha grave ao buscar o histórico de execuções.");
    }
}

async function carregarDadosAgendamento() {
    log.info("Carregando dados para dropdowns de agendamento...");
    try {
        if (cacheProdutoresCompleto.length === 0) {
            log.info("Cache de produtores completo vazio. Buscando todos (9999)...");
            const produtorData = await api.getProdutores(1, null, 9999); 
            
            if (produtorData.error) {
                throw new Error(`Falha ao buscar produtores: ${produtorData.error}`);
            }
            cacheProdutoresCompleto = produtorData.produtores;
        }
        
        if (cacheServicosCompleto.length === 0) {
            log.info("Cache de serviços completo vazio. Buscando todos (9999)...");
            const servicoData = await api.getServicos(1, 9999);
            
            if (servicoData.error) {
                 throw new Error(`Falha ao buscar serviços: ${servicoData.error}`);
            }
            cacheServicosCompleto = servicoData.servicos;
        }
        
        ui.popularDropdownProdutores(cacheProdutoresCompleto);
        ui.popularDropdownServicos(cacheServicosCompleto);
        
        log.info("Dropdowns de agendamento populados.");
        
    } catch (error) {
         log.error("Erro ao carregar dados para agendamento:", error);
         await api.dialog.alert(`Falha grave ao buscar dados para agendamento:\n${error.message}`);
         ui.popularDropdownProdutores([]);
         ui.popularDropdownServicos([]);
    }
}
async function handleEditExecucao(execucao) { 
    log.info("Editando execução:", execucao);
    try { 
        await carregarDadosAgendamento(); 

        ui.preencherFormularioAgendamento(execucao);
        const agendamentoTabButton = document.querySelector('button[data-tab="painel-agendamento"]');
        if (agendamentoTabButton) {
            ui.trocarAba(agendamentoTabButton);
        } else {
            log.error("Não foi possível encontrar o botão da aba de agendamento.");
        }
    } catch (error) { 

        log.error("Erro ao preparar edição de execução:", error);
        await api.dialog.alert("Erro ao carregar dados para edição.");
    }
}
async function handleDeleteExecucao(id) {
    if (await api.dialog.confirm(`Tem certeza que deseja excluir o agendamento ID ${id}? (Pagamentos associados também serão excluídos!)`)) {
        try {
            log.info(`Excluindo execução ID: ${id}`);
            const resultado = await api.deleteExecucao(id);
            if (resultado && resultado.error) throw new Error(resultado.error);
            await api.dialog.alert(`Agendamento ID ${id} excluído com sucesso.`);
            pagamentosDropdownCarregado = false; 
            historicoCarregadoPrimeiraVez = false;
            setTimeout(async () => { 
                await carregarExecucoes(historicoPaginaAtual); 
            }, 0);
        } catch (error) {
            log.error(`Erro ao excluir execução ${id}:`, error);
            await api.dialog.alert(`Falha ao excluir agendamento: ${error.message}`);
        }
    }
}
async function carregarDadosPagamentos() {
    if (pagamentosDropdownCarregado) return;
    log.info("Carregando dados para dropdown de pagamentos (Pendentes e Pagas)...");

    try {
        const pendentesData = await api.getExecucoes(1, 'pendentes');

        const pagasData = await api.getExecucoes(1, 'pagas');

        ui.popularDropdownExecucoesPendentes(pendentesData.execucoes);

        ui.popularListaAgendamentosPagos(pagasData.execucoes);

        pagamentosDropdownCarregado = true;
        log.info("Dropdowns de pagamentos (Pendentes e Pagas) populados.");

    } catch (error) {
        log.error("Erro ao carregar dados para pagamentos:", error);
        await api.dialog.alert("Falha ao carregar dados para pagamentos.");
        ui.popularDropdownExecucoesPendentes([]);
        ui.popularListaAgendamentosPagos([]);
    }
}
async function _carregarDetalhesExecucao(execucao) {
    cacheExecucaoSelecionada = execucao;
    if (!execucao || !execucao.id) {
        ui.exibirDetalhesExecucaoPagamentos(null, null, null, []);
        ui.desenharListaPagamentos([], handleEditPagamento, handleDeletePagamento);
        return;
    }

    try {
        const execucaoId = execucao.id;
        log.info(`Buscando pagamentos para execução ID: ${execucaoId}`);
        
        const nomeProdutor = execucao.produtor_nome + (execucao.produtor_apelido ? ` (${execucao.produtor_apelido})` : '');
        const nomeServico = execucao.servico_nome;
        
        const pagamentos = await api.getPagamentosPorExecucao(execucaoId);
        cachePagamentosAtuais = pagamentos;
        
        const totalPago = pagamentos.reduce((soma, p) => soma + p.valor_pago, 0);
        const saldoDevedor = execucao.valor_total - totalPago;

        ui.exibirDetalhesExecucaoPagamentos(execucao, nomeProdutor, nomeServico, cachePagamentosAtuais);
        ui.desenharListaPagamentos(cachePagamentosAtuais, handleEditPagamento, handleDeletePagamento);

        if (saldoDevedor <= 0 && pagamentos.length === 1) {
            log.info("Execução paga com um único pagamento. Pré-preenchendo formulário.");
            ui.preencherFormularioPagamento(pagamentos[0]);
        } else {

            ui.limparFormularioPagamento();
        }

    } catch (error) {
        log.error(`Erro ao buscar pagamentos para execução ${execucao.id}:`, error);
        await api.dialog.alert("Falha ao buscar pagamentos para a execução selecionada.");
        ui.exibirDetalhesExecucaoPagamentos(null, null, null, []);
        ui.desenharListaPagamentos([], handleEditPagamento, handleDeletePagamento);
    }
}
async function handleExecucaoSelecionada(event) {
    const selectElement = event.target;
    const selectedOption = selectElement.options[selectElement.selectedIndex];
    const execucaoId = selectedOption.value;

    if (!execucaoId) {
        await _carregarDetalhesExecucao(null);
        return;
    }
    
    const execucao = JSON.parse(selectedOption.dataset.execucao);
    await _carregarDetalhesExecucao(execucao); 
}
async function handleAgendamentoPagoSelecionado(execucao) {
    log.info(`Item pago clicado. Carregando detalhes para Exec. ID: ${execucao.id}`);
    
    const selectElement = document.getElementById('pagamentos-select-execucao');
    if (selectElement) selectElement.value = '';

    await _carregarDetalhesExecucao(execucao); 
}
async function handleSavePagamento(event) {
    event.preventDefault();
    const idPagamento = ui.getIdPagamento();
    const dadosPagamento = ui.coletarDadosPagamento();
    if (!dadosPagamento) return;

    const execucaoIdSelecionada = document.getElementById('pagamentos-select-execucao').value;
    if (!execucaoIdSelecionada) {
        await api.dialog.alert("Erro interno: Nenhuma execução selecionada para associar o pagamento.");
        return;
    }

    try {
        let pagamentoSalvo;
        if (idPagamento) {
            log.info(`Atualizando pagamento ID: ${idPagamento}`);
            pagamentoSalvo = await api.updatePagamento(idPagamento, dadosPagamento);
            if (pagamentoSalvo && pagamentoSalvo.error) throw new Error(pagamentoSalvo.error);
            if (!pagamentoSalvo) throw new Error("API não retornou o pagamento atualizado.");
            
            const index = cachePagamentosAtuais.findIndex(p => p.id == idPagamento);
            if (index !== -1) {
                cachePagamentosAtuais[index] = pagamentoSalvo;
            }
            await api.dialog.alert(`Pagamento ID ${pagamentoSalvo.id} atualizado!`);
            pagamentosDropdownCarregado = false;

        } else {
            log.info(`Criando pagamento para execução ID: ${execucaoIdSelecionada}`);
            pagamentoSalvo = await api.createPagamento(execucaoIdSelecionada, dadosPagamento);
            if (pagamentoSalvo && pagamentoSalvo.error) throw new Error(pagamentoSalvo.error);
            if (!pagamentoSalvo) throw new Error("API não retornou o novo pagamento.");

            cachePagamentosAtuais.push(pagamentoSalvo);
            await api.dialog.alert(`Pagamento criado com ID: ${pagamentoSalvo.id}`);
            pagamentosDropdownCarregado = false;
        }

        const selectElement = document.getElementById('pagamentos-select-execucao');
        const selectedOption = selectElement.options[selectElement.selectedIndex];
        const execucao = JSON.parse(selectedOption.dataset.execucao);
        const nomeProdutor = selectedOption.dataset.produtorNome;
        const nomeServico = selectedOption.dataset.servicoNome;

        ui.exibirDetalhesExecucaoPagamentos(execucao, nomeProdutor, nomeServico, cachePagamentosAtuais);
        ui.desenharListaPagamentos(cachePagamentosAtuais, handleEditPagamento, handleDeletePagamento);

        ui.limparFormularioPagamento();

    } catch (error) {
        log.error("Erro ao salvar pagamento:", error);
        await api.dialog.alert(`Falha ao salvar pagamento: ${error.message}`);
        
        await refreshPagamentosVisiveis();
    }
}
function handleClearPagamento() {
    ui.limparFormularioPagamento();
}
function handleEditPagamento(pagamento) {
    log.info("Editando pagamento:", pagamento);
    ui.preencherFormularioPagamento(pagamento);
}
async function handleDeletePagamento(id) {
    if (await api.dialog.confirm(`Tem certeza que deseja excluir o pagamento ID ${id}?`)) {
        try {
            log.info(`Excluindo pagamento ID: ${id}`);
            const resultado = await api.deletePagamento(id);
            if (resultado && resultado.error) throw new Error(resultado.error);
            await api.dialog.alert(`Pagamento ID ${id} excluído com sucesso.`);
            pagamentosDropdownCarregado = false;

            cachePagamentosAtuais = cachePagamentosAtuais.filter(p => p.id != id);

            if (!cacheExecucaoSelecionada) {
                log.error("handleDeletePagamento: Nenhuma execução selecionada no cache.");
                await api.dialog.alert("Erro: Nenhuma execução selecionada para atualizar a lista.");
                return;
            }
            const execucao = cacheExecucaoSelecionada;
            const nomeProdutor = execucao.produtor_nome + (execucao.produtor_apelido ? ` (${execucao.produtor_apelido})` : '');
            const nomeServico = execucao.servico_nome;

            ui.exibirDetalhesExecucaoPagamentos(execucao, nomeProdutor, nomeServico, cachePagamentosAtuais);
            ui.desenharListaPagamentos(cachePagamentosAtuais, handleEditPagamento, handleDeletePagamento);

            ui.limparFormularioPagamento();

        } catch (error) {
            log.error(`Erro ao excluir pagamento ${id}:`, error);
            await api.dialog.alert(`Falha ao excluir pagamento: ${error.message}`);
            await refreshPagamentosVisiveis();
        }
    }
}
function handleCancelEditPagamento() {
    ui.limparFormularioPagamento();
}
async function refreshPagamentosVisiveis() {
    const selectElement = document.getElementById('pagamentos-select-execucao');
    const selectedOption = selectElement.options[selectElement.selectedIndex];
    const execucaoId = selectedOption.value;
     if (!execucaoId) return; 
     try {
        const execucao = JSON.parse(selectedOption.dataset.execucao);
        const nomeProdutor = selectedOption.dataset.produtorNome;
        const nomeServico = selectedOption.dataset.servicoNome;
        const pagamentos = await api.getPagamentosPorExecucao(execucaoId);
        ui.exibirDetalhesExecucaoPagamentos(execucao, nomeProdutor, nomeServico, pagamentos);
        ui.desenharListaPagamentos(pagamentos, handleEditPagamento, handleDeletePagamento);
     } catch(error) {
         log.error("Erro ao refrescar pagamentos:", error);
     }
}

async function carregarDadosRelatorios() {
    if (relatorioProdutoresCarregado) return;
    log.info("Carregando produtores para dropdown de relatórios...");
    
    try {
        if (cacheProdutoresCompleto.length === 0) {
            log.info("Cache de produtores completo vazio. Buscando todos (9999)...");
            const produtorData = await api.getProdutores(1, null, 9999); 
            cacheProdutoresCompleto = produtorData.produtores;
            }        

    } catch (error) {
         log.error("Erro ao carregar dados para relatórios:", error);
         await api.dialog.alert("Falha grave ao buscar produtores para o relatório.");
    }

    ui.popularDropdownRelatorioProdutores(cacheProdutoresCompleto);
    relatorioProdutoresCarregado = true; 
}
async function handleRelatorioProdutorSelecionado(event) {
    const selectElement = event.target;
    const produtorId = selectElement.value;

    if (!produtorId) {
        ui.desenharRelatorioDividas([]); 
        return;
    }
    try {
        log.info(`Buscando relatório de dívidas para produtor ID: ${produtorId}`);
        const dividas = await api.getRelatorioDividas(produtorId);
        if (dividas && dividas.error) throw new Error(dividas.error);
        ui.desenharRelatorioDividas(dividas);
    } catch (error) {
        log.error(`Erro ao buscar relatório de dívidas para produtor ${produtorId}:`, error);
        await api.dialog.alert("Falha ao buscar relatório de dívidas.");
        ui.desenharRelatorioDividas(null); 
    }
}

async function handleProdutorPaginaProxima() {
    if (produtoresPaginaAtual < produtoresTotalPaginas) {
        await carregarProdutores(produtoresPaginaAtual + 1, produtorSearchTerm);
    }
}

async function handleProdutorPaginaAnterior() {
    if (produtoresPaginaAtual > 1) {
        await carregarProdutores(produtoresPaginaAtual - 1, produtorSearchTerm);
    }
}

async function handleServicoPaginaProxima() {
    if (servicosPaginaAtual < servicosTotalPaginas) {
        await carregarServicos(servicosPaginaAtual + 1);
    }
}

async function handleServicoPaginaAnterior() {
    if (servicosPaginaAtual > 1) {
        await carregarServicos(servicosPaginaAtual - 1);
    }
}

async function handleHistoricoPaginaProxima() {
    if (historicoPaginaAtual < historicoTotalPaginas) {
        await carregarExecucoes(historicoPaginaAtual + 1, historicoSearchTerm);
    }
}

async function handleHistoricoPaginaAnterior() {
    if (historicoPaginaAtual > 1) {
        await carregarExecucoes(historicoPaginaAtual - 1, historicoSearchTerm);
    }
}

async function handleProdutorSearch() {
    const searchTerm = ui.getProdutorSearchTerm();
    log.info(`Buscando produtores com termo: "${searchTerm}"`);
    await carregarProdutores(1, searchTerm);
}

async function handleProdutorClearSearch() {
    log.info("Limpando busca de produtores.");
    ui.clearProdutorSearchTerm();
    await carregarProdutores(1, null);
}

async function handleHistoricoSearch() {
    const searchTerm = ui.getHistoricoSearchTerm();
    log.info(`Buscando histórico com termo: "${searchTerm}"`);
    await carregarExecucoes(1, searchTerm);
}

async function handleHistoricoClearSearch() {
    log.info("Limpando busca do histórico.");
    ui.clearHistoricoSearchTerm();
    await carregarExecucoes(1, null);
}

async function handleListBackups() {
    log.info("Buscando lista de backups...");
    try {
        const backupFiles = await api.adminListBackups();
        if (backupFiles.erro) throw new Error(backupFiles.erro);
        ui.desenharListaBackups(backupFiles);   
    } catch (error) {
        log.error("Falha ao desenhar lista de backups:", error);
        ui.desenharListaBackups([]); 
    }
}

async function handleManualBackup() {
    log.warn("Solicitando backup manual...");
    try {
        const resultado = await api.adminRunBackup();
        if (resultado.sucesso) {
            await api.dialog.alert(resultado.mensagem);
        } else {
            throw new Error(resultado.erro);
        }
    } catch (error) {
         await api.dialog.alert(`Falha ao criar backup manual:\n${error.message}`);
    }
    await handleListBackups(); 
}

async function handleRestoreBackup(filename) {
    log.warn(`Solicitando RESTAURAÇÃO do backup: ${filename}`);
    if (await api.dialog.confirm(`ATENÇÃO!\n\nTem certeza que deseja restaurar o backup "${filename}"?\n\nTODOS os dados atuais serão PERDIDOS e substituídos pelo backup.`)) {
        await api.dialog.alert("Restauração solicitada! A aplicação será reiniciada.");
    }
}

async function handleDeleteBackup(filename) {
    log.warn(`Solicitando EXCLUSÃO do backup: ${filename}`);
     if (await api.dialog.confirm(`Tem certeza que deseja EXCLUIR PERMANENTEMENTE o backup "${filename}"?`)) {
        await api.dialog.alert("Backup excluído.");
        await handleListBackups(); 
    }
}

async function handleVerExcluidos(tipo) {
    log.info(`Buscando itens excluídos do tipo: ${tipo}`);
}

async function handleImportar(tipo) {
    const fileInput = document.getElementById(`admin-import-file-${tipo}`);
    if (!fileInput || !fileInput.files || fileInput.files.length === 0) {
        await api.dialog.alert(`Por favor, selecione um arquivo Excel para importar ${tipo}.`);
        return;
    }
    const filePath = fileInput.files[0].path;
    log.warn(`Solicitando importação de ${tipo} do arquivo: ${filePath}`);
    
    if (!await api.dialog.confirm(`Iniciar importação de '${tipo}'?\n\nArquivo: ${filePath}\n\nIsso pode demorar alguns segundos.`)) {
        fileInput.value = null;
        return;
    }

    try {
        const resultado = await api.adminImportar(tipo, filePath);

        if (resultado && resultado.sucesso) {
            await api.dialog.alert(`Importação Concluída!\n\n${resultado.mensagem}`);
            
            if (tipo === 'produtores') {
                cacheProdutoresCompleto = [];
                relatorioProdutoresCarregado = false;
                agendamentoDropdownsCarregados = false;
            }
            if (tipo === 'servicos') {
                cacheServicosCompleto = [];
                agendamentoDropdownsCarregados = false;
            }
        } else {
            const erroMsg = resultado.erros ? resultado.erros.join('\n') : (resultado.erro || "A API retornou uma falha sem detalhes.");
            throw new Error(erroMsg);
        }

    } catch (error) {
        log.error(`Falha grave ao importar ${tipo}:`, error);
        await api.dialog.alert(`Erro na Importação de ${tipo}:\n\n${error.message}`);
    } finally {
        fileInput.value = null; 
    }
}

/**
 * Esta é a função de callback que o ui.js chamará quando uma aba for trocada.
 * @param {string} painelId - O ID do painel que está sendo ativado.
 */
function handleTabChange(painelId) {
    log.info(`Renderer: Aba ${painelId} ativada.`);
    switch (painelId) {
        case 'painel-produtores':
            carregarProdutores(); 
            break;
        case 'painel-servicos':
            carregarServicos();
            break;
        case 'painel-agendamento':
            carregarDadosAgendamento();
            break;
        case 'painel-historico':
            carregarExecucoes(); 
            break;
        case 'painel-pagamentos':
            carregarDadosPagamentos();
            break;
        case 'painel-relatorios':
            carregarDadosRelatorios(); 
            break;
        case 'painel-admin':
            handleListBackups(); 
            break;
    }
}

ui.inicializarApp({
    onTabChange: handleTabChange,
    onSaveProdutor: handleSaveProdutor,
    onClearProdutor: handleClearProdutor,

    onEditProdutor: handleEditProdutor,
    onDeleteProdutor: handleDeleteProdutor,
    onProdutorPaginaAnterior: handleProdutorPaginaAnterior,
    onProdutorPaginaProxima: handleProdutorPaginaProxima,
    onProdutorSearch: handleProdutorSearch,
    onProdutorClearSearch: handleProdutorClearSearch,

    onSaveServico: handleSaveServico,
    onClearServico: handleClearServico,

    onEditServico: handleEditServico,
    onDeleteServico: handleDeleteServico,
    onServicoPaginaAnterior: handleServicoPaginaAnterior,
    onServicoPaginaProxima: handleServicoPaginaProxima,

    onSaveExecucao: handleSaveExecucao,
    onClearAgendamento: handleClearAgendamento,

    onEditExecucao: handleEditExecucao,
    onDeleteExecucao: handleDeleteExecucao,
    onHistoricoPaginaAnterior: handleHistoricoPaginaAnterior,
    onHistoricoPaginaProxima: handleHistoricoPaginaProxima,
    onHistoricoSearch: handleHistoricoSearch,
    onHistoricoClearSearch: handleHistoricoClearSearch,

    onExecucaoSelecionada: handleExecucaoSelecionada,
    onSavePagamento: handleSavePagamento,
    onClearPagamento: handleClearPagamento,
    onCancelEditPagamento: handleCancelEditPagamento,
    onEditPagamento: handleEditPagamento,
    onDeletePagamento: handleDeletePagamento,
    onAgendamentoPagoSelecionado: handleAgendamentoPagoSelecionado,
    onRelatorioProdutorSelecionado: handleRelatorioProdutorSelecionado,

    onManualBackup: handleManualBackup,
    onRestoreBackup: handleRestoreBackup,
    onDeleteBackup: handleDeleteBackup,
    onVerExcluidos: handleVerExcluidos,
    onImportar: handleImportar
});