package priv.asura.wechat_service.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class Client implements Serializable {
    private String id;
    private String name;
}
