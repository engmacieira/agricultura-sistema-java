const { ipcMain, dialog, BrowserWindow } = require('electron');
const log = require('electron-log'); 

function registerLogHandlers() {

    ipcMain.on('log:info', (event, ...args) => {
        log.info(...args);
    });

    ipcMain.on('log:warn', (event, ...args) => {
        log.warn(...args);
    });

    ipcMain.on('log:error', (event, ...args) => {
        log.error(...args);
    });

ipcMain.handle('dialog:alert', (event, message) => {
        const window = BrowserWindow.fromWebContents(event.sender);
        dialog.showMessageBoxSync(window, {
            type: 'info',
            message: message,
            title: 'Gestor Sol',
            buttons: ['OK']
        });
        return true; 
    });

    ipcMain.handle('dialog:confirm', (event, message) => {
        const window = BrowserWindow.fromWebContents(event.sender);
        
        const choice = dialog.showMessageBoxSync(window, {
            type: 'question',
            message: message,
            title: 'Confirmação',
            buttons: ['Cancelar', 'Confirmar'], 
            defaultId: 0,
            cancelId: 0
        });
        
        return (choice === 1); 
    });

    console.log("Handlers de Log e Diálogo registrados.");
}

module.exports = { registerLogHandlers };