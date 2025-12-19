const { app, BrowserWindow } = require('electron');
const path = require('path');
const fs = require('fs');
const log = require('electron-log');
const fetch = require('node-fetch');

const { spawn } = require('child_process');
const { registerIpcHandlers } = require('./ipcHandlers');

log.info('Aplicação iniciada (main.js)');

let backendProcess = null;

async function runBackupRoutine() {
    log.info("[Backup] Acionando rotina de backup (via API)...");
    try {
        const response = await fetch(`${API_URL}/api/admin/backup`, { 
            method: 'POST'
        });

        if (!response.ok) {
            const errorBody = await response.json();
            throw new Error(errorBody.erro || response.statusText);
        }

        const result = await response.json();
        log.info(`[Backup] Sucesso: ${result.mensagem}`);

    } catch (error) {
        log.error(`[Backup] Falha grave ao rodar a rotina de backup: ${error.message}`);
    }
}

const userDataPath = app.getPath('userData');
const PURGE_TRACKER_FILE = path.join(userDataPath, 'last_purge.txt');
const API_URL = 'http://127.0.0.1:5000'; 

async function runPurgeRoutine() {
    const today = new Date().toISOString().split('T')[0]; 
    let lastPurgeDate = '';

    if (fs.existsSync(PURGE_TRACKER_FILE)) {
        lastPurgeDate = fs.readFileSync(PURGE_TRACKER_FILE, 'utf-8');
    }

    if (lastPurgeDate === today) {
        log.info('[Purge] Rotina de limpeza já executada hoje. Pulando.');
        return;
    }

    log.warn('[Purge] Rotina de limpeza diária iniciada...');
    
    try {
        const response = await fetch(`${API_URL}/api/admin/purge`, {
            method: 'POST'
        });

        if (!response.ok) {
            const errorBody = await response.json();
            throw new Error(errorBody.erro || response.statusText);
        }

        const result = await response.json();
        log.info(`[Purge] Sucesso: ${result.mensagem}`);
        
        fs.writeFileSync(PURGE_TRACKER_FILE, today, 'utf-8');

    } catch (error) {
        log.error(`[Purge] Falha grave ao rodar a rotina de limpeza: ${error.message}`);
    }
}

function startBackend() {
    const isProd = app.isPackaged;
    
    let backendExePath;
    let backendWorkingDir;

    if (isProd) {
        backendWorkingDir = path.join(process.resourcesPath, 'backend');
        backendExePath = path.join(backendWorkingDir, 'run.exe');
        log.info(`[Backend] Modo Produção. Diretório: ${backendWorkingDir}`);
    } else {
        backendWorkingDir = path.join(__dirname, '../backend_api/dist/');
        backendExePath = path.join(backendWorkingDir, 'run.exe');
        log.info(`[Backend] Modo Desenvolvimento. Diretório: ${backendWorkingDir}`);
    }

    log.info(`[Backend] Iniciando backend em: ${backendExePath}`);
    
    const userDataPath = app.getPath('userData');
    log.info(`[Backend] Passando userData path para o backend: ${userDataPath}`);

    backendProcess = spawn(backendExePath, [userDataPath], { 
        cwd: backendWorkingDir,
        windowsHide: true 
    });

    backendProcess.stdout.on('data', (data) => {
        log.info(`[Backend STDOUT]: ${data.toString()}`);
    });
    backendProcess.stderr.on('data', (data) => {
        log.error(`[Backend STDERR]: ${data.toString()}`);
    });
    backendProcess.on('close', (code) => {
        log.warn(`[Backend] Processo encerrado com código: ${code}`);
    });
    backendProcess.on('error', (err) => {
        log.error(`[Backend] Falha ao iniciar processo: ${err.message}`);
    });
}

function pingBackend(url, timeout = 5000) {
    const startTime = Date.now();
    return new Promise((resolve, reject) => {
        const tryConnect = () => {
            fetch(url)
                .then(() => {
                    log.info(`[Backend] Conexão com API (ping) bem-sucedida.`);
                    resolve(true);
                })
                .catch(() => {
                    const elapsedTime = Date.now() - startTime;
                    if (elapsedTime > timeout) {
                        reject(new Error(`Timeout: API do backend não respondeu em ${timeout}ms.`));
                    } else {
                        setTimeout(tryConnect, 500); 
                    }
                });
        };
        tryConnect();
    });
}

function createWindow () {
  const win = new BrowserWindow({
    width: 1024,
    height: 768,
    webPreferences: {
        preload: path.join(__dirname, 'preload.js') 
    }
  });

  win.loadFile('index.html');
}

app.whenReady().then(async () => {

try{

  log.info("Iniciando aplicação, iniciando backend...");
  startBackend();

  log.info("Verificando backend online...");
  await pingBackend(`${API_URL}/health`);

  log.info("...backend online, rodando rotina de backup...");
  await runBackupRoutine();
  
  log.info("...rodando rotina de purge...");
  await runPurgeRoutine();

  } catch (error) {
    log.error(`[Startup] Falha grave na inicialização: ${error.message}`);
  }

  registerIpcHandlers();
  
  createWindow();

  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) {
      createWindow();
    }
  });
});

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    log.info("[Shutdown] Todas as janelas fechadas. Encerrando o backend...");
    if (backendProcess) {
        backendProcess.kill();
        log.info("[Shutdown] Backend encerrado.");
    }
    app.quit();
  }
});