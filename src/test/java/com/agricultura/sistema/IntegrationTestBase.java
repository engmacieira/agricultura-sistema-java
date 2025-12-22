package com.agricultura.sistema;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
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

import java.util.concurrent.TimeoutException;

@ExtendWith(ApplicationExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public abstract class IntegrationTestBase {

    @Autowired
    protected ApplicationContext applicationContext;

    @BeforeAll
    public static void setupSpec() throws Exception {
        // 1. Configurações de Sistema
        System.setProperty("java.awt.headless", "false");
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "false");
        System.setProperty("prism.order", "sw");

        // 2. Garante que o JavaFX Thread esteja vivo
        try {
            FxToolkit.registerPrimaryStage();
            Platform.setImplicitExit(false);
        } catch (Exception e) {
            // Ignora se já estiver registrado
        }
    }

    @BeforeEach
    public void setup() throws Exception {
        System.err.println(">>> SETUP: Iniciando configuração manual do Stage...");

        // 3. Configuração MANUAL do Stage (Substitui o @Start que estava falhando)
        FxToolkit.setupStage(stage -> {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
                fxmlLoader.setControllerFactory(applicationContext::getBean);
                Parent root = fxmlLoader.load();

                Scene scene = new Scene(root, 1200, 800);
                stage.setScene(scene);
                stage.setTitle("Sistema Agricultura - Teste E2E");

                stage.show();
                stage.toFront();
                stage.requestFocus();

                System.err.println(">>> SETUP: Scene carregada com sucesso! Root: " + root);
            } catch (Exception e) {
                System.err.println(">>> ERRO NO SETUP DO STAGE: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Falha ao carregar FXML no teste", e);
            }
        });

        // 4. Espera tudo renderizar antes de liberar para o teste
        WaitForAsyncUtils.waitForFxEvents();
    }

    @AfterEach
    public void tearDown() throws TimeoutException {
        FxToolkit.hideStage();
        WaitForAsyncUtils.waitForFxEvents();
    }

    // NOTA: O método @Start foi removido intencionalmente para evitar conflitos.
}