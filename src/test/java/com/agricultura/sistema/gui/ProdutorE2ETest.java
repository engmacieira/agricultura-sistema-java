package com.agricultura.sistema.gui;

import com.agricultura.sistema.IntegrationTestBase;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;
import org.testfx.util.WaitForAsyncUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ProdutorE2ETest extends IntegrationTestBase {

    @Test
    public void testCadastrarProdutor(FxRobot robot) throws TimeoutException {
        // Aguarda a interface estabilizar antes de começar
        WaitForAsyncUtils.waitForFxEvents();

        // --- BLOCO DE DIAGNÓSTICO (Usando System.err para aparecer no log) ---
        System.err.println("\n\n================= DIAGNOSTICO VISUAL =================");
        int janelas = robot.robotContext().getWindowFinder().listWindows().size();
        System.err.println("Total de Janelas Abertas: " + janelas);

        robot.robotContext().getWindowFinder().listWindows().forEach(w -> {
            System.err.println("Janela detectada: " + w);
            if (w.getScene() != null && w.getScene().getRoot() != null) {
                // Procura especificamente o botão
                var buscaBotao = robot.lookup("#btnProdutores").tryQuery();
                System.err.println("   -> Buscando '#btnProdutores' nesta janela: "
                        + (buscaBotao.isPresent() ? "ENCONTRADO!" : "NÃO ENCONTRADO"));

                // Lista todos os botões visíveis para conferência
                robot.lookup(".button").queryAll().forEach(b -> {
                    javafx.scene.control.Button btn = (javafx.scene.control.Button) b;
                    System.err.println(
                            "      Botão visível -> Texto: [" + btn.getText() + "] | ID: [" + btn.getId() + "]");
                });
            }
        });
        System.err.println("================= FIM DO DIAGNOSTICO =================\n\n");
        // ----------------------------------------

        // Se o botão não existir, falha com uma mensagem clara
        if (robot.lookup("#btnProdutores").tryQuery().isEmpty()) {
            Assertions.fail(
                    "CRÍTICO: O botão '#btnProdutores' não foi encontrado na tela. Verifique o diagnóstico acima.");
        }

        // 1. Clicar no botão do menu "Produtores"
        robot.clickOn("#btnProdutores");

        // 2. Verificar se a tabela apareceu.
        robot.lookup("#tabelaProdutores").tryQuery().ifPresentOrElse(
                node -> Assertions.assertTrue(node.isVisible(), "A tabela de produtores deveria estar visível"),
                () -> Assertions.fail("A tabela de produtores não foi encontrada"));

        // 3. Preencher formulário
        robot.clickOn("#txtNome").write("Produtor Teste E2E");
        robot.clickOn("#txtCpf").write("123.456.789-00");
        robot.clickOn("#txtTelefone").write("(99) 99999-9999");
        robot.clickOn("#txtApelido").write("Mestre do Teste");
        robot.clickOn("#txtRegiao").write("Vale do Silício");

        // 5. Clicar no botão "Salvar".
        robot.clickOn("#btnSalvar");

        // 6. Assert: Verificar Alerta
        WaitForAsyncUtils.waitForFxEvents(); // Espera o alerta aparecer

        List<Window> windows = robot.robotContext().getWindowFinder().listWindows();
        boolean successAlertFound = windows.stream()
                .filter(window -> window instanceof Stage)
                .map(window -> (Stage) window)
                .anyMatch(stage -> {
                    Scene scene = stage.getScene();
                    if (scene == null)
                        return false;
                    return scene.lookup(".dialog-pane") != null;
                });

        Assertions.assertTrue(successAlertFound, "Deveria ter aparecido um alerta de sucesso.");

        try {
            robot.clickOn("OK");
        } catch (Exception e) {
            // Ignorar
        }
    }
}