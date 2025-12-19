const { ipcMain } = require('electron');
const fetch = require('node-fetch'); 
const log = require('electron-log');

const API_URL = 'http://127.0.0.1:5000';

function registerProdutorHandlers() {

    ipcMain.handle('get-produtores', async (event, page, searchTerm, perPage) => { 
        try {
            const itemsPerPage = perPage || 10;
            let url = `${API_URL}/api/produtores?page=${page || 1}&per_page=${itemsPerPage}`;
            
            if (searchTerm) {
                url += `&q=${encodeURIComponent(searchTerm)}`;
            }
            
            log.info(`Buscando produtores: ${url}`); 
            const response = await fetch(url);

            if (!response.ok) {
                let errorBody = null; try { errorBody = await response.json(); } catch (e) { }
                const errorMessage = errorBody?.erro || `Erro na API: ${response.statusText}`;
                throw new Error(errorMessage);
            }
            
            return await response.json();

        } catch (error) {
            log.error('Falha ao buscar produtores:', error);
            return { error: error.message || 'Erro desconhecido no IPC Handler' };
        }
    });

    ipcMain.handle('create-produtor', async (event, produtorData) => {
        try {
            const response = await fetch(`${API_URL}/api/produtores`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(produtorData),
            });
            if (!response.ok) throw new Error(`Erro na API: ${response.statusText}`);
            return await response.json();
        } catch (error) {
            log.error('Falha ao criar produtor:', error);
            return null;
        }
    });

    ipcMain.handle('update-produtor', async (event, produtorId, produtorData) => {
        try {
            const response = await fetch(`${API_URL}/api/produtores/${produtorId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(produtorData),
            });
            if (!response.ok) throw new Error(`Erro na API: ${response.statusText}`);
            return await response.json();
        } catch (error) {
            log.error(`Falha ao atualizar produtor ${produtorId}:`, error);
            return null;
        }
    });

    ipcMain.handle('delete-produtor', async (event, produtorId) => {
        try {
            const response = await fetch(`${API_URL}/api/produtores/${produtorId}`, {
                method: 'DELETE',
            });
            if (!response.ok) throw new Error(`Erro na API: ${response.statusText}`);
            return await response.json();
        } catch (error) {
            log.error(`Falha ao deletar produtor ${produtorId}:`, error);
            return null;
        }
    });
}

module.exports = { registerProdutorHandlers };