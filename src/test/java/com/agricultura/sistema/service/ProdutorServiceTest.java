package com.agricultura.sistema.service;

import com.agricultura.sistema.exception.ResourceNotFoundException;
import com.agricultura.sistema.model.Produtor;
import com.agricultura.sistema.repository.ProdutorRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
public class ProdutorServiceTest {

    @Mock
    private ProdutorRepository produtorRepository;

    @InjectMocks
    private ProdutorService produtorService;

    @Test
    public void testCreateProdutorSuccess() {
        Produtor produtor = Produtor.builder().nome("João Silva").cpf("12345678900").build();
        Mockito.when(produtorRepository.save(Mockito.any(Produtor.class))).thenReturn(produtor);

        Produtor created = produtorService.create(produtor);

        Assertions.assertNotNull(created);
        Assertions.assertEquals("João Silva", created.getNome());
        Mockito.verify(produtorRepository, Mockito.times(1)).save(produtor);
    }

    @Test
    public void testCreateProdutorValidationError() {
        Produtor produtor = Produtor.builder().build(); // Sem nome

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            produtorService.create(produtor);
        });

        Assertions.assertEquals("O campo 'nome' é obrigatório", exception.getMessage());
        Mockito.verify(produtorRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void testFindByIdSuccess() {
        Produtor produtor = Produtor.builder().id(1L).nome("Maria").build();
        Mockito.when(produtorRepository.findById(1L)).thenReturn(Optional.of(produtor));

        Produtor found = produtorService.findById(1L);

        Assertions.assertEquals("Maria", found.getNome());
    }

    @Test
    public void testFindByIdNotFound() {
        Mockito.when(produtorRepository.findById(1L)).thenReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            produtorService.findById(1L);
        });
    }

    @Test
    public void testSoftDeleteSuccess() {
        Mockito.when(produtorRepository.existsById(1L)).thenReturn(true);
        Mockito.doNothing().when(produtorRepository).deleteById(1L);

        produtorService.delete(1L);

        Mockito.verify(produtorRepository, Mockito.times(1)).deleteById(1L);
    }
}
