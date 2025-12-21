package com.agricultura.sistema;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

@ExtendWith(ApplicationExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public abstract class IntegrationTestBase {

    @Autowired
    protected ApplicationContext applicationContext;

    @BeforeAll
    public static void setupSpec() throws Exception {
        // Força a JVM a não entrar em modo headless antes de qualquer teste
        System.setProperty("java.awt.headless", "false");
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "false");

        // Registra o Stage principal manualmente para evitar que o TestFX se perca
        FxToolkit.registerPrimaryStage();
    }

    @Start
    public void start(Stage stage) throws Exception {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            // Isso garante que o Spring injete os Controllers corretamente
            fxmlLoader.setControllerFactory(applicationContext::getBean);

            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root, 1200, 800);
            stage.setScene(scene);
            stage.show();
            stage.toFront();

            System.err.println(">>> JANELA CARREGADA COM SUCESSO! <<<");
        } catch (Exception e) {
            System.err.println(">>> ERRO AO CARREGAR FXML: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}