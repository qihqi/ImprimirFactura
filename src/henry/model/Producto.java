package henry.model;

import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;

public class Producto {
    @Expose
    @Getter @Setter
    private String codigo;

    @Expose
    @Getter @Setter
    private String nombre;

    //precios en unidad de centavos
    @Expose
    @Getter @Setter
    private int precio1;

    @Expose
    @Getter @Setter
    private int precio2;

    @Expose
    @Getter @Setter
    private int threshold;

    @Override
    public String toString() {
        return nombre;
    }
}
