package com.agricultura.sistema.controller;

import com.agricultura.sistema.service.BackupService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Component
public class AdminController {

    private final BackupService backupService;

    public AdminController(BackupService backupService) {
        this.backupService = backupService;
    }

    @FXML
    public void initialize() {
        // Inicialização se necessário
    }

    @FXML
    public void onFazerBackup() {
        try {
            // Chama o serviço para criar um backup manual agora
            // O serviço já deve ter a lógica de salvar com timestamp na pasta backups/
            backupService.realizarBackupAoIniciar();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Backup");
            alert.setHeaderText("Sucesso");
            alert.setContentText("Backup realizado com sucesso na pasta 'backups/'!");
            alert.showAndWait();
        } catch (Exception e) {
            mostrarErro("Falha ao realizar backup", e.getMessage());
        }
    }

    @FXML
    public void onRestaurarBackup() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar Backup para Restaurar");

        // Tenta abrir direto na pasta de backups do sistema
        File backupDir = new File("backups");
        if (backupDir.exists() && backupDir.isDirectory()) {
            fileChooser.setInitialDirectory(backupDir);
        }

        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos de Banco de Dados", "*.db"));

        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirmar Restauração");
            confirmAlert.setHeaderText("Restaurar backup: " + selectedFile.getName());
            confirmAlert.setContentText(
                    "ATENÇÃO: Isso substituirá todos os dados atuais pelos do backup selecionado.\n\n" +
                            "O sistema precisará ser FECHADO para aplicar a mudança. Deseja continuar?");

            if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                agendarRestauracao(selectedFile);
            }
        }
    }

    private void agendarRestauracao(File backupSelecionado) {
        try {
            // 1. Cria o arquivo de "gatilho" que o AppLauncher vai procurar ao iniciar
            File gatilhoRestore = new File("restaurar.db");

            // 2. Copia o backup escolhido para esse arquivo temporário
            Files.copy(
                    backupSelecionado.toPath(),
                    gatilhoRestore.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);

            // 3. Avisa o usuário e mata o processo
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Restauração Agendada");
            successAlert.setHeaderText("Reinicialização Necessária");
            successAlert.setContentText(
                    "O arquivo de restauração foi preparado com sucesso!\n\n" +
                            "O sistema será fechado agora automaicamente.\n" +
                            "POR FAVOR, ABRA O SISTEMA NOVAMENTE para concluir a restauração dos dados.");
            successAlert.showAndWait();

            // Fecha a aplicação JavaFX e mata a JVM para liberar o arquivo do banco
            Platform.exit();
            System.exit(0);

        } catch (IOException e) {
            mostrarErro("Erro ao agendar restauração", e.getMessage());
            e.printStackTrace();
        }
    }

    private void mostrarErro(String titulo, String mensagem) {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setTitle("Erro");
        errorAlert.setHeaderText(titulo);
        errorAlert.setContentText(mensagem);
        errorAlert.showAndWait();
    }
}