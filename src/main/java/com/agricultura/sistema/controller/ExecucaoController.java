package com.agricultura.sistema.controller;

import com.agricultura.sistema.model.Execucao;
import com.agricultura.sistema.model.Produtor;
import com.agricultura.sistema.model.Servico;
import com.agricultura.sistema.service.ExecucaoService;
import com.agricultura.sistema.service.ProdutorService;
import com.agricultura.sistema.service.ServicoService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Component
public class ExecucaoController {

    @Autowired
    private ExecucaoService execucaoService;

    @Autowired
    private ProdutorService produtorService;

    @Autowired
    private ServicoService servicoService;

    @FXML
    private ComboBox<Produtor> cmbProdutor;
    @FXML
    private ComboBox<Servico> cmbServico;
    @FXML
    private DatePicker dtDataExecucao;
    @FXML
    private TextField txtHoras;
    @FXML
    private TextField txtValorTotal;

    @FXML
    private TableView<Execucao> tabelaExecucoes;

    @FXML
    private TableColumn<Execucao, Long> colId;
    @FXML
    private TableColumn<Execucao, String> colProdutor;
    @FXML
    private TableColumn<Execucao, String> colServico;
    @FXML
    private TableColumn<Execucao, String> colData;
    @FXML
    private TableColumn<Execucao, BigDecimal> colHoras;
    @FXML
    private TableColumn<Execucao, BigDecimal> colTotal;

    private ObservableList<Execucao> listaExecucoes = FXCollections.observableArrayList();
    private Execucao execucaoAtual;

    @FXML
    public void initialize() {
        configurarComboBoxes();
        configurarTabela();
        carregarExecucoes();

        // Auto-calculate logic (optional visual enhancement)
        txtHoras.textProperty().addListener((obs, oldVal, newVal) -> calcularPreviaTotal());
        cmbServico.valueProperty().addListener((obs, oldVal, newVal) -> calcularPreviaTotal());
    }

    private void configurarComboBoxes() {
        cmbProdutor.setItems(FXCollections.observableArrayList(produtorService.listarTodos()));
        cmbProdutor.setConverter(new StringConverter<Produtor>() {
            @Override
            public String toString(Produtor produtor) {
                return produtor != null ? produtor.getNome() : "";
            }

            @Override
            public Produtor fromString(String s) {
                return null; // Not needed for selection-only combo
            }
        });

        cmbServico.setItems(FXCollections.observableArrayList(servicoService.listarTodos()));
        cmbServico.setConverter(new StringConverter<Servico>() {
            @Override
            public String toString(Servico servico) {
                return servico != null ? servico.getNome() + " (R$ " + servico.getValorUnitario() + "/h)" : "";
            }

            @Override
            public Servico fromString(String s) {
                return null;
            }
        });
    }

    private void configurarTabela() {
        colProdutor
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProdutor().getNome()));
        colServico
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getServico().getNome()));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        colData.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getDataExecucao().format(formatter)));

        colHoras.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("horasPrestadas"));
        colTotal.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("valorTotal"));
    }

    private void carregarExecucoes() {
        listaExecucoes.clear();
        listaExecucoes.addAll(execucaoService.listarTodos());
        tabelaExecucoes.setItems(listaExecucoes);
    }

    private void calcularPreviaTotal() {
        try {
            if (cmbServico.getValue() != null && !txtHoras.getText().isEmpty()) {
                BigDecimal horas = new BigDecimal(txtHoras.getText().replace(",", "."));
                BigDecimal total = cmbServico.getValue().getValorUnitario().multiply(horas);
                txtValorTotal.setText(String.format("R$ %.2f", total));
            } else {
                txtValorTotal.clear();
            }
        } catch (Exception e) {
            txtValorTotal.setText("Erro");
        }
    }

    @FXML
    public void onSalvar() {
        if (execucaoAtual == null) {
            execucaoAtual = new Execucao();
        }

        try {
            execucaoAtual.setProdutor(cmbProdutor.getValue());
            execucaoAtual.setServico(cmbServico.getValue());
            execucaoAtual.setDataExecucao(dtDataExecucao.getValue());
            execucaoAtual.setHorasPrestadas(new BigDecimal(txtHoras.getText().replace(",", ".")));

            if (execucaoAtual.getProdutor() == null || execucaoAtual.getServico() == null
                    || execucaoAtual.getDataExecucao() == null) {
                mostrarAlerta("Erro", "Preencha todos os campos obrigatórios.", Alert.AlertType.ERROR);
                return;
            }

            if (execucaoAtual.getId() == null) {
                execucaoService.create(execucaoAtual);
            } else {
                execucaoService.update(execucaoAtual.getId(), execucaoAtual);
            }
            mostrarAlerta("Sucesso", "Execução salva com sucesso!", Alert.AlertType.INFORMATION);
            limparFormulario();
            carregarExecucoes();
        } catch (NumberFormatException e) {
            mostrarAlerta("Erro", "Valor de horas inválido.", Alert.AlertType.ERROR);
        } catch (Exception e) {
            mostrarAlerta("Erro", "Erro ao salvar execução: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void onEditar() {
        Execucao selecionada = tabelaExecucoes.getSelectionModel().getSelectedItem();
        if (selecionada != null) {
            preencherFormulario(selecionada);
        } else {
            mostrarAlerta("Aviso", "Selecione uma execução para editar.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    public void onExcluir() {
        Execucao selecionada = tabelaExecucoes.getSelectionModel().getSelectedItem();
        if (selecionada != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmação de Exclusão");
            alert.setHeaderText("Excluir Execução");
            alert.setContentText("Tem certeza que deseja excluir esta execução?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    execucaoService.delete(selecionada.getId());
                    carregarExecucoes();
                    limparFormularioIfSelected(selecionada);
                    mostrarAlerta("Sucesso", "Execução excluída com sucesso!", Alert.AlertType.INFORMATION);
                } catch (Exception e) {
                    mostrarAlerta("Erro", "Erro ao excluir execução: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        } else {
            mostrarAlerta("Aviso", "Selecione uma execução para excluir.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    public void onLimpar() {
        limparFormulario();
    }

    private void preencherFormulario(Execucao execucao) {
        execucaoAtual = execucao;

        // Populate fields
        // For ComboBoxes, we need to find the matching item in the list
        cmbProdutor.getItems().stream()
                .filter(p -> p.getId().equals(execucao.getProdutor().getId()))
                .findFirst()
                .ifPresent(cmbProdutor::setValue);

        cmbServico.getItems().stream()
                .filter(s -> s.getId().equals(execucao.getServico().getId()))
                .findFirst()
                .ifPresent(cmbServico::setValue);

        dtDataExecucao.setValue(execucao.getDataExecucao());
        txtHoras.setText(execucao.getHorasPrestadas().toString());
        calcularPreviaTotal();
    }

    private void limparFormulario() {
        execucaoAtual = null;
        cmbProdutor.setValue(null);
        cmbServico.setValue(null);
        dtDataExecucao.setValue(null);
        txtHoras.clear();
        txtValorTotal.clear();
    }

    private void limparFormularioIfSelected(Execucao execucaoExcluida) {
        if (execucaoAtual != null && execucaoAtual.getId().equals(execucaoExcluida.getId())) {
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
