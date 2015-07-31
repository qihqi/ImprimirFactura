package henry.printing;

import static henry.Helpers.displayAsMoney;
import static henry.Helpers.displayMilesimas;

import henry.model.Item;
import henry.model.Documento;
import henry.model.Cliente;
import henry.model.Producto;
import henry.printing.Config.ImpressionConfig;
import lombok.Setter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public abstract class GenericPrinter {

    @Setter
    protected Documento documento;
    protected Config config;

    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    private double lastItemY;
    private static final double[] ZERO = new double[]{0.0, 0.0};

    public GenericPrinter(Config config) {
        this.config = config;
    }

    public abstract boolean printFactura(Documento documento);
    public abstract void printContent(String content, double x, double y);

    protected void printTitle() {
        if (!config.isFacturaBlanco()) {//titulos ya estan impresas
            return;
        }
        double[] titlePos = config.getImpression().getTitle();
        int codigo = documento.getCodigo();
        String value = (config.isFactura() ? "Orden De Despacho: " :
                                             "Nota De Pedido: ") + codigo;
        System.out.println("" + titlePos[0] + " " + titlePos[1]);
        printContent(value, titlePos[0], titlePos[1]);
    }

    protected void drawElement(double[] pos, double[] delta, String label, String content) {
        if (content == null) {
            content = "";
        }
        double x = pos[0];
        double y = pos[1];
        if (config.isFacturaBlanco() && label.length() > 0) {
            content = String.format("%s: %s", label.toUpperCase(), content);
            x += delta[0];
            y += delta[1];
        }
        printContent(content, x, y);
    }
    
    protected void printClient(Cliente cliente) {
        Config.ImpressionConfig impConfig = config.getImpression();
        drawElement(impConfig.getRuc(), impConfig.getRucDelta(),
                    "RUC", cliente.getCodigo());
        drawElement(impConfig.getCliente(), impConfig.getClienteDelta(),
                    "cliente", cliente.toString());
        drawElement(impConfig.getTelf(), impConfig.getTelfDelta(),
                    "telf", cliente.getTelefono());
        String direccion = cliente.getDireccion();
        if (direccion == null) {
            direccion = "";
        }
        if (direccion.length() > 20) {
            direccion = direccion.substring(0, 20);
        }
        drawElement(impConfig.getDireccion(), impConfig.getDireccionDelta(),
                    "direccion", direccion);
        drawElement(impConfig.getFecha(), impConfig.getFechaDelta(),
                    "fecha", format.format(new Date()));
    }

    protected boolean printContent(int page, int lineWidth) {
        int lines = config.getImpression().getLineas();
        // page is the current page number. 
        // if say it is 2 there are 2 printed pages, we should start from the next
        // line
        int start = page * lines; // total lines?
        int end;
        boolean endOfPage;
        List<Item> items = documento.getItems();
        if (start + lines > items.size()) {
            // if we are here means that this is the last page
            end = items.size(); 
            endOfPage = true; 
        }
        else { 
            end = (start + lines);
            endOfPage = false;
        }

        double[] pos = config.getImpression().getContenido().getPos();
        double[] spacing = config.getImpression().getContenido().getSp();
        final double LEFT_EDGE = pos[0];
        System.out.println(lineWidth);
        double x = pos[0];
        double y = pos[1];
        for (int i = start; i < end; i++) {
            Item item = items.get(i);
            if (item!= null && item.getProducto() != null && item.getCantidad() > 0) {
                for (int j = 0; j < 5; j++) {
                    printContent(getItemValue(j, items.get(i)), x, y);
                    if (j < 4) {
                       x += spacing[j];
                    }
                }
                x = LEFT_EDGE;
                y += lineWidth;
                lastItemY = (float) y;
            }
        }
        return endOfPage;
    }

    private static String getItemValue(int pos, Item v) {
        Producto p = v.getProducto();
        switch (pos) {
            case 0:
                return p.getCodigo();
            case 1:
                return displayMilesimas(v.getCantidad());
            case 2:
                String nombre = p.getNombre();
                if (nombre.length() > 55)
                    return shorten(nombre);
                return nombre;
            case 3:
                return displayAsMoney(p.getPrecio1());
            case 4:
                return displayAsMoney(v.getSubtotal());
        }
        return null;
    }
    
    private static String shorten(String s) {
        String [] words = s.split("[ ]+");
        String result = "";
        for (String w : words) {
            if (w.length() > 6)
                result += w.substring(0, 5);
            else 
                result += w;
            result += ' ';
        }
        return result;
    }
    
    protected void printValues() {
        String [] titles = { "bruto", "neto", "desc", "iva", "total" };
        ImpressionConfig imp = config.getImpression();
        drawElement(imp.getBruto(), ZERO, "Subtotal", displayAsMoney(documento.getSubtotal()));
        drawElement(imp.getNeto(), ZERO, "Valor Neto", displayAsMoney(documento.getTotalNeto()));
        drawElement(imp.getDesc(), ZERO, "Descuento", displayAsMoney(documento.getDescuento()));
        drawElement(imp.getIva(), ZERO, "Descuento", displayAsMoney(documento.getIva()));
        drawElement(imp.getTotal(), ZERO, "Descuento", displayAsMoney(documento.getTotal()));
    }
    
    protected void printFirma() {
        ImpressionConfig imp = config.getImpression();
        String value = config.isFactura() ? "Cliente": "Despachador";
        drawElement(imp.getFirma1(), ZERO, "", value);
        drawElement(imp.getFirma2(), ZERO, "", "Verificador");
        drawElement(imp.getFirma3(), ZERO, "", "Seguridad");
    }
}

