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
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.TimeoutException;

@ExtendWith(ApplicationExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public abstract class IntegrationTestBase {

    @Autowired
    protected ApplicationContext applicationContext;

    @BeforeAll
    public static void setupSpec() throws Exception {
        // 1. Configurações de Sistema (Headless & Renderização)
        System.setProperty("java.awt.headless", "false");
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "false");
        System.setProperty("prism.order", "sw"); // Renderização via Software

        // 2. Inicializa o Toolkit e PREVINE que ele feche sozinho
        try {
            FxToolkit.registerPrimaryStage();
            Platform.setImplicitExit(false); // <--- O PULO DO GATO PARA EVITAR 0 JANELAS
        } catch (Exception e) {
            // Ignora se já estiver registrado
        }
    }

    @BeforeEach
    public void setup() throws Exception {
        // 3. Força o Stage a "acordar" antes de cada teste
        // Isso garante que o robô tenha uma janela para olhar, mesmo que vazia
        // inicialmente
        FxToolkit.setupStage(stage -> {
            stage.show();
            stage.toFront();
        });
        WaitForAsyncUtils.waitForFxEvents(); // Espera a janela subir
    }

    @AfterEach
    public void tearDown() throws TimeoutException {
        // Limpeza segura
        FxToolkit.hideStage();
        // Não fechamos o Toolkit para não quebrar os próximos testes da bateria
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Start
    public void start(Stage stage) throws Exception {
        try {
            System.err.println(">>> START INICIADO. Preparando Scene...");

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            fxmlLoader.setControllerFactory(applicationContext::getBean);

            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root, 1200, 800);

            // Anexa a scene ao stage que o FxToolkit preparou no @BeforeEach
            stage.setScene(scene);
            stage.setTitle("Sistema Agricultura - Teste E2E");
            stage.toFront();
            stage.requestFocus();

            // Espera CRÍTICA para renderização
            WaitForAsyncUtils.waitForFxEvents();

            System.err.println(">>> START CONCLUÍDO. Scene definida? " + (stage.getScene() != null));

        } catch (Exception e) {
            System.err.println(">>> ERRO FATAL NO START: " + e.getMessage());
            e.printStackTrace();
            throw e; // Lança para falhar o teste explicitamente
        }
    }
}