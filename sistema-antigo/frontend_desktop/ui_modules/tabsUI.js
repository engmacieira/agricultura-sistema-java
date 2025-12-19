let tabButtons;
let tabPanes;
let onTabChangeCallback; 

function _inicializarDOM() {
    tabButtons = document.querySelectorAll('.tab-button');
    tabPanes = document.querySelectorAll('.tab-pane');
    console.log("TabsUI: Elementos DOM das abas 'cacheados'.");
}

function _vincularEventos() {
    if (!tabButtons) return; 
    tabButtons.forEach(button => {
        button.addEventListener('click', () => {
            const targetPaneId = trocarAba(button);
            if (onTabChangeCallback) onTabChangeCallback(targetPaneId);
        });
    });
    console.log("TabsUI: Eventos das abas vinculados.");
}

export function trocarAba(buttonElement) {
    const targetPaneId = buttonElement.dataset.tab;
    if (tabButtons) {
        tabButtons.forEach(btn => btn.classList.toggle('active', btn === buttonElement));
    }
    if (tabPanes) {
        tabPanes.forEach(pane => pane.classList.toggle('active', pane.id === targetPaneId));
    }
    return targetPaneId;
}

/**
 * Inicializa o módulo de abas.
 * @param {function} callback - Função a ser chamada quando uma aba mudar.
 */
export function inicializar(callback) {
    onTabChangeCallback = callback;
    _inicializarDOM();
    _vincularEventos();
}