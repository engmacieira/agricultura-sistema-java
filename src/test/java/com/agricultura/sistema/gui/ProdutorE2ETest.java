package com.agricultura.sistema.gui;

import com.agricultura.sistema.IntegrationTestBase;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;

import java.util.List;

public class ProdutorE2ETest extends IntegrationTestBase {

    @Test
    public void testCadastrarProdutor(FxRobot robot) {
        // 1. Clicar no botão do menu "Produtores".
        robot.clickOn("Produtores");

        // 2. Verificar se a tabela apareceu.
        robot.lookup("#tabelaProdutores").tryQuery().ifPresentOrElse(
                node -> Assertions.assertTrue(node.isVisible(), "A tabela de produtores deveria estar visível"),
                () -> Assertions.fail("A tabela de produtores não foi encontrada"));

        // 3. Preencher o campo "Nome" (Input ID: #txtNome).
        robot.clickOn("#txtNome").write("Produtor Teste E2E");

        // 4. Preencher "CPF" e "Telefone".
        robot.clickOn("#txtCpf").write("123.456.789-00");
        robot.clickOn("#txtTelefone").write("(99) 99999-9999");

        // Preencher outros campos obrigatórios ou úteis
        robot.clickOn("#txtApelido").write("Mestre do Teste");
        robot.clickOn("#txtRegiao").write("Vale do Silício");

        // 5. Clicar no botão "Salvar".
        robot.clickOn("#btnSalvar");

        // 6. Assert: Verificar se o novo produtor apareceu na tabela ou se um Alerta de
        // sucesso foi exibido.
        List<Window> windows = robot.robotContext().getWindowFinder().listWindows();
        boolean successAlertFound = windows.stream()
                .filter(window -> window instanceof Stage)
                .map(window -> (Stage) window)
                .anyMatch(stage -> {
                    Scene scene = stage.getScene();
                    if (scene == null)
                        return false;
                    // Alerts usually have a DialogPane
                    return scene.lookup(".dialog-pane") != null;
                });

        Assertions.assertTrue(successAlertFound, "Deveria ter aparecido um alerta de sucesso.");

        // Tentar fechar qualquer alerta que tenha aparecido
        try {
            robot.clickOn("OK");
        } catch (Exception e) {
            // Ignorar
        }
    }
}
