package com.sh.sh.pos.system.service.serviceImpl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.domain.StoreStatus;
import com.sh.sh.pos.system.exceptions.UserException;
import com.sh.sh.pos.system.mapper.StoreMapper;
import com.sh.sh.pos.system.model.Store;
import com.sh.sh.pos.system.model.StoreContact;
import com.sh.sh.pos.system.model.User;
import com.sh.sh.pos.system.payload.dto.StoreDTO;
import com.sh.sh.pos.system.repository.StoreRepository;
import com.sh.sh.pos.system.service.StoreService;
import com.sh.sh.pos.system.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {
	
	private final StoreRepository storeRepository;
	private final UserService  userService;

	@Override
	public StoreDTO createStore(StoreDTO storeDTO, User user) {
		Store store = StoreMapper.toEntity(storeDTO, user);
		
		return StoreMapper.toDTO(storeRepository.save(store));
	}

	@Override
	public StoreDTO getStoreById(Long id) throws Exception {
		Store store = storeRepository.findById(id).orElseThrow(()-> new Exception("store not found"));
		return StoreMapper.toDTO(store);
	}

	@Override
	public List<StoreDTO> getAllStores() {
		List<Store> dtos = storeRepository.findAll(); 
		return dtos.stream().map(StoreMapper::toDTO).collect(Collectors.toList());
		
	}

	@Override
	public Store getStoreByAdmin() throws UserException {
		User admin = userService.getCurrentUser();
		return storeRepository.findByStoreAdminId(admin.getId());
	}

	@Override
	public StoreDTO updateStore(Long id, StoreDTO storeDTO) throws UserException {
		User currentUser = userService.getCurrentUser();
		
		Store existing = storeRepository.findByStoreAdminId(currentUser.getId());
		
		if(existing == null) {
			throw new UserException("store not found");
		}
		
		existing.setBrand(storeDTO.getBrand());
		existing.setDescription(storeDTO.getDescription()); 
		
		if(storeDTO.getStoreType()!=null) {
			existing.setStoreType(storeDTO.getStoreType());
		}
		if(storeDTO.getContact()!=null) {
			StoreContact contact = StoreContact.builder()
					.address(storeDTO.getContact().getAddress())
					.phone(storeDTO.getContact().getPhone())
					.email(storeDTO.getContact().getEmail())
					.build();
			existing.setContact(contact);
		}
		Store updateStore = storeRepository.save(existing);
		
		return StoreMapper.toDTO(updateStore);
	}

	@Override
	public void deletedStore(Long id) throws UserException {
		Store store = getStoreByAdmin();
		storeRepository.delete(store);
	
	}

	@Override
	public StoreDTO getStoreByEmployee() throws UserException {
		User currentUser = userService.getCurrentUser();
		
		if(currentUser==null) {
			throw new UserException("you don't have permission to access this store");
		}
		return StoreMapper.toDTO(currentUser.getStore()); 
	}

	@Override
	public StoreDTO moderateStore(Long id, StoreStatus status) throws Exception {
		Store store = storeRepository.findById(id).orElseThrow(()-> new Exception("store not found"));
		store.setStatus(status);
		Store updatedStore=storeRepository.save(store);
		return StoreMapper.toDTO(updatedStore) ;
	}

}
