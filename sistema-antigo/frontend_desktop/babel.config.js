/*
 * babel.config.js
 * Arquivo de configuração do Babel.
 * Ele diz ao babel-jest como 'traduzir' nosso código antes de enviá-lo ao Jest.
 */

module.exports = {
  presets: [
    [
      '@babel/preset-env',
      {
        targets: {
          node: 'current',
        },
      },
    ],
  ],
};