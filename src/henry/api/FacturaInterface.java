package henry.api;

import henry.model.Cliente;
import henry.model.Documento;
import henry.model.Producto;
import henry.model.Usuario;
import henry.model.Item;

import java.util.List;

public interface FacturaInterface {
    Producto getProductoPorCodigo(String codigo) throws NotFoundException;
    List<Producto> buscarProducto(String prefijo);
    
    Cliente getClientePorCodigo(String codigo) throws NotFoundException;
    List<Cliente> buscarCliente(String prefijo);

    int guardarDocumento(Documento doc, boolean isFactura);
    Documento getPedidoPorCodigo(String codigo) throws NotFoundException;

    Usuario authenticate(String username, String password);
    void commitDocument(int docId);
    List<Item> getItems(String url, int num);

    public static class NotFoundException extends Exception {
        public NotFoundException(String message) {
            super(message);
        }
    }
    public static class ServerErrorException extends Exception {}
}
