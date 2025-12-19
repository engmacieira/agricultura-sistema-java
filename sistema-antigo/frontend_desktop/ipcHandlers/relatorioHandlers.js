const { ipcMain } = require('electron');
const fetch = require('node-fetch');
const log = require('electron-log');

const API_URL = 'http://127.0.0.1:5000';

function registerRelatorioHandlers() {

    ipcMain.handle('get-relatorio-dividas', async (event, produtorId) => {
        try {
            const response = await fetch(`${API_URL}/api/relatorios/produtor/${produtorId}/dividas`);
            
            if (!response.ok) {
                let errorBody = null; try { errorBody = await response.json(); } catch (e) { }
                const errorMessage = errorBody?.erro || `Erro na API: ${response.statusText}`;
                throw new Error(errorMessage);
            }
            return await response.json(); 
            
        } catch (error) {
            log.error(`Falha ao buscar relatório de dívidas para produtor ${produtorId}:`, error);
            return { error: error.message || 'Erro desconhecido ao buscar relatório.' };
        }
    });

}

module.exports = { registerRelatorioHandlers };