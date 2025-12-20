package com.agricultura.sistema.dto;

import java.math.BigDecimal;

public class ResumoFinanceiroDTO {

    private String nomeProdutor;
    private BigDecimal valorTotalDivida;
    private BigDecimal valorTotalPago;

    public ResumoFinanceiroDTO(String nomeProdutor, BigDecimal valorTotalDivida, BigDecimal valorTotalPago) {
        this.nomeProdutor = nomeProdutor;
        this.valorTotalDivida = valorTotalDivida;
        this.valorTotalPago = valorTotalPago;
    }

    public String getNomeProdutor() {
        return nomeProdutor;
    }

    public void setNomeProdutor(String nomeProdutor) {
        this.nomeProdutor = nomeProdutor;
    }

    public BigDecimal getValorTotalDivida() {
        return valorTotalDivida;
    }

    public void setValorTotalDivida(BigDecimal valorTotalDivida) {
        this.valorTotalDivida = valorTotalDivida;
    }

    public BigDecimal getValorTotalPago() {
        return valorTotalPago;
    }

    public void setValorTotalPago(BigDecimal valorTotalPago) {
        this.valorTotalPago = valorTotalPago;
    }
}
