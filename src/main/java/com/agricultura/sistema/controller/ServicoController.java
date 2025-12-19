package com.agricultura.sistema.controller;

import com.agricultura.sistema.model.Servico;
import com.agricultura.sistema.service.ServicoService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class ServicoController {

    @Autowired
    private ServicoService servicoService;

    @FXML
    private TextField txtNome;
    @FXML
    private TextField txtValor;

    @FXML
    private TableView<Servico> tabelaServicos;

    @FXML
    private TableColumn<Servico, Long> colId;
    @FXML
    private TableColumn<Servico, String> colNome;
    @FXML
    private TableColumn<Servico, BigDecimal> colValor;

    private ObservableList<Servico> listaServicos = FXCollections.observableArrayList();
    private Servico servicoAtual;

    @FXML
    public void initialize() {
        carregarServicos();

        tabelaServicos.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // Optional: preencherFormulario(newSelection);
            }
        });
    }

    private void carregarServicos() {
        listaServicos.clear();
        listaServicos.addAll(servicoService.listarTodos());
        tabelaServicos.setItems(listaServicos);
    }

    @FXML
    public void onSalvar() {
        if (servicoAtual == null) {
            servicoAtual = new Servico();
        }

        try {
            servicoAtual.setNome(txtNome.getText());
            servicoAtual.setValorUnitario(new BigDecimal(txtValor.getText().replace(",", ".")));

            if (servicoAtual.getId() == null) {
                servicoService.create(servicoAtual);
            } else {
                servicoService.update(servicoAtual.getId(), servicoAtual);
            }
            mostrarAlerta("Sucesso", "Serviço salvo com sucesso!", Alert.AlertType.INFORMATION);
            limparFormulario();
            carregarServicos();
        } catch (NumberFormatException e) {
            mostrarAlerta("Erro", "Valor inválido. Use ponto ou vírgula.", Alert.AlertType.ERROR);
        } catch (Exception e) {
            mostrarAlerta("Erro", "Erro ao salvar serviço: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void onEditar() {
        Servico selecionado = tabelaServicos.getSelectionModel().getSelectedItem();
        if (selecionado != null) {
            preencherFormulario(selecionado);
        } else {
            mostrarAlerta("Aviso", "Selecione um serviço para editar.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    public void onExcluir() {
        Servico selecionado = tabelaServicos.getSelectionModel().getSelectedItem();
        if (selecionado != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmação de Exclusão");
            alert.setHeaderText("Excluir Serviço");
            alert.setContentText("Tem certeza que deseja excluir o serviço " + selecionado.getNome() + "?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    servicoService.delete(selecionado.getId());
                    carregarServicos();
                    limparFormularioIfSelected(selecionado);
                    mostrarAlerta("Sucesso", "Serviço excluído com sucesso!", Alert.AlertType.INFORMATION);
                } catch (Exception e) {
                    mostrarAlerta("Erro", "Erro ao excluir serviço: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        } else {
            mostrarAlerta("Aviso", "Selecione um serviço para excluir.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    public void onLimpar() {
        limparFormulario();
    }

    private void preencherFormulario(Servico servico) {
        servicoAtual = servico;
        txtNome.setText(servico.getNome());
        txtValor.setText(servico.getValorUnitario().toString());
    }

    private void limparFormulario() {
        servicoAtual = null;
        txtNome.clear();
        txtValor.clear();
    }

    private void limparFormularioIfSelected(Servico servicoExcluido) {
        if (servicoAtual != null && servicoAtual.getId().equals(servicoExcluido.getId())) {
            limparFormulario();
        }
    }

    private void mostrarAlerta(String titulo, String conteudo, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(conteudo);
        alert.showAndWait();
    }
}
