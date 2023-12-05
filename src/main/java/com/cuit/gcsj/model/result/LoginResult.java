package com.cuit.gcsj.model.result;


import lombok.Data;

import java.io.Serializable;

/**
 * @author L
 */
@Data
public class LoginResult implements Serializable {
    private String status;
    private String type;
}
