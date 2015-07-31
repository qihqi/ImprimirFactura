package henry.ui;

import static henry.Helpers.displayAsMoney;
import static henry.Helpers.streamToString;

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
    private JTextField codigoViejo;
    private JTextField codigoIngreso;
    private ClientePanel cliente;
    private Documento doc = new Documento();
    private static final String PEDIDO = "/api/pedido/%d";
    private static final String FACTURA = "/api/bod/%d";
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
        
        input.add(new JLabel("Por Nota de pedido: "));
        pedidoField = new JTextField();
        input.add(pedidoField, "wrap");
        pedidoField.setColumns(20);

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
        input.add(load);
        input.add(print, "wrap");

        display = new JTextArea(20, 40);
        JScrollPane scroll = new JScrollPane(display);
        display.setLineWrap(true);
        input.add(display);
        panel.add(input);
        setTitle("Imprimir Factura");
        String displayFacturaText = "";
        panel.setBackground(Color.RED);
        setBounds(100, 100, 735, 655);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        load.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int num;
                String method;
                try {
                    num = Integer.parseInt(pedidoField.getText());
                    method = PEDIDO;
                } catch (NumberFormatException e1) {
                    try {
                        num = Integer.parseInt(codigo.getText());
                        method = FACTURA;
                    } catch (NumberFormatException e2) {
                        try {
                            num = Integer.parseInt(codigoIngreso.getText());
                            method = INGRESO;
                        } catch (NumberFormatException e3) {
                            display.setText("Codigos incorrecto");
                            return;
                        }
                    }
                }
                List<Item> items = api.getItems(method, num);
                String s = "";
                for (Item x : items) {
                    s += gson.toJson(x) + '\n';
                    doc.addItem(x);
                }
                display.setText(s);
            }
        });
    }

    private String getUrl(URI uri) {
        HttpGet req = new HttpGet(uri);
        req.setConfig(timeoutConfig);
        try (CloseableHttpResponse response = httpClient.execute(req)) {
            HttpEntity entity = response.getEntity();
            if (response.getStatusLine().getStatusCode() == 200) {
                String content = streamToString(entity.getContent());
                return content;
            }
        }
        catch (IOException e) {
        }
        return null;
    }

}
