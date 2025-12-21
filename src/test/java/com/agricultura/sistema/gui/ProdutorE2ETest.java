package com.agricultura.sistema.gui;

import com.agricultura.sistema.IntegrationTestBase;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
        // 1. Aguarda a interface estabilizar (Renderização Inicial)
        WaitForAsyncUtils.waitForFxEvents();
        WaitForAsyncUtils.sleep(1000, TimeUnit.MILLISECONDS); // Espera extra de segurança para o Stage subir

        // --- BLOCO DE DIAGNÓSTICO BLINDADO (Anti-NullPointer) ---
        System.err.println("\n\n================= DIAGNOSTICO VISUAL =================");
        List<Window> windows = robot.robotContext().getWindowFinder().listWindows();
        System.err.println("Total de Janelas Detectadas: " + windows.size());

        windows.forEach(w -> {
            System.err.println("Janela: " + w);

            // VERIFICAÇÃO DE SEGURANÇA: Pula janelas sem Scene (Stages vazios/internos)
            Scene scene = w.getScene();
            if (scene == null) {
                System.err.println("   -> [ALERTA] Janela sem Scene (Ignorando...)");
                return;
            }

            if (scene.getRoot() == null) {
                System.err.println("   -> [ALERTA] Scene existe mas sem Root (Ignorando...)");
                return;
            }

            // Se chegou aqui, é uma janela válida. Vamos procurar o botão nela.
            var buscaBotao = robot.lookup("#btnProdutores").tryQuery();
            System.err.println("   -> Buscando '#btnProdutores' nesta janela: "
                    + (buscaBotao.isPresent() ? "ENCONTRADO!" : "NÃO ENCONTRADO"));

            // Lista botões visíveis para conferência (Debug)
            robot.lookup(".button").queryAll().forEach(b -> {
                if (b instanceof Button btn) {
                    System.err.println(
                            "      Botão visível -> Texto: [" + btn.getText() + "] | ID: [" + btn.getId() + "]");
                }
            });
        });
        System.err.println("================= FIM DO DIAGNOSTICO =================\n\n");
        // ----------------------------------------

        // Se o botão não existir, falha com uma mensagem clara
        if (robot.lookup("#btnProdutores").tryQuery().isEmpty()) {
            Assertions.fail(
                    "CRÍTICO: O botão '#btnProdutores' não foi encontrado. Verifique se o ID no FXML é realmente 'btnProdutores' e se a tela carregou.");
        }

        // --- FLUXO DE TESTE ---

        // 1. Clicar no botão do menu "Produtores"
        robot.clickOn("#btnProdutores");
        WaitForAsyncUtils.waitForFxEvents();

        // 2. Verificar se a tabela apareceu.
        robot.lookup("#tabelaProdutores").tryQuery().ifPresentOrElse(
                node -> Assertions.assertTrue(node.isVisible(), "A tabela de produtores deveria estar visível"),
                () -> Assertions.fail("A tabela de produtores (#tabelaProdutores) não foi encontrada após o clique."));

        // 3. Preencher formulário (Verifique se esses IDs batem com seu produtor.fxml)
        robot.clickOn("#txtNome").write("Produtor Teste E2E");

        // Dica: Se o campo tiver máscara, as vezes é melhor clicar e limpar antes
        // robot.clickOn("#txtCpf").eraseText(15).write("12345678900");
        robot.clickOn("#txtCpf").write("123.456.789-00");
        robot.clickOn("#txtTelefone").write("(99) 99999-9999");
        robot.clickOn("#txtApelido").write("Mestre do Teste");
        robot.clickOn("#txtRegiao").write("Vale do Silício");

        // 5. Clicar no botão "Salvar".
        robot.clickOn("#btnSalvar");

        // 6. Assert: Verificar Alerta
        WaitForAsyncUtils.waitForFxEvents();

        boolean successAlertFound = robot.robotContext().getWindowFinder().listWindows().stream()
                .filter(window -> window instanceof Stage)
                .map(window -> (Stage) window)
                .anyMatch(stage -> {
                    Scene s = stage.getScene();
                    return s != null && s.lookup(".dialog-pane") != null;
                });

        Assertions.assertTrue(successAlertFound, "Deveria ter aparecido um alerta de sucesso.");

        try {
            robot.clickOn("OK");
        } catch (Exception e) {
            // Ignorar se o robô já clicou ou se fechou rápido
        }
    }
}