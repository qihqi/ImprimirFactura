package henry.ui;

import henry.api.FacturaInterface;
import henry.model.BaseModel;
import henry.model.Item;
import henry.model.Observable;
import henry.model.Producto;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static henry.Helpers.displayAsMoney;
import static henry.Helpers.displayMilesimas;

/** This Class represents a row in a nota de venta o factura
 *
 */
@SuppressWarnings("serial")
public class ItemPanel extends JPanel implements BaseModel.Listener {
    private JTextField codigo;
    private JTextField cantidad;
    private JTextField nombre;
    private JTextField precio;
    private JTextField subtotal;

    private ItemContainer parent;
    private Observable<Item> item;
    private SearchDialog<Producto> dialog;
    private FacturaInterface api;

    public ItemPanel(ItemContainer parent_, Item item, FacturaInterface api, 
                     SearchDialog<Producto> searchDialog) {
        parent = parent_;
        this.item = new Observable<>();
        this.item.setRef(item);
        this.item.addListener(this);
        this.api = api;
        this.dialog = searchDialog;
        initUI();
        if (item != null && item.getProducto() != null) {
            onDataChanged();
        }
    }

    public void addItemLister(BaseModel.Listener listener) {
        item.addListener(listener);
    }

    //this class notifies the parent about currently selected item
    private class ReFocusListener extends MouseAdapter {
        @Override
        public void  mouseClicked(MouseEvent e) {
            parent.setCurrent(ItemPanel.this);
        }
    }

    //This class gives TextField ability to select all text
    //when gain focus
    private static class HighlightFocusListener extends FocusAdapter {
        private JTextField text;
        public HighlightFocusListener(JTextField t) {
            text = t;
        }
        @Override
        public void focusGained(FocusEvent e) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    text.selectAll();
                }
            });
        }
    }

    private class ProductoLoader implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            final String code = codigo.getText();
            System.out.println("Started requesting");
            Producto prod = null;
            try {
                prod = api.getProductoPorCodigo(code);
            } catch (FacturaInterface.NotFoundException e1) {
                codigo.requestFocus();
                codigo.selectAll();
                parent.setMessage("Codigo no existe");
                return;
            }
            System.out.println("Finished requesting");
            if (prod != null) {
                item.getRef().setProducto(prod);
                item.notifyListeners();
                if (item.getRef().getCantidad() > 0) {
                    loadNextRow();
                } else {
                    cantidad.requestFocus();
                }
            } else {
                codigo.requestFocus();
            }
        }
    }

    private class CantidadUpdater implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String cantString = cantidad.getText();
            int cantReal;
            try {
                cantReal = (int) Math.round(Double.parseDouble(cantString) * 1000);
            }
            catch (NumberFormatException e1) {
                parent.setMessage("Cantidad debe ser un numero");
                return;
            }
            item.getRef().setCantidad(cantReal);
            item.notifyListeners();
            if (item.getRef().getProducto() != null) {
                // no point to notify listener now if product is not there yet
                loadNextRow();
            }
            else {
                codigo.requestFocus();
            }
        }
    }

    private void loadNextRow() {
        parent.onDataChanged();
        parent.shiftEvent(ItemPanel.this);
        parent.scrollDown();
    }

    private class SearchProducto implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            showSearchDialog();
        }
    }

    private class EraseRow implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            eraseRow();
        }
    }

    public void showSearchDialog() {
        dialog.setVisible(true);
        Producto result = dialog.getResult();
        item.getRef().setProducto(result);
        item.notifyListeners();
        if (item.getRef().getCantidad() > 0) {
            // no point to notify listener now if product is not there yet
            parent.onDataChanged();
            parent.shiftEvent(ItemPanel.this);
            parent.scrollDown();
        }
    }

    public void initUI() {
        codigo = new JTextField();
        cantidad = new JTextField();
        nombre = new JTextField();
        nombre.setEditable(false);
        precio = new JTextField();
        precio.setEditable(false);
        subtotal = new JTextField();
        subtotal.setEditable(false);

        JButton buscar = new JButton("Bus");
        buscar.setFont(new Font("Dialog", Font.BOLD, 10));
        buscar.addActionListener(new SearchProducto());
        final JButton eraserow = new JButton();
        eraserow.setText("-");
        eraserow.addActionListener(new EraseRow());
        codigo.addFocusListener(new HighlightFocusListener(codigo));
        codigo.addMouseListener(new ReFocusListener());
        codigo.addActionListener(new ProductoLoader());

        cantidad.addFocusListener(new HighlightFocusListener(cantidad));
        cantidad.addMouseListener(new ReFocusListener());
        cantidad.addActionListener(new CantidadUpdater());

        setLayout(new MigLayout());
        add(buscar, "width :30:");
        add(codigo, "width :100:");
        add(cantidad, "width :80:");
        add(nombre, "width :200:");
        add(precio, "width :80: ");
        add(subtotal, "width :100:");
        add(eraserow, "width :15:,height :20:");
    }

    public void focus() {
        codigo.requestFocus();
    }

    public void eraseRow() {
        item.getRef().setProducto(null);
        item.getRef().setCantidad(0);
        clear();
    }

    public void clear() {
        codigo.setText("");
        cantidad.setText("");
        precio.setText("");
        nombre.setText("");
        subtotal.setText("");
    }

    public void onDataChanged() {
        System.out.println("onDataChagned");
        Producto prod = item.getRef().getProducto();
        int cant= item.getRef().getCantidad();
        if (prod != null && cant >= 0) {
            subtotal.setText(displayAsMoney(item.getRef().getSubtotal()));
        }
        if (prod != null) {
            codigo.setText(prod.getCodigo());
            precio.setText(displayAsMoney(prod.getPrecio1()));
            nombre.setText(prod.getNombre());
        }
        if (cant >= 0) {
            cantidad.setText(displayMilesimas(item.getRef().getCantidad()));
        }
    }

}
