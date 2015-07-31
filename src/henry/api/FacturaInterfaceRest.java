package henry.api;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
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

import henry.model.Cliente;
import henry.model.Documento;
import henry.model.Item;
import henry.model.Producto;
import henry.model.Usuario;

import static henry.Helpers.streamToString;


/**
 * Created by han on 12/30/13.
 */
public class FacturaInterfaceRest implements FacturaInterface {

    private static final String PROD_URL_PATH = "/api/producto";
    private static final String CLIENT_URL_PATH = "/api/cliente";
    private static final String VENTA_URL_PATH = "/api/pedido";
    private static final String FACTURA_URL_PATH = "/api/nota";
    private static final String PROD_URL = "/api/alm/%s/producto/%s";
    private static final String LOGIN_URL = "/api/authenticate";

    private BasicCookieStore cookieStore;
    private CloseableHttpClient httpClient;
    private String baseUrl;
    private Gson gson;
    private int almacenId;
    private static int TIMEOUT_MILLIS = 30000;
    private RequestConfig timeoutConfig;

    public FacturaInterfaceRest(String baseUrl, int almacenId) {
        timeoutConfig = RequestConfig.custom()
            .setConnectionRequestTimeout(TIMEOUT_MILLIS)
            .setConnectTimeout(TIMEOUT_MILLIS)
            .setSocketTimeout(TIMEOUT_MILLIS)
            .build();

        cookieStore = new BasicCookieStore();
        httpClient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
        this.baseUrl = baseUrl;
        this.almacenId = almacenId;
        gson = new GsonBuilder()
                   .excludeFieldsWithoutExposeAnnotation()
                   .create();
    }

    @Override
    public Producto getProductoPorCodigo(String codigo) throws NotFoundException {
        try {
            String url = String.format(PROD_URL, "1", codigo);
            URI prodUri = new URIBuilder().setScheme("http")
                                          .setHost(baseUrl)
                                          .setPath(url)
                                          .build();
            String content = getUrl(prodUri);
            if (content == null) {
                throw new NotFoundException(String.format("Producto %s no encontrado", codigo));
            }
            return gson.fromJson(content, Producto.class);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Producto> buscarProducto(String prefijo) {
        try {
            URI prodUri = new URIBuilder().setScheme("http")
                    .setHost(baseUrl)
                    .setPath(PROD_URL_PATH)
                    .setParameter("prefijo", prefijo)
                    .setParameter("bodega_id", "1").build();
            System.out.println(prodUri.toString());
            String content = getUrl(prodUri);
            if (content == null) {
                return new ArrayList<>();
            }
            return Arrays.asList(gson.fromJson(content, Producto[].class));
        }
        catch (URISyntaxException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public Cliente getClientePorCodigo(String codigo) throws NotFoundException {
        try {
            URI uri = new URIBuilder().setScheme("http")
                    .setHost(baseUrl)
                    .setPath(CLIENT_URL_PATH + "/" + codigo)
                    .build();
            String content = getUrl(uri);
            if (content == null) {
                throw new NotFoundException("cliente not found");
            }
            return gson.fromJson(content, Cliente.class);
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
            throw new NotFoundException("");
        }
    }

    @Override
    public List<Cliente> buscarCliente(String prefijo) {
        try {
            URI uri = new URIBuilder().setScheme("http")
                    .setHost(baseUrl)
                    .setPath(CLIENT_URL_PATH)
                    .setParameter("prefijo", prefijo).build();
            String content = getUrl(uri);
            if (content == null) {
                return new ArrayList<>();
            }
            return Arrays.asList(gson.fromJson(content, Cliente[].class));
        }
        catch (URISyntaxException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    static class Codigo {
        @Expose
        int codigo;
    }

    @Override
    public int guardarDocumento(Documento doc, boolean isFactura) {
        System.out.println("guardarDocumento");
        JsonObject factura = serializeDocumento(doc);
        String content = gson.toJson(factura);
        System.out.println(content);
        String path = isFactura ? FACTURA_URL_PATH : VENTA_URL_PATH;
        try {
            URI uri = new URIBuilder().setScheme("http").setHost(baseUrl)
                    .setPath(path).build();
            HttpPost req = new HttpPost(uri);
            req.setConfig(timeoutConfig);
            req.setEntity(new StringEntity(content));
            try (CloseableHttpResponse response = httpClient.execute(req)) {
                if (response.getStatusLine().getStatusCode() == 200) {
                    HttpEntity entity = response.getEntity();
                    String result = streamToString(entity.getContent());
                    int codigo = gson.fromJson(result, Codigo.class).codigo;
                    return codigo;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        catch (URISyntaxException|UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }
        return -1;

    }

    private JsonObject serializeDocumento(Documento doc) {
        JsonObject meta = new JsonObject();
        meta.add("client", gson.toJsonTree(doc.getCliente()));
        meta.addProperty("user", doc.getUser().getNombre());
        meta.addProperty("total", doc.getTotal());
        meta.addProperty("subtotal", doc.getSubtotal());
        meta.addProperty("discount", doc.getDescuento());

        meta.addProperty("tax_percent", doc.getIvaPorciento());
        meta.addProperty("discount_percent", doc.getDescuentoGlobalPorciento());

        meta.addProperty("almacen_id", almacenId);

        meta.addProperty("total", doc.getTotal());
        meta.addProperty("subtotal", doc.getSubtotal());
        meta.addProperty("tax", doc.getIva());
        meta.addProperty("discount", doc.getDescuento());
        meta.addProperty("codigo", "" + doc.getCodigo());
        meta.addProperty("paid_amount", doc.getPagado());
        meta.addProperty("change", doc.getCambio());
        meta.addProperty("payment_format", doc.getFormaPago());

        JsonArray items = new JsonArray();
        for (Item i : doc.getItems()) {
            if (i.getProducto() != null) {
                items.add(gson.toJsonTree(i));
            }
        }
        JsonObject factura = new JsonObject();
        factura.add("meta", meta);
        factura.add("items", items);
        return factura;
    }

    Documento parseDocumento(JsonObject json) {
        Documento doc = new Documento();
        JsonObject metadata = json.get("meta").getAsJsonObject();
        JsonElement clientObj = metadata.get("client"); 
        if (clientObj != null) {
            String clientString = clientObj.toString();
            if (clientString.length() > 0) {
                doc.setCliente(gson.fromJson(clientString, Cliente.class));
            }
        }
        JsonArray items = json.get("items").getAsJsonArray();
        for (JsonElement e : items) {
            JsonObject obj = e.getAsJsonObject();
            Item item = gson.fromJson(obj.toString(), Item.class);
            if (item != null) {
                doc.addItem(item);
            }
        }
        JsonElement iva_porciento = metadata.get("iva_porciento");
        if (iva_porciento != null) {
            doc.setIvaPorciento(iva_porciento.getAsInt());
        }
        JsonElement descuentoGlobal= metadata.get("descuento_global_porciento");
        if (descuentoGlobal != null) {
            doc.setDescuentoGlobalPorciento(descuentoGlobal.getAsInt());
        }
        return doc;
    }

    @Override
    public Documento getPedidoPorCodigo(String codigo) throws NotFoundException {
        try {
            URI uri = new URIBuilder().setScheme("http")
                    .setHost(baseUrl)
                    .setPath(VENTA_URL_PATH + "/" + codigo).build();
            String content = getUrl(uri);
            if (content == null) {
                throw new NotFoundException(String.format("Pedido numero %s no encontrado", codigo));
            }
            JsonObject documentObject = gson.fromJson(content, JsonObject.class);
            return parseDocumento(documentObject);
        }
        catch (URISyntaxException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public void commitDocument(int docId) {
        URI uri = null;
        try {
            uri = new URIBuilder().setScheme("http")
                .setHost(baseUrl)
                .setPath(String.format("%s/%d", FACTURA_URL_PATH, docId))
                .build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        HttpPut req = new HttpPut(uri);
        try (CloseableHttpResponse response = httpClient.execute(req)) {
            if (response.getStatusLine().getStatusCode() == 200) {
                return;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Usuario authenticate(String username, String password) {
        try {
            URI uri = new URIBuilder().setScheme("http")
                    .setHost(baseUrl)
                    .setPath(LOGIN_URL).build();
            HttpPost req = new HttpPost(uri);
            req.setConfig(timeoutConfig);
            NameValuePair[] params = new NameValuePair[]{
                new BasicNameValuePair("username", username),
                new BasicNameValuePair("password", password),
            };
            req.setEntity(new UrlEncodedFormEntity(Arrays.asList(params)));
            try (CloseableHttpResponse response = httpClient.execute(req)) {
                HttpEntity entity = response.getEntity();
                String result = streamToString(entity.getContent());
                System.out.println(result);
                JsonObject obj = new Gson().fromJson(result, JsonObject.class);
                boolean status = obj.get("status").getAsBoolean();
                if (status) {
                    Usuario user = new Usuario();
                    user.setNombre(username);
                    user.setBodega(obj.get("bodega_factura_id").getAsInt());
                    user.setLastFactura(obj.get("last_factura").getAsInt());
                    return user;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
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

    public List<Item> getItems(String url, int num) {
        String finalurl = String.format(url, num);
        URI uri;
        try {
            uri = new URIBuilder().setScheme("http")
                                              .setHost("localhost:8080")
                                              .setPath(finalurl)
                                              .build();
        }
        catch (URISyntaxException ex) {
            ex.printStackTrace();
            return null;
        }
        String content = getUrl(uri);
        if (content == null) {
            return null;
        }
        JsonObject json = gson.fromJson(content, JsonObject.class); 
        JsonArray items = json.get("items").getAsJsonArray();
        List<Item> result = new ArrayList<>();
        for (JsonElement e : items) {
            JsonObject obj = e.getAsJsonObject();
            Item item = gson.fromJson(obj.toString(), Item.class);
            if (item != null) {
                result.add(item);
            }
        }
        return result;
    }
}

