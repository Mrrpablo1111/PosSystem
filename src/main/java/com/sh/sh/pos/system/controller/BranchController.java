package com.sh.sh.pos.system.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sh.sh.pos.system.exceptions.UserException;
import com.sh.sh.pos.system.payload.dto.BranchDTO;
import com.sh.sh.pos.system.service.BranchService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/branches")
public class BranchController {
	
	private final BranchService branchService;
	
	
	public ResponseEntity<BranchDTO> createBranch(@RequestBody BranchDTO branchDTO) throws UserException{
		
		BranchDTO createdBranch = branchService.createBranch(branchDTO);
		return ResponseEntity.ok(createdBranch);
	}
	
}
