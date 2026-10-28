package com.sh.sh.pos.system.service.storeservice;

import java.util.List;

import com.sh.sh.pos.system.domain.StoreStatus;
import com.sh.sh.pos.system.exception.ResourceNotFoundException;
import com.sh.sh.pos.system.exceptions.UserException;
import com.sh.sh.pos.system.model.user.User;
import com.sh.sh.pos.system.payload.dto.storedto.StoreDTO;
import com.sh.sh.pos.system.payload.dto.userdto.UserDTO;

public interface StoreService {
	StoreDTO createStore(StoreDTO storeDTO, User user) throws UserException;

	StoreDTO getStoreById(Long id) throws ResourceNotFoundException, UserException;

	List<StoreDTO> getAllStores(StoreStatus status) throws UserException;

	StoreDTO getStoreByAdminId() throws UserException;

	StoreDTO updateStore(Long id, StoreDTO storeDTO) throws ResourceNotFoundException, UserException;

	void deactivateStore(Long id) throws UserException, ResourceNotFoundException;

	StoreDTO getStoreByEmployee() throws UserException;
	List<UserDTO> getEmployeesByStore(Long storeId) throws UserException;

	StoreDTO moderateStore(Long storeId, StoreStatus status) throws ResourceNotFoundException, UserException;

    UserDTO addEmployee(Long id, UserDTO userDto) throws UserException;

}
