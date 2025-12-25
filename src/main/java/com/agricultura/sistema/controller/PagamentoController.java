package com.agricultura.sistema.controller;

import com.agricultura.sistema.model.Execucao;
import com.agricultura.sistema.model.Pagamento;
import com.agricultura.sistema.service.PagamentoService;
import com.agricultura.sistema.service.ExecucaoService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class PagamentoController {

    private final PagamentoService pagamentoService;
    private final ExecucaoService execucaoService;

    @FXML
    private ComboBox<Execucao> cbExecucao;
    @FXML
    private Label lblSaldo;
    @FXML
    private TextField txtValor;
    @FXML
    private DatePicker dtPagamento;
    @FXML
    private TableView<Pagamento> tbPagamentos;
    @FXML
    private TableColumn<Pagamento, Long> colId;
    @FXML
    private TableColumn<Pagamento, String> colData;
    @FXML
    private TableColumn<Pagamento, String> colValor;

    @FXML
    public void initialize() {
        configurarComboBox();
        configurarTabela();
        cbExecucao.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                atualizarSaldo(newValue);
                atualizarTabela(newValue);
            }
        });
        carregarExecucoes();
    }

    private void configurarComboBox() {
        cbExecucao.setConverter(new StringConverter<Execucao>() {
            @Override
            public String toString(Execucao execucao) {
                if (execucao == null)
                    return "";
                return String.format("%d - %s - %s",
                        execucao.getId(),
                        execucao.getProdutor().getNome(),
                        execucao.getServico().getNome());
            }

            @Override
            public Execucao fromString(String string) {
                return null; // Not needed for ComboBox selection
            }
        });
    }

    private void configurarTabela() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colData.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getDataPagamento().toString()));
        colValor.setCellValueFactory(cellData -> {
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));
            return new SimpleStringProperty(currencyFormat.format(cellData.getValue().getValorPago()));
        });
    }

    private void carregarExecucoes() {
        // Ideally filter only unpaid ones, but fetching all for now as per requirement
        // interpretation
        List<Execucao> execucoes = execucaoService.listarTodos();
        cbExecucao.setItems(FXCollections.observableArrayList(execucoes));
    }

    private void atualizarSaldo(Execucao execucao) {
        List<Pagamento> pagamentos = pagamentoService.listarPorExecucao(execucao.getId());
        BigDecimal totalPago = pagamentos.stream()
                .map(Pagamento::getValorPago)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal saldo = execucao.getValorTotal().subtract(totalPago);

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));
        lblSaldo.setText(currencyFormat.format(saldo));
    }

    private void atualizarTabela(Execucao execucao) {
        List<Pagamento> pagamentos = pagamentoService.listarPorExecucao(execucao.getId());
        tbPagamentos.setItems(FXCollections.observableArrayList(pagamentos));
    }

    @FXML
    public void salvarPagamento() {
        try {
            Execucao selectedExecucao = cbExecucao.getSelectionModel().getSelectedItem();
            if (selectedExecucao == null) {
                mostrarAlerta(Alert.AlertType.WARNING, "Selecione uma execução.");
                return;
            }

            if (txtValor.getText().isEmpty() || dtPagamento.getValue() == null) {
                mostrarAlerta(Alert.AlertType.WARNING, "Preencha todos os campos.");
                return;
            }

            BigDecimal valor = new BigDecimal(txtValor.getText().replace(",", "."));

            Pagamento pagamento = new Pagamento();
            pagamento.setExecucao(selectedExecucao);
            pagamento.setValorPago(valor);
            pagamento.setDataPagamento(dtPagamento.getValue());

            pagamentoService.create(pagamento);

            mostrarAlerta(Alert.AlertType.INFORMATION, "Pagamento realizado com sucesso!");

            // Refresh
            atualizarSaldo(selectedExecucao);
            atualizarTabela(selectedExecucao);
            txtValor.clear();
            dtPagamento.setValue(null);

        } catch (IllegalArgumentException e) {
            mostrarAlerta(Alert.AlertType.ERROR, e.getMessage());
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro ao salvar pagamento: " + e.getMessage());
        }
    }

    private void mostrarAlerta(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Sistema");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
