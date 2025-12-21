package com.agricultura.sistema.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "servicos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Servico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "servico_id")
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(name = "valor_unitario")
    private BigDecimal valorUnitario;
}
