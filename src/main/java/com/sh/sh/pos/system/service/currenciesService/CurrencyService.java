package com.sh.sh.pos.system.service.currenciesService;



import java.util.List;

import com.sh.sh.pos.system.payload.dto.currenciesDTO.CurrencyDTO;
import com.sh.sh.pos.system.payload.request.CurrencyRequestDTO;

public interface CurrencyService {

    List<CurrencyDTO> getAll();
 
    List<CurrencyDTO> getActive();
 
    CurrencyDTO getById(Long id);
 
    CurrencyDTO getByCode(String code);
 
    CurrencyDTO create(CurrencyRequestDTO req);
 
    CurrencyDTO update(Long id, CurrencyRequestDTO req);
 
    CurrencyDTO setActive(Long id, boolean active);
 
    void delete(Long id);
}