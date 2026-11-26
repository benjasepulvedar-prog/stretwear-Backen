package cl.duoc.streetwear_backend.models;

import java.util.Locale;

public enum Role {
    ADMIN, EMPLOYEE, CUSTOMER;

    public String getValue() {
        return name().toLowerCase(Locale.ROOT);
    }
}
