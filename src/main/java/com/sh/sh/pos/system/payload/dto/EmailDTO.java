package com.sh.sh.pos.system.payload.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailDTO implements Serializable{
    private String to;
    private String subject;
    private String body;
}
