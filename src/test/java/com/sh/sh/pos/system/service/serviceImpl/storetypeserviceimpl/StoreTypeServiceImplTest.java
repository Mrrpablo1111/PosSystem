package com.sh.sh.pos.system.service.serviceimpl.storetypeserviceimpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
import com.sh.sh.pos.system.mapper.storemapper.StoreTypeMapper;
import com.sh.sh.pos.system.model.store.StoreType;
import com.sh.sh.pos.system.payload.dto.storedto.StoreTypeDTO;
import com.sh.sh.pos.system.repository.storetyperepository.StoreTypeRepository;
import com.sh.sh.pos.system.service.serviceimpl.storeserviceimpl.StoreTypeServiceImpl;

@ExtendWith(MockitoExtension.class)
@DisplayName("StoreTypeServiceImpl tests")
class StoreTypeServiceImplTest {

    @Mock
    private StoreTypeRepository storeTypeRepository;

    @Mock
    private StoreTypeMapper storeTypeMapper;

    @InjectMocks
    private StoreTypeServiceImpl storeTypeService;

    private StoreType storeType;
    private StoreTypeDTO storeTypeDTO;

    @BeforeEach
    void setUp() {

        storeType = new StoreType();
        storeType.setId(1L);
        storeType.setCode("RICE_SHOP");
        storeType.setName("Rice Shop");
        storeType.setDescription("Rice retail business");
        storeType.setActive(true);

        storeTypeDTO = new StoreTypeDTO();
        storeTypeDTO.setId(1L);
        storeTypeDTO.setCode("RICE_SHOP");
        storeTypeDTO.setName("Rice Shop");
        storeTypeDTO.setDescription("Rice retail business");
        storeTypeDTO.setActive(true);
    }

    // =====================================================
    // GET STORE TYPE
    // =====================================================

    @Test
    @DisplayName("Get StoreType by ID returns DTO")
    void getStoreTypeById_found() throws Exception {

        when(storeTypeRepository.findById(1L))
                .thenReturn(Optional.of(storeType));

        when(storeTypeMapper.toDTO(storeType))
                .thenReturn(storeTypeDTO);

        StoreTypeDTO result =
                storeTypeService.getStoreTypeById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCode()).isEqualTo("RICE_SHOP");

        verify(storeTypeRepository).findById(1L);
    }

    @Test
    @DisplayName("Get missing StoreType throws exception")
    void getStoreTypeById_notFound() {

        when(storeTypeRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> storeTypeService.getStoreTypeById(99L)
        );
    }

    // =====================================================
    // CREATE STORE TYPE
    // =====================================================

    @Test
    @DisplayName("Create StoreType saves and returns DTO")
    void createStoreType_success() throws Exception {

        StoreTypeDTO request = new StoreTypeDTO();
        request.setCode("RICE_SHOP");
        request.setName("Rice Shop");
        request.setDescription("Rice retail business");

        when(storeTypeRepository.existsByCode("RICE_SHOP"))
                .thenReturn(false);

        when(storeTypeRepository.existsByNameIgnoreCase("Rice Shop"))
                .thenReturn(false);

        when(storeTypeMapper.toDTO(any(StoreType.class)))
                .thenReturn(storeTypeDTO);

        when(storeTypeRepository.save(any(StoreType.class)))
                .thenAnswer(invocation -> {
                    StoreType saved = invocation.getArgument(0);
                    saved.setId(1L);
                    return saved;
                });

        StoreTypeDTO result =
                storeTypeService.createStoreType(request);

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("RICE_SHOP");

        verify(storeTypeRepository).save(any(StoreType.class));
    }

    @Test
    @DisplayName("Create duplicate StoreType code throws exception")
    void createStoreType_duplicateCode() {

        StoreTypeDTO request = new StoreTypeDTO();
        request.setCode("RICE_SHOP");
        request.setName("Rice Shop");

        when(storeTypeRepository.existsByCode("RICE_SHOP"))
                .thenReturn(true);

        assertThrows(
                UserException.class,
                () -> storeTypeService.createStoreType(request)
        );

        verify(storeTypeRepository, never())
                .save(any(StoreType.class));
    }

    // =====================================================
    // UPDATE STORE TYPE
    // =====================================================

    @Test
    @DisplayName("Update StoreType changes its name")
    void updateStoreType_success() throws Exception {

        StoreTypeDTO request = new StoreTypeDTO();
        request.setCode("RICE_SHOP");
        request.setName("Rice and Grain Shop");
        request.setDescription("Rice and grain retail");

        when(storeTypeRepository.findById(1L))
                .thenReturn(Optional.of(storeType));

        when(storeTypeRepository.save(storeType))
                .thenReturn(storeType);

        when(storeTypeMapper.toDTO(storeType))
                .thenAnswer(invocation -> {
                    StoreType entity = invocation.getArgument(0);

                    StoreTypeDTO dto = new StoreTypeDTO();
                    dto.setId(entity.getId());
                    dto.setCode(entity.getCode());
                    dto.setName(entity.getName());
                    dto.setDescription(entity.getDescription());
                    dto.setActive(entity.isActive());

                    return dto;
                });

        StoreTypeDTO result =
                storeTypeService.updateStoreType(1L, request);

        assertThat(result.getName())
                .isEqualTo("Rice and Grain Shop");

        assertThat(storeType.getDescription())
                .isEqualTo("Rice and grain retail");

        verify(storeTypeRepository).save(storeType);
    }

    @Test
    @DisplayName("Update missing StoreType throws exception")
    void updateStoreType_notFound() {

        StoreTypeDTO request = new StoreTypeDTO();
        request.setCode("RICE_SHOP");
        request.setName("Rice Shop");

        when(storeTypeRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> storeTypeService.updateStoreType(99L, request)
        );

        verify(storeTypeRepository, never())
                .save(any(StoreType.class));
    }

    // =====================================================
    // ACTIVATE / DEACTIVATE
    // =====================================================

    @Test
    @DisplayName("Deactivate StoreType sets active to false")
    void deactivateStoreType_success() throws Exception {

        when(storeTypeRepository.findById(1L))
                .thenReturn(Optional.of(storeType));

        when(storeTypeRepository.save(storeType))
                .thenReturn(storeType);

        when(storeTypeMapper.toDTO(storeType))
                .thenAnswer(invocation -> {
                    StoreType entity = invocation.getArgument(0);

                    StoreTypeDTO dto = new StoreTypeDTO();
                    dto.setId(entity.getId());
                    dto.setCode(entity.getCode());
                    dto.setName(entity.getName());
                    dto.setActive(entity.isActive());

                    return dto;
                });

        StoreTypeDTO result =
                storeTypeService.deactivateStoreType(1L);

        assertThat(storeType.isActive()).isFalse();
        assertThat(result.isActive()).isFalse();

        verify(storeTypeRepository).save(storeType);
    }

    @Test
    @DisplayName("Activate StoreType sets active to true")
    void activateStoreType_success() throws Exception {

        storeType.setActive(false);

        when(storeTypeRepository.findById(1L))
                .thenReturn(Optional.of(storeType));

        when(storeTypeRepository.save(storeType))
                .thenReturn(storeType);

        when(storeTypeMapper.toDTO(storeType))
                .thenAnswer(invocation -> {
                    StoreType entity = invocation.getArgument(0);

                    StoreTypeDTO dto = new StoreTypeDTO();
                    dto.setId(entity.getId());
                    dto.setCode(entity.getCode());
                    dto.setName(entity.getName());
                    dto.setActive(entity.isActive());

                    return dto;
                });

        StoreTypeDTO result =
                storeTypeService.activateStoreType(1L);

        assertThat(storeType.isActive()).isTrue();
        assertThat(result.isActive()).isTrue();

        verify(storeTypeRepository).save(storeType);
    }

    // =====================================================
    // GET ACTIVE STORE TYPES
    // =====================================================

    @Test
    @DisplayName("Get active StoreTypes returns active types")
    void getActiveStoreTypes_success() {

        when(storeTypeRepository.findByIsActiveTrueOrderByNameAsc())
                .thenReturn(List.of(storeType));

        when(storeTypeMapper.toDTO(storeType))
                .thenReturn(storeTypeDTO);

        List<StoreTypeDTO> result =
                storeTypeService.getActiveStoreTypes();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode())
                .isEqualTo("RICE_SHOP");
    }
}
