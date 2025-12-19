/*
 * utils.js
 * Arquivo para funções auxiliares puras.
 * Vamos usar o formato CommonJS (module.exports) por enquanto,
 * para ser compatível com o Jest sem configuração extra.
 */

/**
 * Soma dois números. Uma função pura de exemplo.
 * @param {number} a - O primeiro número.
 * @param {number} b - O segundo número.
 * @returns {number} - A soma de a e b.
 */
export function soma(a, b) {
  return a + b;
}

// Exporta a função para que os testes possam usá-la
module.exports = { soma };