package henry.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

public class Item extends BaseModel {
    @Expose @SerializedName("cant")
    @Getter private int cantidad;  // en milesimas
    @Expose @SerializedName("prod")
    @Getter @Setter private Producto producto;

    public Item() {
        cantidad = -1;
    }

    public void setCantidad(int cantidad) {
        if (cantidad >= 0) {
            this.cantidad = cantidad;
        }
    }

    public int getSubtotal() {
        if (producto == null) {
            return 0;
        }
        int precio = producto.getPrecio1();
        return milesimaPorCentavos(cantidad, precio);
    }

    public int getDescuento() {
        if (producto == null) {
            return 0;
        }
        if (cantidad >= producto.getThreshold()) {
            int descUnit = producto.getPrecio1() - getProducto().getPrecio2();
            return milesimaPorCentavos(cantidad, descUnit);
        }
        else {
            return 0;
        }
    }

    private static int milesimaPorCentavos(int milesimas, int centavos) {
        return (milesimas / 1000) * centavos + ((milesimas % 1000) * centavos) / 1000 ;
    }
}
