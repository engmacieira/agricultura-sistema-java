package com.agricultura.sistema.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.application.Platform;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class MainController {

    @FXML
    private StackPane contentArea;

    @Autowired
    private ApplicationContext applicationContext;

    @FXML
    public void initialize() {
        // Load default view (e.g., Produtores)
        onProdutoresClick();
    }

    @FXML
    public void onProdutoresClick() {
        loadView("/fxml/produtor.fxml");
    }

    @FXML
    public void onServicosClick() {
        loadView("/fxml/servico.fxml");
    }

    @FXML
    public void onExecucoesClick() {
        loadView("/fxml/execucao.fxml");
    }

    @FXML
    public void onPagamentosClick() {
        loadView("/fxml/pagamento.fxml");
    }

    @FXML
    public void onSairClick() {
        Platform.exit();
        System.exit(0);
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(applicationContext::getBean);
            Parent view = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (IOException e) {
            e.printStackTrace();
            // Could show an alert here
        }
    }
}
