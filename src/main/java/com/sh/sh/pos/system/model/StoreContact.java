package com.sh.sh.pos.system.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Embeddable
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StoreContact {
	@Column(name = "address")
    private String address;
 
    @Column(name = "phone")
    private String phone;
 
    @Email(message = "Invalid email format")
    @Column(name = "email")
    private String email;
}
