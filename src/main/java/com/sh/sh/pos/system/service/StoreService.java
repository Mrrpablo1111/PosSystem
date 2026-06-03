package com.sh.sh.pos.system.service;

import java.util.List;

import com.sh.sh.pos.system.domain.StoreStatus;
import com.sh.sh.pos.system.exception.ResourceNotFoundException;
import com.sh.sh.pos.system.exceptions.UserException;
import com.sh.sh.pos.system.model.Store;
import com.sh.sh.pos.system.model.User;
import com.sh.sh.pos.system.payload.dto.StoreDTO;
import com.sh.sh.pos.system.payload.dto.UserDTO;

public interface StoreService {
	StoreDTO createStore(StoreDTO storeDTO, User user);

	StoreDTO getStoreById(Long id) throws ResourceNotFoundException;

	List<StoreDTO> getAllStores(StoreStatus status);

	Store getStoreByAdminId() throws UserException;

	StoreDTO updateStore(Long id, StoreDTO storeDTO) throws ResourceNotFoundException, UserException;

	void deletedStore() throws UserException, ResourceNotFoundException;
	StoreDTO getStoreByEmployee() throws UserException;
	List<UserDTO> getEmployeesByStore(Long storeId) throws UserException;

	StoreDTO moderateStore(Long storeId, StoreStatus status) throws ResourceNotFoundException;

    UserDTO addEmployee(Long id, UserDTO userDto) throws UserException;

}
