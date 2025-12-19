const { ipcMain } = require('electron');
const fetch = require('node-fetch');
const log = require('electron-log');

const API_URL = 'http://127.0.0.1:5000'; 

function registerAdminHandlers() {

    ipcMain.handle('admin:importar', async (event, tipo, filePath) => {
        log.info(`[IPC Handler] Recebida solicitação de importação para '${tipo}' do arquivo: ${filePath}`);
        
        try {
            const response = await fetch(`${API_URL}/api/admin/importar/${tipo}`, { 
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ file_path: filePath }),
            });

            if (!response.ok) {
                let errorBody = null; 
                try { 
                    errorBody = await response.json(); 
                } catch (e) { 
                }
                const errorMessage = errorBody?.erro || `Erro na API: ${response.statusText}`;
                throw new Error(errorMessage);
            }
            
            return await response.json(); // Espera { "mensagem": "...", "sucesso": true, "total": X }

        } catch (error) {
            log.error(`[IPC Handler] Falha ao importar dados '${tipo}':`, error);
            return { sucesso: false, erro: error.message || 'Erro desconhecido no IPC Handler' };
        }
    });

ipcMain.handle('admin:run-backup', async () => {
        log.info(`[IPC Handler] Recebida solicitação de backup manual...`);
        try {
            const response = await fetch(`${API_URL}/api/admin/backup`, { 
                method: 'POST'
            });
            if (!response.ok) {
                const errorBody = await response.json();
                throw new Error(errorBody.erro || "Erro da API");
            }
            return await response.json();
        } catch (error) {
            log.error(`[IPC Handler] Falha no backup manual:`, error);
            return { sucesso: false, erro: error.message };
        }
    });

ipcMain.handle('admin:list-backups', async () => {
        log.info(`[IPC Handler] Solicitando lista de backups da API...`);
        try {
            const response = await fetch(`${API_URL}/api/admin/backups`);
            if (!response.ok) {
                const errorBody = await response.json();
                throw new Error(errorBody.erro || "Erro da API ao listar backups");
            }
            const files = await response.json(); // Espera um array de strings
            return files;
        } catch (error) {
            log.error(`[IPC Handler] Falha ao listar backups:`, error);
            return { erro: error.message };
        }
    });

}

module.exports = { registerAdminHandlers };