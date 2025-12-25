package com.agricultura.sistema;

import org.junit.jupiter.api.Test;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.util.WaitForAsyncUtils;

public class FluxoNavegacaoTest extends IntegrationTestBase {

    @Test
    void deveNavegarPorTodoMenuLateral(FxRobot robot) {
        // Pausa rápida apenas para garantir renderização inicial
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }

        // 1. Validação Inicial
        FxAssert.verifyThat("Gestor Sol", NodeMatchers.isVisible());

        // --- NAVEGAÇÃO COMPLETA ---

        // 2. Produtores
        robot.clickOn("Produtores");
        WaitForAsyncUtils.waitForFxEvents();
        // Verifica se carregou algo da tela de produtores (ex: um botão de "Novo" ou
        // título)
        // Se este passo falhar, verifique qual texto existe na tela de produtores
        FxAssert.verifyThat("Produtores", NodeMatchers.isVisible());

        // 3. Serviços
        robot.clickOn("Serviços");
        WaitForAsyncUtils.waitForFxEvents();

        // 4. Execuções (Adicionado)
        robot.clickOn("Execuções");
        WaitForAsyncUtils.waitForFxEvents();

        // 5. Pagamentos (Adicionado)
        robot.clickOn("Pagamentos");
        WaitForAsyncUtils.waitForFxEvents();

        // 6. Relatórios
        robot.clickOn("Relatórios");
        WaitForAsyncUtils.waitForFxEvents();

        // 7. Administração (Adicionado)
        robot.clickOn("Administração");
        WaitForAsyncUtils.waitForFxEvents();

        // --- SAÍDA ---

        // 8. Validação do botão de Sair
        FxAssert.verifyThat("Sair do Sistema", NodeMatchers.isVisible());
    }
}