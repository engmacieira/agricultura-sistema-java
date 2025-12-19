const { ipcMain } = require('electron');
const fetch = require('node-fetch');
const log = require('electron-log');

const API_URL = 'http://127.0.0.1:5000';

function registerServicoHandlers() {

    ipcMain.handle('get-servicos', async (event, page, perPage) => { 
        try {
            const itemsPerPage = perPage || 10;
            const response = await fetch(`${API_URL}/api/servicos?page=${page || 1}&per_page=10`);
            if (!response.ok) throw new Error(`Erro na API: ${response.statusText}`);
            return await response.json();
        } catch (error) {
            log.error('Falha ao buscar serviços:', error);
            return [];
        }
    });

    ipcMain.handle('create-servico', async (event, servicoData) => {
        try {
            const response = await fetch(`${API_URL}/api/servicos`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(servicoData),
            });
            if (!response.ok) throw new Error(`Erro na API: ${response.statusText}`);
            return await response.json();
        } catch (error) {
            log.error('Falha ao criar serviço:', error);
            return null;
        }
    });

    ipcMain.handle('update-servico', async (event, servicoId, servicoData) => {
        try {
            const response = await fetch(`${API_URL}/api/servicos/${servicoId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(servicoData),
            });
            if (!response.ok) throw new Error(`Erro na API: ${response.statusText}`);
            return await response.json();
        } catch (error) {
            log.error(`Falha ao atualizar serviço ${servicoId}:`, error);
            return null;
        }
    });

    ipcMain.handle('delete-servico', async (event, servicoId) => {
        try {
            const response = await fetch(`${API_URL}/api/servicos/${servicoId}`, {
                method: 'DELETE',
            });
            if (!response.ok) throw new Error(`Erro na API: ${response.statusText}`);
            return await response.json();
        } catch (error) {
            log.error(`Falha ao deletar serviço ${servicoId}:`, error);
            return null;
        }
    });
}

module.exports = { registerServicoHandlers };