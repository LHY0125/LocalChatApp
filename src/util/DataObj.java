package util;

import java.io.Serializable;

public class DataObj implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String message;

    public DataObj(){}

    public DataObj(String id, String name, String message) {
        this.id = id;
        this.name = name;
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getMessage() {
        return message;
    }
}