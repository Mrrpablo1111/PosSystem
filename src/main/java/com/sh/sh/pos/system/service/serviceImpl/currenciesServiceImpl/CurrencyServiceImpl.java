package com.sh.sh.pos.system.service.serviceimpl.currenciesserviceimpl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sh.sh.pos.system.exception.ResourceNotFoundException;
import com.sh.sh.pos.system.exception.UserException;
import com.sh.sh.pos.system.mapper.currenciesmapper.CurrencyMapper;
import com.sh.sh.pos.system.model.currencies.Currency;
import com.sh.sh.pos.system.payload.dto.currenciesdto.CurrencyDTO;
import com.sh.sh.pos.system.payload.request.CurrencyRequestDTO;
import com.sh.sh.pos.system.repository.currenciesrepository.CurrencyRepository;
import com.sh.sh.pos.system.service.currenciesservice.CurrencyService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CurrencyServiceImpl implements CurrencyService {

    private static final String CODE_EXISTS = "Currency code already exists: ";

    private final CurrencyRepository repo;
    private final CurrencyMapper     mapper;

    // ── Read ──────────────────────────────────────────────────────────────

    @Override
    public List<CurrencyDTO> getAll() {
        return repo.findAll().stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public List<CurrencyDTO> getActive() {
        return repo.findByActiveTrue().stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public CurrencyDTO getById(Long id) {
        return mapper.toDTO(findOrThrow(id));
    }

    @Override
    public CurrencyDTO getByCode(String code) {
        return repo.findByCode(code.toUpperCase())
                .map(mapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Currency not found with code: " + code));
    }

    // ── Create ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public CurrencyDTO create(CurrencyRequestDTO req) {
        String code = req.getCode().toUpperCase().trim();
        if (repo.existsByCode(code)) {
            throw new UserException(CODE_EXISTS + code);
        }
        return mapper.toDTO(repo.save(mapper.toEntity(req)));
    }

    // ── Update ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public CurrencyDTO update(Long id, CurrencyRequestDTO req) {
        Currency c = findOrThrow(id);

        String newCode = req.getCode().toUpperCase().trim();
        if (!newCode.equals(c.getCode()) && repo.existsByCode(newCode)) {
            throw new UserException(CODE_EXISTS + newCode);
        }

        mapper.updateEntity(c, req);
        return mapper.toDTO(repo.save(c));
    }

    // ── Activate / Deactivate ─────────────────────────────────────────────

    @Override
    @Transactional
    public CurrencyDTO setActive(Long id, boolean active) {
        Currency c = findOrThrow(id);
        c.setActive(active);
        return mapper.toDTO(repo.save(c));
    }

    // ── Delete ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void delete(Long id) {
        repo.delete(findOrThrow(id));
    }

    // ── Private ───────────────────────────────────────────────────────────

    private Currency findOrThrow(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Currency not found with id: " + id));
    }
}