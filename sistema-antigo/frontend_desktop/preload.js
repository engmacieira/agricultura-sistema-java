const { contextBridge, ipcRenderer } = require('electron');

contextBridge.exposeInMainWorld('api', {

  getProdutores: (page, searchTerm, perPage) => ipcRenderer.invoke('get-produtores', page, searchTerm, perPage), 
  createProdutor: (produtorData) => ipcRenderer.invoke('create-produtor', produtorData),
  updateProdutor: (produtorId, produtorData) => ipcRenderer.invoke('update-produtor', produtorId, produtorData),
  deleteProdutor: (produtorId) => ipcRenderer.invoke('delete-produtor', produtorId),

  getServicos: (page, perPage) => ipcRenderer.invoke('get-servicos', page, perPage),
  createServico: (servicoData) => ipcRenderer.invoke('create-servico', servicoData),
  updateServico: (servicoId, servicoData) => ipcRenderer.invoke('update-servico', servicoId, servicoData),
  deleteServico: (servicoId) => ipcRenderer.invoke('delete-servico', servicoId),

  createExecucao: (execucaoData) => ipcRenderer.invoke('create-execucao', execucaoData),
  getExecucoes: (page, status, searchTerm) => ipcRenderer.invoke('get-execucoes', page, status, searchTerm), 
  updateExecucao: (execucaoId, execucaoData) => ipcRenderer.invoke('update-execucao', execucaoId, execucaoData),
  deleteExecucao: (execucaoId) => ipcRenderer.invoke('delete-execucao', execucaoId),

  getPagamentosPorExecucao: (execucaoId) => {
    return ipcRenderer.invoke('get-pagamentos-por-execucao', execucaoId);
  },
  createPagamento: (execucaoId, pagamentoData) => {
    return ipcRenderer.invoke('create-pagamento', execucaoId, pagamentoData);
  },
  updatePagamento: (pagamentoId, pagamentoData) => {
    return ipcRenderer.invoke('update-pagamento', pagamentoId, pagamentoData);
  },
  deletePagamento: (pagamentoId) => {
    return ipcRenderer.invoke('delete-pagamento', pagamentoId);
  },
  
  getRelatorioDividas: (produtorId) => {
    return ipcRenderer.invoke('get-relatorio-dividas', produtorId);
  },

  adminImportar: (tipo, filePath) => {
    return ipcRenderer.invoke('admin:importar', tipo, filePath);
  },

  adminRunBackup: () => ipcRenderer.invoke('admin:run-backup'),
  adminListBackups: () => ipcRenderer.invoke('admin:list-backups'),
  
  dialog: {
    alert: (message) => ipcRenderer.invoke('dialog:alert', message),
    confirm: (message) => ipcRenderer.invoke('dialog:confirm', message)
  },

  log: {
    info: (...args) => ipcRenderer.send('log:info', ...args),
    warn: (...args) => ipcRenderer.send('log:warn', ...args),
    error: (...args) => ipcRenderer.send('log:error', ...args)
  }

});