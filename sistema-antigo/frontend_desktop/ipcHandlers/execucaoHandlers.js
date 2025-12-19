const { ipcMain } = require('electron');
const fetch = require('node-fetch');
const log = require('electron-log');

const API_URL = 'http://127.0.0.1:5000'; 

function registerExecucaoHandlers() {

    ipcMain.handle('create-execucao', async (event, execucaoData) => {
        try {
            const response = await fetch(`${API_URL}/api/execucoes`, { 
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(execucaoData),
            });
            if (!response.ok) {
                let errorBody = null; try { errorBody = await response.json(); } catch (e) { }
                const errorMessage = errorBody?.erro || `Erro na API: ${response.statusText}`;
                throw new Error(errorMessage);
            }
            return await response.json();
        } catch (error) {
            log.error('Falha ao criar execução:', error);
            return { error: error.message || 'Erro desconhecido ao criar execução.' };
        }
    });

    ipcMain.handle('get-execucoes', async (event, page, status, searchTerm) => { 
        try {
            const perPage = (status === 'pendentes' || status === 'pagas') ? 500 : 10;
            
            let url = `${API_URL}/api/execucoes?page=${page || 1}&per_page=${perPage}&status=${status || 'todos'}`;
            
            if (searchTerm) {
                url += `&q=${encodeURIComponent(searchTerm)}`;
            }
            
            log.info(`Buscando execuções: ${url}`); 
            const response = await fetch(url);

            if (!response.ok) {
                let errorBody = null; try { errorBody = await response.json(); } catch (e) { }
                const errorMessage = errorBody?.erro || `Erro na API: ${response.statusText}`;
                throw new Error(errorMessage);
            }
            
            return await response.json();

        } catch (error) {
            log.error('Falha ao buscar execuções:', error);
            return { error: error.message || 'Erro desconhecido no IPC Handler' };
        }
    });

    ipcMain.handle('update-execucao', async (event, execucaoId, execucaoData) => {
        try {
            const response = await fetch(`${API_URL}/api/execucoes/${execucaoId}`, { 
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(execucaoData),
            });
             if (!response.ok) {
                let errorBody = null; try { errorBody = await response.json(); } catch (e) { }
                const errorMessage = errorBody?.erro || `Erro na API: ${response.statusText}`;
                throw new Error(errorMessage);
            }
            return await response.json(); 
        } catch (error) {
            log.error(`Falha ao atualizar execução ${execucaoId}:`, error);
             return { error: error.message || 'Erro desconhecido ao atualizar execução.' };
        }
    });

    ipcMain.handle('delete-execucao', async (event, execucaoId) => {
        try {
            const response = await fetch(`${API_URL}/api/execucoes/${execucaoId}`, { 
                method: 'DELETE',
            });
             if (!response.ok) {
                let errorBody = null; try { errorBody = await response.json(); } catch (e) { }
                const errorMessage = errorBody?.erro || `Erro na API: ${response.statusText}`;
                throw new Error(errorMessage);
            }
            return await response.json();
        } catch (error) {
            log.error(`Falha ao deletar execução ${execucaoId}:`, error);
            return { error: error.message || 'Erro desconhecido ao deletar execução.' };
        }
    });

}

module.exports = { registerExecucaoHandlers };