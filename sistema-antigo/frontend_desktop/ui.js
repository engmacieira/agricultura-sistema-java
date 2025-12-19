import * as tabsUI from './ui_modules/tabsUI.js';
import * as produtorUI from './ui_modules/produtorUI.js';
import * as servicoUI from './ui_modules/servicoUI.js';
import * as agendamentoUI from './ui_modules/agendamentoUI.js';
import * as historicoUI from './ui_modules/historicoUI.js';
import * as pagamentoUI from './ui_modules/pagamentoUI.js';
import * as relatorioUI from './ui_modules/relatorioUI.js';
import * as adminUI from './ui_modules/adminUI.js';

/**
 * PONTO DE ENTRADA PRINCIPAL DA UI
 * Chamado pelo renderer.js. Esta função é responsável por ESPERAR o DOM.
 * @param {object} handlers - Objeto contendo todos os handlers do renderer.js
 */
export function inicializarApp(handlers) {
    console.log("UI Principal: Aguardando DOM Ready...");

    document.addEventListener('DOMContentLoaded', () => {
        console.log("UI Principal: DOM Carregado. Iniciando módulos...");

        tabsUI.inicializar(handlers.onTabChange);

        produtorUI.inicializar({
            onSaveProdutor: handlers.onSaveProdutor,
            onClearProdutor: handlers.onClearProdutor,
            onEditProdutor: handlers.onEditProdutor,
            onDeleteProdutor: handlers.onDeleteProdutor,
            onProdutorPaginaAnterior: handlers.onProdutorPaginaAnterior,
            onProdutorPaginaProxima: handlers.onProdutorPaginaProxima,
            onProdutorSearch: handlers.onProdutorSearch,
            onProdutorClearSearch: handlers.onProdutorClearSearch
        });

        servicoUI.inicializar({
            onSaveServico: handlers.onSaveServico,
            onClearServico: handlers.onClearServico,
            onEditServico: handlers.onEditServico,
            onDeleteServico: handlers.onDeleteServico,
            onServicoPaginaAnterior: handlers.onServicoPaginaAnterior,
            onServicoPaginaProxima: handlers.onServicoPaginaProxima
        });

        agendamentoUI.inicializar({
            onSaveExecucao: handlers.onSaveExecucao,
            onClearAgendamento: handlers.onClearAgendamento
        });

        historicoUI.inicializar({
            onEditExecucao: handlers.onEditExecucao,
            onDeleteExecucao: handlers.onDeleteExecucao,
            onHistoricoPaginaAnterior: handlers.onHistoricoPaginaAnterior,
            onHistoricoPaginaProxima: handlers.onHistoricoPaginaProxima,
            onHistoricoSearch: handlers.onHistoricoSearch,
            onHistoricoClearSearch: handlers.onHistoricoClearSearch

        });

        pagamentoUI.inicializar({
            onExecucaoSelecionada: handlers.onExecucaoSelecionada,
            onSavePagamento: handlers.onSavePagamento,
            onClearPagamento: handlers.onClearPagamento,
            onCancelEditPagamento: handlers.onCancelEditPagamento,
            onEditPagamento: handlers.onEditPagamento,
            onDeletePagamento: handlers.onDeletePagamento,
            onAgendamentoPagoSelecionado: handlers.onAgendamentoPagoSelecionado
        });

        relatorioUI.inicializar({
            onRelatorioProdutorSelecionado: handlers.onRelatorioProdutorSelecionado
        });

        adminUI.inicializar({
            onManualBackup: handlers.onManualBackup,
            onRestoreBackup: handlers.onRestoreBackup,
            onDeleteBackup: handlers.onDeleteBackup,
            onVerExcluidos: handlers.onVerExcluidos,
            onImportar: handlers.onImportar
        });
        
        handlers.onTabChange('painel-produtores');

        console.log("UI Principal: Todos os módulos inicializados.");
    });
}

export const trocarAba = tabsUI.trocarAba;

export const desenharListaProdutores = produtorUI.desenharListaProdutores;
export const limparFormularioProdutor = produtorUI.limparFormularioProdutor;
export const preencherFormularioProdutor = produtorUI.preencherFormularioProdutor;
export const coletarDadosProdutor = produtorUI.coletarDadosProdutor;
export const getIdProdutor = produtorUI.getIdProdutor;
export const getProdutorSearchTerm = produtorUI.getProdutorSearchTerm;
export const clearProdutorSearchTerm = produtorUI.clearProdutorSearchTerm;

export const desenharListaServicos = servicoUI.desenharListaServicos;
export const limparFormularioServico = servicoUI.limparFormularioServico;
export const preencherFormularioServico = servicoUI.preencherFormularioServico;
export const coletarDadosServico = servicoUI.coletarDadosServico;
export const getIdServico = servicoUI.getIdServico;

export const popularDropdownProdutores = agendamentoUI.popularDropdownProdutores;
export const popularDropdownServicos = agendamentoUI.popularDropdownServicos;
export const limparFormularioAgendamento = agendamentoUI.limparFormularioAgendamento;
export const preencherFormularioAgendamento = agendamentoUI.preencherFormularioAgendamento;
export const getIdAgendamento = agendamentoUI.getIdAgendamento;
export const coletarDadosAgendamento = agendamentoUI.coletarDadosAgendamento;

export const desenharListaExecucoes = historicoUI.desenharListaExecucoes;
export const getHistoricoSearchTerm = historicoUI.getHistoricoSearchTerm;
export const clearHistoricoSearchTerm = historicoUI.clearHistoricoSearchTerm;

export const popularDropdownExecucoesPendentes = pagamentoUI.popularDropdownExecucoesPendentes;
export const popularListaAgendamentosPagos = pagamentoUI.popularListaAgendamentosPagos;
export const exibirDetalhesExecucaoPagamentos = pagamentoUI.exibirDetalhesExecucaoPagamentos;
export const desenharListaPagamentos = pagamentoUI.desenharListaPagamentos;
export const limparFormularioPagamento = pagamentoUI.limparFormularioPagamento;
export const preencherFormularioPagamento = pagamentoUI.preencherFormularioPagamento;
export const coletarDadosPagamento = pagamentoUI.coletarDadosPagamento;
export const getIdPagamento = pagamentoUI.getIdPagamento;

export const popularDropdownRelatorioProdutores = relatorioUI.popularDropdownRelatorioProdutores;
export const desenharRelatorioDividas = relatorioUI.desenharRelatorioDividas;

export const desenharListaBackups = adminUI.desenharListaBackups;