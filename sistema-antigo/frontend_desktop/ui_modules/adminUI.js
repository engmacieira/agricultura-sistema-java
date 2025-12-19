let listaBackups;
let listaItensExcluidos;
let btnBackupManual;
let btnVerProdutoresExcluidos;
let btnVerServicosExcluidos;
let btnImportarProdutores, btnImportarServicos, btnImportarExecucoes, btnImportarPagamentos;

let handlers = {};

function _formatarNomeBackup(filename) {
    try {
        const parts = filename.replace('gestao_backup_', '').replace('.db', '');
        const [date, time] = parts.split('T');
        const [ano, mes, dia] = date.split('-');
        const [hora, min, seg] = time.split('-');
        return `${dia}/${mes}/${ano} - ${hora}:${min}:${seg}`;
    } catch (e) {
        return filename; 
    }
}

function _inicializarDOM() {
    listaBackups = document.getElementById('lista-backups');
    listaItensExcluidos = document.getElementById('lista-itens-excluidos');
    btnBackupManual = document.getElementById('admin-btn-backup-manual');
    btnVerProdutoresExcluidos = document.getElementById('admin-btn-ver-produtores');
    btnVerServicosExcluidos = document.getElementById('admin-btn-ver-servicos');
    btnImportarProdutores = document.getElementById('admin-btn-importar-produtores');
    btnImportarServicos = document.getElementById('admin-btn-importar-servicos');
    btnImportarExecucoes = document.getElementById('admin-btn-importar-execucoes');
    btnImportarPagamentos = document.getElementById('admin-btn-importar-pagamentos');
    console.log("AdminUI: Elementos DOM 'cacheados'.");
}

function _vincularEventos() {
    if (btnBackupManual) {
        btnBackupManual.addEventListener('click', handlers.onManualBackup);
    }
    if (btnVerProdutoresExcluidos) {
        btnVerProdutoresExcluidos.addEventListener('click', () => handlers.onVerExcluidos('produtores'));
    }
     if (btnVerServicosExcluidos) {
        btnVerServicosExcluidos.addEventListener('click', () => handlers.onVerExcluidos('servicos'));
    }
    if (btnImportarProdutores) {
        btnImportarProdutores.addEventListener('click', () => handlers.onImportar('produtores'));
    }
    if (btnImportarServicos) {
        btnImportarServicos.addEventListener('click', () => handlers.onImportar('servicos'));
    }
    if (btnImportarExecucoes) {
        btnImportarExecucoes.addEventListener('click', () => handlers.onImportar('execucoes'));
    }
    if (btnImportarPagamentos) {
        btnImportarPagamentos.addEventListener('click', () => handlers.onImportar('pagamentos'));
    }
    console.log("AdminUI: Eventos vinculados.");
}

/**
 * Inicializa o módulo UI de Admin.
 * @param {object} externalHandlers - Handlers vindos do renderer.js
 */
export function inicializar(externalHandlers) {
    handlers = externalHandlers;
    _inicializarDOM();
    _vincularEventos();
}

/**
 * Desenha a lista de backups na tela.
 * @param {string[]} backupFiles - Array de nomes de arquivos de backup.
 */
export function desenharListaBackups(backupFiles) {
    if (!listaBackups) return;
    listaBackups.innerHTML = '';

    if (!backupFiles || backupFiles.length === 0) {
        listaBackups.innerHTML = '<li>Nenhum backup manual encontrado.</li>';
        return;
    }

    backupFiles.sort().reverse(); 

    backupFiles.forEach(filename => {
        const item = document.createElement('li');
        
        const infoContainer = document.createElement('div');
        infoContainer.classList.add('list-item-info');
        infoContainer.innerHTML = `
            <div class="list-item-main">${_formatarNomeBackup(filename)}</div>
            <div class="list-item-secondary">${filename}</div>
        `;

        const actionsContainer = document.createElement('div');
        actionsContainer.classList.add('list-item-actions');

        const btnRestaurar = document.createElement('button');
        btnRestaurar.textContent = 'Restaurar'; 
        btnRestaurar.classList.add('btn-primary', 'btn-action');
        btnRestaurar.onclick = () => handlers.onRestoreBackup(filename); 

        const btnExcluir = document.createElement('button');
        btnExcluir.textContent = 'Excluir'; 
        btnExcluir.classList.add('btn-delete', 'btn-action');
        btnExcluir.onclick = () => handlers.onDeleteBackup(filename); 

        actionsContainer.appendChild(btnRestaurar);
        actionsContainer.appendChild(btnExcluir);
        
        item.appendChild(infoContainer);
        item.appendChild(actionsContainer);
        listaBackups.appendChild(item);
    });
}