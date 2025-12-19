package com.agricultura.sistema.controller;

import com.agricultura.sistema.model.Produtor;
import com.agricultura.sistema.service.ProdutorService;
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

import java.util.Optional;

@Component
public class ProdutorController {

    @Autowired
    private ProdutorService produtorService;

    @FXML
    private TextField txtNome;
    @FXML
    private TextField txtApelido;
    @FXML
    private TextField txtCpf;
    @FXML
    private TextField txtRegiao;
    @FXML
    private TextField txtReferencia;
    @FXML
    private TextField txtTelefone;

    @FXML
    private TableView<Produtor> tabelaProdutores;

    @FXML
    private TableColumn<Produtor, Long> colId;
    @FXML
    private TableColumn<Produtor, String> colNome;
    @FXML
    private TableColumn<Produtor, String> colApelido;
    @FXML
    private TableColumn<Produtor, String> colCpf;
    @FXML
    private TableColumn<Produtor, String> colRegiao;
    @FXML
    private TableColumn<Produtor, String> colTelefone;

    private ObservableList<Produtor> listaProdutores = FXCollections.observableArrayList();

    private Produtor produtorAtual;

    @FXML
    public void initialize() {
        carregarProdutores();

        // Listener para seleção na tabela (opcional, mas bom pra UX)
        tabelaProdutores.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // Pode preencher o form automaticamente ao selecionar, se desejar
                // preencherFormulario(newSelection);
            }
        });
    }

    private void carregarProdutores() {
        listaProdutores.clear();
        listaProdutores.addAll(produtorService.listarTodos());
        tabelaProdutores.setItems(listaProdutores);
    }

    @FXML
    public void onSalvar() {
        if (produtorAtual == null) {
            produtorAtual = new Produtor();
        }

        produtorAtual.setNome(txtNome.getText());
        produtorAtual.setApelido(txtApelido.getText());
        produtorAtual.setCpf(txtCpf.getText());
        produtorAtual.setRegiao(txtRegiao.getText());
        produtorAtual.setReferencia(txtReferencia.getText());
        produtorAtual.setTelefone(txtTelefone.getText());

        try {
            if (produtorAtual.getId() == null) {
                produtorService.create(produtorAtual);
            } else {
                produtorService.update(produtorAtual.getId(), produtorAtual);
            }
            mostrarAlerta("Sucesso", "Produtor salvo com sucesso!", Alert.AlertType.INFORMATION);
            limparFormulario();
            carregarProdutores();
        } catch (Exception e) {
            mostrarAlerta("Erro", "Erro ao salvar produtor: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void onEditar() {
        Produtor selecionado = tabelaProdutores.getSelectionModel().getSelectedItem();
        if (selecionado != null) {
            preencherFormulario(selecionado);
        } else {
            mostrarAlerta("Aviso", "Selecione um produtor para editar.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    public void onExcluir() {
        Produtor selecionado = tabelaProdutores.getSelectionModel().getSelectedItem();
        if (selecionado != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmação de Exclusão");
            alert.setHeaderText("Excluir Produtor");
            alert.setContentText("Tem certeza que deseja excluir o produtor " + selecionado.getNome() + "?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    produtorService.delete(selecionado.getId());
                    carregarProdutores();
                    limparFormularioIfSelected(selecionado);
                    mostrarAlerta("Sucesso", "Produtor excluído com sucesso!", Alert.AlertType.INFORMATION);
                } catch (Exception e) {
                    mostrarAlerta("Erro", "Erro ao excluir produtor: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        } else {
            mostrarAlerta("Aviso", "Selecione um produtor para excluir.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    public void onLimpar() {
        limparFormulario();
    }

    private void preencherFormulario(Produtor produtor) {
        produtorAtual = produtor;
        txtNome.setText(produtor.getNome());
        txtApelido.setText(produtor.getApelido());
        txtCpf.setText(produtor.getCpf());
        txtRegiao.setText(produtor.getRegiao());
        txtReferencia.setText(produtor.getReferencia());
        txtTelefone.setText(produtor.getTelefone());
    }

    private void limparFormulario() {
        produtorAtual = null;
        txtNome.clear();
        txtApelido.clear();
        txtCpf.clear();
        txtRegiao.clear();
        txtReferencia.clear();
        txtTelefone.clear();
    }

    private void limparFormularioIfSelected(Produtor produtorExcluido) {
        if (produtorAtual != null && produtorAtual.getId().equals(produtorExcluido.getId())) {
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
