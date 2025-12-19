const { ipcMain } = require('electron');
const fetch = require('node-fetch');
const log = require('electron-log');

const API_URL = 'http://127.0.0.1:5000'; 

function registerPagamentoHandlers() {

    ipcMain.handle('get-pagamentos-por-execucao', async (event, execucaoId) => {
        try {
            const response = await fetch(`${API_URL}/api/execucoes/${execucaoId}/pagamentos`);
            if (!response.ok) {
                throw new Error(`Erro na API: ${response.statusText}`);
            }
            return await response.json();
        } catch (error) {
            log.error(`Falha ao buscar pagamentos para execução ${execucaoId}:`, error);
            return []; 
        }
    });

    ipcMain.handle('create-pagamento', async (event, execucaoId, pagamentoData) => {
        try {
            const response = await fetch(`${API_URL}/api/execucoes/${execucaoId}/pagamentos`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(pagamentoData), 
            });
            if (!response.ok) {
                let errorBody = null; try { errorBody = await response.json(); } catch (e) { }
                const errorMessage = errorBody?.erro || `Erro na API: ${response.statusText}`;
                throw new Error(errorMessage);
            }
            return await response.json(); 
        } catch (error) {
            log.error(`Falha ao criar pagamento para execução ${execucaoId}:`, error);
            return { error: error.message || 'Erro desconhecido ao criar pagamento.' };
        }
    });

    ipcMain.handle('update-pagamento', async (event, pagamentoId, pagamentoData) => {
        try {
            const response = await fetch(`${API_URL}/api/pagamentos/${pagamentoId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(pagamentoData), 
            });
             if (!response.ok) {
                let errorBody = null; try { errorBody = await response.json(); } catch (e) { }
                const errorMessage = errorBody?.erro || `Erro na API: ${response.statusText}`;
                throw new Error(errorMessage);
            }
            return await response.json(); 
        } catch (error) {
            log.error(`Falha ao atualizar pagamento ${pagamentoId}:`, error);
             return { error: error.message || 'Erro desconhecido ao atualizar pagamento.' };
        }
    });

    ipcMain.handle('delete-pagamento', async (event, pagamentoId) => {
        try {
            const response = await fetch(`${API_URL}/api/pagamentos/${pagamentoId}`, {
                method: 'DELETE',
            });
             if (!response.ok) {
                let errorBody = null; try { errorBody = await response.json(); } catch (e) { }
                const errorMessage = errorBody?.erro || `Erro na API: ${response.statusText}`;
                throw new Error(errorMessage);
            }
            return await response.json(); 
        } catch (error) {
            log.error(`Falha ao deletar pagamento ${pagamentoId}:`, error);
            return { error: error.message || 'Erro desconhecido ao deletar pagamento.' };
        }
    });

}

module.exports = { registerPagamentoHandlers };