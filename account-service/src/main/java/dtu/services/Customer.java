package dtu.services;
import java.io.Serializable;

public class Customer implements Serializable{

    private static final long serialVersionUID = 9023222981284806610L;
    private String name;
    private String id;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
}
