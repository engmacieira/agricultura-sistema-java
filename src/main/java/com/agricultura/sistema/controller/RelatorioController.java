package com.agricultura.sistema.controller;

import com.agricultura.sistema.dto.ResumoFinanceiroDTO;
import com.agricultura.sistema.service.RelatorioService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@Controller
public class RelatorioController {

    @Autowired
    private RelatorioService relatorioService;

    @FXML
    private TableView<ResumoFinanceiroDTO> tabelaDevedores;

    @FXML
    private TableColumn<ResumoFinanceiroDTO, String> colunaNome;

    @FXML
    private TableColumn<ResumoFinanceiroDTO, BigDecimal> colunaValor;

    @FXML
    private Label lblTotalGeral;

    @FXML
    private PieChart graficoPizza;

    @FXML
    public void initialize() {
        configurarTabela();
        carregarDados();
    }

    private void configurarTabela() {
        colunaNome.setCellValueFactory(new PropertyValueFactory<>("nomeProdutor"));
        colunaValor.setCellValueFactory(new PropertyValueFactory<>("valorTotalDivida"));

        // Format currency
        colunaValor.setCellFactory(tc -> new TableCell<ResumoFinanceiroDTO, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(NumberFormat.getCurrencyInstance(Locale.of("pt", "BR")).format(item));
                }
            }
        });
    }

    private void carregarDados() {
        List<ResumoFinanceiroDTO> dados = relatorioService.gerarRelatorioDevedores();
        ObservableList<ResumoFinanceiroDTO> observableList = FXCollections.observableArrayList(dados);

        tabelaDevedores.setItems(observableList);

        // Calculate Total General
        BigDecimal totalGeral = dados.stream()
                .map(ResumoFinanceiroDTO::getValorTotalDivida)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        lblTotalGeral.setText("Total Geral a Receber: "
                + NumberFormat.getCurrencyInstance(Locale.of("pt", "BR")).format(totalGeral));

        // Populate Chart
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (ResumoFinanceiroDTO dto : dados) {
            pieChartData.add(new PieChart.Data(dto.getNomeProdutor(), dto.getValorTotalDivida().doubleValue()));
        }
        graficoPizza.setData(pieChartData);
    }
}
