package henry.model;

import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;

public class Cliente {
    @Expose
    @Getter @Setter
    private String codigo;

    @Expose
    @Getter @Setter
    private String nombres;

    @Expose
    @Getter @Setter
    private String apellidos;

    @Expose
    @Getter @Setter
    private String direccion;

    @Expose
    @Getter @Setter
    private String ciudad;

    @Expose
    @Getter @Setter
    private String tipo;

    @Expose
    @Getter @Setter
    private String telefono;

    @Override
    public String toString() {
        return apellidos + " " + nombres;
    }
}

