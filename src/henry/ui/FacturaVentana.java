package henry.ui;

import static henry.Helpers.displayAsMoney;
import static henry.Helpers.streamToString;
import static henry.Helpers.displayMilesimas;

import henry.api.FacturaInterface;
import henry.model.Documento;
import henry.model.Item;
import henry.model.Usuario;
import henry.model.Producto;
import henry.model.Cliente;
import henry.api.SearchEngine;
import henry.printing.GenericPrinter;
import lombok.Getter;
import net.miginfocom.swing.MigLayout;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

@SuppressWarnings("serial")
public class FacturaVentana extends JFrame {
    private JPanel panel;
    private JTextField codigo;
    private JTextField ruc;
    private JTextField pedidoField;
    private JTextField percentField;
    private JTextField codigoViejo;
    private JTextField codigoIngreso;
    private ClientePanel cliente;
    private Documento doc = new Documento();
    private static final String PEDIDO = "/api/pedido/%d";
    private static final String FACTURA = "/api/alm/2/nota/%d";
    private static final String INGRESO = "/api/ingreso/%d";

    final private FacturaInterface api;
    private int almacenId;
    Usuario usuario;

    private JButton load;
    private JButton print;
    private final JTextArea display;

    private int numero = 0;
    private GenericPrinter printer;

    private BasicCookieStore cookieStore;
    private CloseableHttpClient httpClient;
    private String baseUrl;
    private static int TIMEOUT_MILLIS = 30000;
    private RequestConfig timeoutConfig;
    private List<Item> realItems;
    private JLabel msg;

    private SearchDialog<Producto> prodSearchDialog =
            new SearchDialog<>(new SearchEngine<Producto>() {
                @Override
                public List<Producto> search(String prefijo) {
                    return api.buscarProducto(prefijo);
                }

                @Override
                public String toString() {
                    return "Producto";
                }
            });
    private SearchDialog<Cliente> clienteSearchDialog =
            new SearchDialog<>(new SearchEngine<Cliente>() {
                @Override
                public List<Cliente> search(String prefijo) {
                    return api.buscarCliente(prefijo);
                }

                @Override
                public String toString() {
                    return "Cliente";
                }
            });

    private SimpleDialog dialog = new SimpleDialog();

    private boolean isFactura;
    private Gson gson;

    public FacturaVentana(
            final FacturaInterface api,
            int almacenId,
            Usuario usuario,
            GenericPrinter printer,
            boolean isFactura) {
        this.api = api;
        this.almacenId = almacenId;
        this.usuario = usuario;
        this.printer = printer;
        this.isFactura = isFactura;

        gson = new GsonBuilder()
                   .excludeFieldsWithoutExposeAnnotation()
                   .create();
        timeoutConfig = RequestConfig.custom()
            .setConnectionRequestTimeout(TIMEOUT_MILLIS)
            .setConnectTimeout(TIMEOUT_MILLIS)
            .setSocketTimeout(TIMEOUT_MILLIS)
            .build();

        cookieStore = new BasicCookieStore();
        httpClient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();

        panel = new JPanel();
        getContentPane().add(panel);
        panel.setLayout(new MigLayout("", "[][][][]", ""));

        cliente = new ClientePanel(this.api, clienteSearchDialog);
        panel.add(cliente, "wrap");

        JPanel input = new JPanel();
        input.setLayout(new MigLayout());

        input.add(new JLabel("Numero de Factura: "));
        codigo = new JTextField();
        input.add(codigo, "wrap");
        codigo.setColumns(20);

        input.add(new JLabel("RUC de venta: "));
        ruc = new JTextField();
        input.add(ruc);
        input.add(ruc, "wrap");
        ruc.setColumns(20);
        
        pedidoField = new JTextField();
        pedidoField.setColumns(20);
        /*
        input.add(new JLabel("Por Nota de pedido: "));
        input.add(pedidoField, "wrap");
        */

        input.add(new JLabel("Porciento: "));
        percentField = new JTextField();
        percentField.setText("100");
        input.add(percentField, "wrap");

        input.add(new JLabel("Por Factura de Bodega: "));
        codigoViejo = new JTextField();
        input.add(codigoViejo, "wrap");
        codigoViejo.setColumns(20);

        input.add(new JLabel("Por Ingreso: "));
        codigoIngreso = new JTextField();
        input.add(codigoIngreso, "wrap");
        codigoIngreso.setColumns(20);

        load = new JButton("Cargar");
        print = new JButton("Imprimir");
        JButton cancel = new JButton("Cancelar");
        input.add(load);
        input.add(cancel);
        input.add(print, "wrap");

        msg = new JLabel();
        input.add(msg, "span 3, wrap");

        display = new JTextArea(100, 55);
        JScrollPane scroll = new JScrollPane(display);
        display.setLineWrap(true);
        display.setEditable(false);
        input.add(display, "span 3");
        panel.add(input);
        setTitle("Imprimir Factura");
        String displayFacturaText = "";
        panel.setBackground(Color.RED);
        setBounds(100, 100, 735, 655);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        load.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<Item> items = getItemFromUI();
                int percent = 100;
                try {
                    percent = Integer.parseInt(percentField.getText());
                } catch (NumberFormatException ex) { }
                if (items == null) {
                    display.setText("Codigos incorrecto");
                    return;
                }
                StringBuilder s = new StringBuilder();
                int total = 0;
                for (Item x : items) {
                    Producto prod = x.getProducto();
                    prod.setPrecio1(prod.getPrecio1() * percent / 100);
                    prod.setPrecio2(prod.getPrecio2() * percent / 100);
                    doc.addItem(x);
                }

                for (Item x : doc.getItems()) {
                    s.append(displayMilesimas(x.getCantidad()));
                    s.append('\t');
                    s.append(x.getProducto().getNombre());
                    s.append('\t');
                    s.append(displayAsMoney(x.getProducto().getPrecio1()));
                    s.append('\t');
                    s.append(displayAsMoney(x.getSubtotal()));
                    s.append('\n');
                    total += x.getSubtotal();
                }
                doc.setSubtotal(total);
                doc.setIvaPorciento(12);
                int iva = total * 12 / 100;
                StringBuilder s2 = new StringBuilder();
                s2.append(String.format("Total: %s, Iva: %s\n", 
                    displayAsMoney(doc.getTotal()), displayAsMoney(doc.getIva())));
                s2.append(s.toString());
                display.setText(s2.toString());
            }
        });

        print.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveAndPrint();
            }
        });

        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clear();
            }
        });
    }

    private boolean saveAndPrint() {
        if (cliente.getCliente() == null) {
            msg.setText("Ingrese Cliente");
            return false;
        }
        int factno;
        try {
            factno = Integer.parseInt(codigo.getText());
        } catch (NumberFormatException e) {
            msg.setText("Ingrese Numero de factura");
            return false;
        }
        if (ruc.getText().length() < 5) {
            msg.setText("Ingrese RUC");
            return false;
        }

        doc.setUser(usuario);
        doc.setCodigo(factno);
        doc.setCliente(cliente.getCliente());
        JsonObject fact = api.serializeDocumento(doc);
        JsonObject meta = fact.getAsJsonObject("meta");
        meta.addProperty("almacen_ruc", ruc.getText());
        meta.remove("almacen_id");
        JsonObject options = new JsonObject();
        options.addProperty("no_alm_id", true);
        fact.add("options", options);
        if (api.guardarDocumentoObj(fact, true) > 0) {
            if (printer.printFactura(doc)) {
                clear();
                return true;
            }
        }
        return false;
    }

    private List<Item> getItemFromUI() {
        int num;
        String method;
        try {
            System.out.println(pedidoField.getText());
            num = Integer.parseInt(pedidoField.getText());
            method = PEDIDO;
        } catch (NumberFormatException e1) {
            try {
                num = Integer.parseInt(codigoViejo.getText());
                method = FACTURA;
            } catch (NumberFormatException e2) {
                try {
                    num = Integer.parseInt(codigoIngreso.getText());
                    method = INGRESO;
                } catch (NumberFormatException e3) {
                    return null;
                }
            }
        }
        List<Item> items = api.getItems(method, num, method.equals(PEDIDO));
        return items;
    }

    void clear() {
        codigo.setText("");
        ruc.setText("");
        codigoViejo.setText("");
        codigoIngreso.setText("");
        pedidoField.setText("");
        display.setText("");
        msg.setText("");
        cliente.clear();
        doc = new Documento();
    }
}
