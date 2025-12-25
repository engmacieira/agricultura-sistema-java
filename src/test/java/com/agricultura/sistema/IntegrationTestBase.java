package com.agricultura.sistema;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import java.time.Instant;
import java.util.concurrent.TimeoutException;

@ExtendWith(ApplicationExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public abstract class IntegrationTestBase {

    @Autowired
    protected ApplicationContext applicationContext;

    @BeforeAll
    public static void setupSpec() throws Exception {
        // Força configurações de vídeo para evitar modo headless acidental
        System.setProperty("java.awt.headless", "false");
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "false");
        System.setProperty("prism.order", "sw");

        // Registra o Stage primário uma única vez
        try {
            FxToolkit.registerPrimaryStage();
        } catch (Exception e) {
            // Ignora se já estiver registrado
        }
    }

    @BeforeEach
    public void setup() throws Exception {
        // Configuração MANUAL do Stage (A que funcionava para você!)
        FxToolkit.setupStage(stage -> {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
                // O PULO DO GATO: Injeção do Spring
                fxmlLoader.setControllerFactory(applicationContext::getBean);
                Parent root = fxmlLoader.load();

                Scene scene = new Scene(root, 1200, 800);
                stage.setScene(scene);
                stage.setTitle("Teste E2E - " + Instant.now()); // Muda o título para vermos que atualizou

                stage.show();
                stage.toFront();
                stage.requestFocus();
            } catch (Exception e) {
                throw new RuntimeException("Falha ao carregar FXML no setup manual", e);
            }
        });

        // Espera a janela renderizar totalmente antes de soltar para o teste
        WaitForAsyncUtils.waitForFxEvents();
    }

    @AfterEach
    public void tearDown() throws TimeoutException {
        // Limpa o palco para o próximo teste não herdar lixo
        FxToolkit.hideStage();
        WaitForAsyncUtils.waitForFxEvents();
    }
}