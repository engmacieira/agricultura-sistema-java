package com.agricultura.sistema.model;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete; // Import necessário
import org.hibernate.annotations.SQLRestriction; // Import necessário (antigo @Where)
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime; // Import necessário

@Entity
@Table(name = "execucoes")
// Adicionamos o comando nativo para atualizar em vez de deletar
@SQLDelete(sql = "UPDATE execucoes SET deletado_em = CURRENT_TIMESTAMP WHERE execucao_id = ?")
// Adicionamos o filtro global para ignorar os deletados nas buscas
@SQLRestriction("deletado_em IS NULL")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Execucao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "execucao_id") // Isso já estava correto, mapeando para o ID antigo
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produtor_id", nullable = false)
    private Produtor produtor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servico_id", nullable = false)
    private Servico servico;

    @Column(name = "data_execucao", nullable = false)
    private LocalDate dataExecucao;

    @Column(name = "horas_prestadas")
    private BigDecimal horasPrestadas;

    @Column(name = "valor_total")
    private BigDecimal valorTotal;

    @OneToMany(mappedBy = "execucao", fetch = FetchType.LAZY)
    @lombok.ToString.Exclude
    @lombok.EqualsAndHashCode.Exclude
    private java.util.List<Pagamento> pagamentos;

    // === NOVO CAMPO OBRIGATÓRIO PARA O SOFT DELETE ===
    @Column(name = "deletado_em")
    private LocalDateTime deletadoEm;
}