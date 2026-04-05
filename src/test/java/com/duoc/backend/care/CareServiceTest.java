package com.duoc.backend.care;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CareServiceTest {

    @Mock
    private CareRepository careRepository;

    @InjectMocks
    private CareService careService;

    @Test
    void crudFlow() {
        Care c = new Care();
        c.setId(1L);
        when(careRepository.findAll()).thenReturn(List.of(c));
        assertThat(careService.getAllCares()).hasSize(1);
        when(careRepository.findById(1L)).thenReturn(Optional.of(c));
        assertThat(careService.getCareById(1L)).isSameAs(c);
        when(careRepository.save(c)).thenReturn(c);
        assertThat(careService.saveCare(c)).isSameAs(c);
        careService.deleteCare(1L);
        verify(careRepository).deleteById(1L);
    }
}
