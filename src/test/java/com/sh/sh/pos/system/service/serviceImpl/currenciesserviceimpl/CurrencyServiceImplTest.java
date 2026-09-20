package com.sh.sh.pos.system.service.serviceimpl.currenciesserviceimpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sh.sh.pos.system.exception.ResourceNotFoundException;
import com.sh.sh.pos.system.exception.UserException;
import com.sh.sh.pos.system.mapper.currenciesmapper.CurrencyMapper;
import com.sh.sh.pos.system.model.currencies.Currency;
import com.sh.sh.pos.system.payload.dto.currenciesdto.CurrencyDTO;
import com.sh.sh.pos.system.payload.request.CurrencyRequestDTO;
import com.sh.sh.pos.system.repository.currenciesrepository.CurrencyRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("CurrencyServiceImpl — code normalization, uniqueness, CRUD")
class CurrencyServiceImplTest {

    @Mock private CurrencyRepository repo;
    @Mock private CurrencyMapper mapper;

    @InjectMocks
    private CurrencyServiceImpl currencyService;

    private Currency usd;
    private CurrencyDTO usdDTO;

    @BeforeEach
    void setUp() {
        usd = new Currency();
        usd.setId(1L);
        usd.setCode("USD");
        usd.setName("US Dollar");
        usd.setSymbol("$");
        usd.setActive(true);

        usdDTO = new CurrencyDTO();
        usdDTO.setId(1L);
        usdDTO.setCode("USD");
    }

    private CurrencyRequestDTO request(String code) {
        CurrencyRequestDTO req = new CurrencyRequestDTO();
        req.setCode(code);
        req.setName("US Dollar");
        req.setSymbol("$");
        return req;
    }

    // ── create ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("create → code normalized to uppercase/trimmed before duplicate check ✅")
    void create_normalizesCode() {
        when(repo.existsByCode("USD")).thenReturn(false);
        when(mapper.toEntity(any(CurrencyRequestDTO.class))).thenReturn(usd);
        when(repo.save(usd)).thenReturn(usd);
        when(mapper.toDTO(usd)).thenReturn(usdDTO);

        CurrencyDTO result = currencyService.create(request("  usd  "));

        assertThat(result).isNotNull();
        // the duplicate check must have used the NORMALIZED code
        verify(repo).existsByCode("USD");
    }

    @Test
    @DisplayName("create → duplicate code → UserException, nothing saved ❌")
    void create_duplicate_rejected() {
        when(repo.existsByCode("USD")).thenReturn(true);

        assertThatThrownBy(() -> currencyService.create(request("usd")))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("already exists");

        verify(repo, never()).save(any());
    }

    // ── update ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("update → keeping own code → duplicate check skipped, saved ✅")
    void update_sameCode_allowed() {
        when(repo.findById(1L)).thenReturn(Optional.of(usd));
        when(repo.save(usd)).thenReturn(usd);
        when(mapper.toDTO(usd)).thenReturn(usdDTO);

        currencyService.update(1L, request("usd"));  // normalizes to USD == own code

        verify(repo, never()).existsByCode(any());
        verify(mapper).updateEntity(eq(usd), any(CurrencyRequestDTO.class));
    }

    @Test
    @DisplayName("update → changing to a taken code → UserException ❌")
    void update_codeConflict_rejected() {
        when(repo.findById(1L)).thenReturn(Optional.of(usd));
        when(repo.existsByCode("KHR")).thenReturn(true);

        assertThatThrownBy(() -> currencyService.update(1L, request("khr")))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("already exists");

        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("update → unknown id → 404 ❌")
    void update_notFound() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> currencyService.update(99L, request("USD")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Currency not found");
    }

    // ── reads ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getByCode → lowercase input still finds uppercase code ✅")
    void getByCode_caseInsensitive() {
        when(repo.findByCode("USD")).thenReturn(Optional.of(usd));
        when(mapper.toDTO(usd)).thenReturn(usdDTO);

        assertThat(currencyService.getByCode("usd")).isNotNull();
    }

    @Test
    @DisplayName("getByCode → unknown code → 404 ❌")
    void getByCode_notFound() {
        when(repo.findByCode("XXX")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> currencyService.getByCode("xxx"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getActive → maps active currencies only ✅")
    void getActive_maps() {
        when(repo.findByActiveTrue()).thenReturn(List.of(usd));
        when(mapper.toDTO(usd)).thenReturn(usdDTO);

        assertThat(currencyService.getActive()).hasSize(1);
    }

    // ── setActive / delete ────────────────────────────────────────────────

    @Test
    @DisplayName("setActive(false) → deactivates and saves ✅")
    void setActive_deactivates() {
        when(repo.findById(1L)).thenReturn(Optional.of(usd));
        when(repo.save(usd)).thenReturn(usd);
        when(mapper.toDTO(usd)).thenReturn(usdDTO);

        currencyService.setActive(1L, false);

        assertThat(usd.getActive()).isFalse();
        verify(repo).save(usd);
    }

    @Test
    @DisplayName("delete → deletes the loaded entity ✅")
    void delete_found() {
        when(repo.findById(1L)).thenReturn(Optional.of(usd));

        currencyService.delete(1L);

        verify(repo).delete(usd);
    }
}