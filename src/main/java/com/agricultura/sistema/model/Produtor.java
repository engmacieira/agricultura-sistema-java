package com.agricultura.sistema.model;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "produtores")
@SQLDelete(sql = "UPDATE produtores SET deletado_em = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deletado_em IS NULL")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Produtor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    private String apelido;

    private String cpf;

    private String regiao;

    private String referencia;

    private String telefone;

    @OneToMany(mappedBy = "produtor", fetch = FetchType.LAZY)
    @lombok.ToString.Exclude
    @lombok.EqualsAndHashCode.Exclude
    private java.util.List<Execucao> execucoes;

    @Column(name = "deletado_em")
    private java.time.LocalDateTime deletadoEm;
}
