package com.agricultura.sistema.service;

import com.agricultura.sistema.exception.ResourceNotFoundException;
import com.agricultura.sistema.model.Produtor;
import com.agricultura.sistema.repository.ProdutorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null") // Silencia alertas de null-safety do Mockito
class ProdutorServiceTest {

    @InjectMocks
    private ProdutorService produtorService;

    @Mock
    private ProdutorRepository produtorRepository;

    private Produtor produtorPadrao;

    @BeforeEach
    void setup() {
        produtorPadrao = new Produtor();
        produtorPadrao.setId(1L);
        produtorPadrao.setNome("João da Silva");
        produtorPadrao.setCpf("123.456.789-00");
        produtorPadrao.setRegiao("Norte");
    }

    // --- TESTES DE CRIAÇÃO (CREATE) ---

    @Test
    @DisplayName("Deve criar produtor com sucesso quando dados são válidos")
    void testCreateSuccess() {
        when(produtorRepository.save(any(Produtor.class))).thenReturn(produtorPadrao);

        Produtor criado = produtorService.create(produtorPadrao);

        assertNotNull(criado);
        assertEquals("João da Silva", criado.getNome());
        verify(produtorRepository, times(1)).save(produtorPadrao);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar produtor sem nome")
    void testCreateSemNome() {
        produtorPadrao.setNome(null); // Cenário inválido

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            produtorService.create(produtorPadrao);
        });

        assertEquals("O campo 'nome' é obrigatório", exception.getMessage());
        verify(produtorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve verificar duplicidade de CPF ao criar")
    void testCreateVerificaCpf() {
        // Simula que já existe alguém com esse CPF
        when(produtorRepository.findByCpf(produtorPadrao.getCpf())).thenReturn(Optional.of(new Produtor()));
        when(produtorRepository.save(any(Produtor.class))).thenReturn(produtorPadrao);

        // O serviço atual apenas loga um aviso (logger.warn), não lança exceção.
        // O teste garante que o método findByCpf foi chamado e o save prosseguiu.
        assertDoesNotThrow(() -> produtorService.create(produtorPadrao));

        verify(produtorRepository, times(1)).findByCpf(produtorPadrao.getCpf());
        verify(produtorRepository, times(1)).save(any());
    }

    // --- TESTES DE BUSCA (FIND) ---

    @Test
    @DisplayName("Deve buscar produtor por ID existente")
    void testFindByIdSuccess() {
        when(produtorRepository.findById(1L)).thenReturn(Optional.of(produtorPadrao));

        Produtor encontrado = produtorService.findById(1L);

        assertEquals(1L, encontrado.getId());
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar ID inexistente")
    void testFindByIdNotFound() {
        when(produtorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            produtorService.findById(99L);
        });
    }

    @Test
    @DisplayName("Deve listar todos os produtores")
    void testListarTodos() {
        when(produtorRepository.findAll()).thenReturn(List.of(produtorPadrao));

        List<Produtor> lista = produtorService.listarTodos();

        assertFalse(lista.isEmpty());
        assertEquals(1, lista.size());
    }

    @Test
    @DisplayName("Deve buscar paginado com termo de busca")
    void testFindAllComBusca() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Produtor> page = new PageImpl<>(Collections.singletonList(produtorPadrao));
        String termo = "João";

        when(produtorRepository.findByNomeContainingIgnoreCaseOrApelidoContainingIgnoreCaseOrCpfContaining(
                eq(termo), eq(termo), eq(termo), any(Pageable.class)))
                .thenReturn(page);

        Page<Produtor> resultado = produtorService.findAll(pageable, termo);

        assertEquals(1, resultado.getTotalElements());
        verify(produtorRepository, never()).findAll(any(Pageable.class)); // Garante que usou a busca específica
    }

    // --- TESTES DE ATUALIZAÇÃO (UPDATE) ---

    @Test
    @DisplayName("Deve atualizar produtor com sucesso")
    void testUpdateSuccess() {
        Produtor dadosNovos = new Produtor();
        dadosNovos.setNome("João Atualizado");
        dadosNovos.setApelido("Jota");
        dadosNovos.setCpf("111.111.111-11"); // Novo CPF

        when(produtorRepository.findById(1L)).thenReturn(Optional.of(produtorPadrao));
        when(produtorRepository.save(any(Produtor.class))).thenReturn(produtorPadrao); // O retorno do save geralmente é
                                                                                       // o obj salvo

        Produtor atualizado = produtorService.update(1L, dadosNovos);

        // Verifica se os campos do objeto recuperado foram alterados antes de salvar
        assertEquals("João Atualizado", produtorPadrao.getNome());
        assertEquals("Jota", produtorPadrao.getApelido());
        assertEquals("111.111.111-11", produtorPadrao.getCpf());

        verify(produtorRepository, times(1)).save(produtorPadrao);
    }

    // --- TESTES DE DELEÇÃO (DELETE) ---

    @Test
    @DisplayName("Deve deletar produtor existente")
    void testDeleteSuccess() {
        when(produtorRepository.existsById(1L)).thenReturn(true);
        doNothing().when(produtorRepository).deleteById(1L);

        produtorService.delete(1L);

        verify(produtorRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar deletar produtor inexistente")
    void testDeleteNotFound() {
        when(produtorRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            produtorService.delete(99L);
        });

        verify(produtorRepository, never()).deleteById(any());
    }
}