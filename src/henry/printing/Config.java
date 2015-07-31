package henry.printing;

import static henry.Helpers.streamToString;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import lombok.Setter;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;

public final class Config {
     public static class ContenidoConfig {
         @Getter
         double[] pos;
         @Getter
         double[] sp;
         @Getter
         double vsp;
    }

    public static class ImpressionConfig {
         double[] disp;
         @Getter
         double[] tamano;
         @Getter
         double[] ruc;
         @Getter
         double[] cliente;
         @Getter
         double[] remision;
         @Getter
         double[] direccion;
         @Getter 
         double[] fecha;
         @Getter
         double[] telf;
         @Getter @SerializedName("ruc_delta")
         double[] rucDelta;
         @Getter @SerializedName("direccion_delta")
         double[] direccionDelta;
         @Getter @SerializedName("fecha_delta")
         double[] fechaDelta;
         @Getter @SerializedName("telf_delta")
         double[] telfDelta;
         @Getter @SerializedName("cliente_delta")
         double[] clienteDelta;
         @Getter
         double[] bruto;
         @Getter
         double[] desc;
         @Getter
         double[] neto;
         @Getter
         double[] iva;
         @Getter
         double[] total;
         @Getter
         double[] title;
         @Getter
         int lineas;
         @Getter
         ContenidoConfig contenido;
         @Getter
         double[] firma1;
         @Getter
         double[] firma2;
         @Getter
         double[] firma3;
    }

    @Getter
    @SerializedName("fontsize")
    int fontSize;
    @Getter
    @SerializedName("fontfamily")
    String fontFamily; 
    @Getter
    boolean factura;

    @Getter
    @SerializedName("factura_blanco")
    boolean facturaBlanco;
    @Getter
    int libre;

    @Getter
    ImpressionConfig impression;

    @Getter @SerializedName("servidores")
    String[] serversOpts;

    @Getter @SerializedName("almacenes")
    String[] storeOpts;

    @Getter @SerializedName("impresora_matriz")
    boolean matrixPrinter;

    public static Config getConfigFromJson(String json) {
        System.out.println(json);
        Config config = new Gson().fromJson(json, Config.class);
        addDisplacement(config.impression);
        return config;
    }

    private static void addDisplacement(ImpressionConfig imp) {
         addto(imp.ruc, imp.disp);
         addto(imp.cliente, imp.disp);
         addto(imp.remision, imp.disp);
         addto(imp.direccion, imp.disp);
         addto(imp.fecha, imp.disp);
         addto(imp.telf, imp.disp);
         addto(imp.bruto, imp.disp);
         addto(imp.desc, imp.disp);
         addto(imp.neto, imp.disp);
         addto(imp.iva, imp.disp);
         addto(imp.total, imp.disp);
         addto(imp.title, imp.disp);
         addto(imp.firma1, imp.disp);
         addto(imp.firma2, imp.disp);
         addto(imp.firma3, imp.disp);
         addto(imp.contenido.pos, imp.disp);
    }
    public static void addto(double[] a, double[] b) {
        a[0] += b[0];
        a[1] += b[1];
    }
    public static void main(String[] args) throws Exception {
        String content;
        try (InputStream stream = new FileInputStream(args[0])) {
            Config config = Config.getConfigFromJson(streamToString(stream));
            System.out.println(config.getImpression().getTitle()[0]);
        }
    }
}
