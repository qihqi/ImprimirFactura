package henry.ui;

import henry.model.BaseModel;
import henry.model.Documento;
import henry.model.Item;
import henry.model.Observable;
import net.miginfocom.swing.MigLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static henry.Helpers.displayAsMoney;
@SuppressWarnings("serial")
public class ItemContainer extends JPanel
        implements BaseModel.Listener, MessageDisplay {

    /** Tiene arreglo de itemPanel, representa los contenidos
     *  de una factura/nota
     *
     */

    private static final int DEFAULT_IVA = 12;
    private static final int VIEWABLE_ROW_COUNT = 6;

    private int current = 0; // the current selected item
    private ArrayList<ItemPanel> items;
    private Map<ItemPanel, Integer> reverseItem;

    private JPanel content;

    private JLabel lCodigo;
    private JLabel lCantidad;
    private JLabel lNombre;
    private JLabel lPrecio;
    private JLabel lSubtotal;
    private JScrollPane scroll;

    private JTextField ivaPorciento;
    private JTextField ivaValor;
    private JTextField totalValor;
    private JTextField descValor;

    private JTextField valorBruto;
    private JTextField valorNeto;
    private JLabel message;

    private Observable<Documento> documento;
    private BaseModel.Listener itemObserver;
    private ItemPanelFactory itemFactory;

    private class IvaUpdater implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
           int iva = Integer.parseInt(ivaPorciento.getText());
           documento.getRef().setIvaPorciento(iva);
           documento.notifyListeners();
        }
    }

    //-------------------------------------------------------------------------------
    public ItemContainer(boolean fact, ItemPanelFactory itemFactory) {
        super(new BorderLayout());
        this.itemFactory = itemFactory;

        items = new ArrayList<>();

        reverseItem = new HashMap<>();
        documento = new Observable<>();
        documento.setRef(new Documento());
        documento.getRef().setIvaPorciento(DEFAULT_IVA);
        documento.addListener(this);
        itemObserver = new TotalController();
        Item firstItem = new Item();
        documento.getRef().addItem(firstItem);
        initUI();
        addItemPanel(firstItem);
    }

    public void initUI() {
        System.out.println("ItemContainer::initUI");
        JPanel header = new JPanel(new MigLayout("",
                "90[]10[][][][]",""));

        lCodigo = new JLabel();
        lCodigo.setText("Codigo");

        lCantidad = new JLabel();
        lCantidad.setText("Cantidad");

        lNombre = new JLabel();
        lNombre.setText("Nombre del Producto");

        lPrecio = new JLabel();
        lPrecio.setText("Precio");

        lSubtotal = new JLabel();
        lSubtotal.setText("Subtotal");

        header.add(lCodigo, "width :100:");
        header.add(lCantidad, "width :80:");
        header.add(lNombre, "width :200:");
        header.add(lPrecio, "width :80:");
        header.add(lSubtotal, "width :100:");
        setPreferredSize(new Dimension(792, 570));

        add(header, BorderLayout.PAGE_START);

        //-----------------CONTENT--------------------------------
        content = new JPanel(new MigLayout());
        scroll = new JScrollPane(content);

        add(scroll, BorderLayout.CENTER);

        //---------TOTAL---------------------------
        JPanel totales = new JPanel();

        add(totales, BorderLayout.PAGE_END);
        totales.setLayout(new MigLayout("", "[400][right][30][][100]","[][][][][]"));

        JLabel ivaLabel = new JLabel("IVA: ");
        JLabel totalLabel = new JLabel("Total: ");
        JLabel porciento = new JLabel("%");
        JLabel netLabel = new JLabel("Valor Neto: ");
        JLabel descLabel = new JLabel("Descuento: ");
        JLabel subLabel = new JLabel("Valor Bruto: ");



        ivaPorciento = new JTextField("" + DEFAULT_IVA);
        ivaValor = new JTextField();
        ivaValor.setEditable(false);

        ivaPorciento.addActionListener(new IvaUpdater());


        message = new JLabel();
        message.setForeground(Color.red);
        totales.add(message, "cell 0 0, width :400:");

        totales.add(subLabel, "cell 1 0");

        valorBruto = new JTextField();
        valorBruto.setEditable(false);

        totales.add(valorBruto, "cell 4 0,width :100:");

        descValor = new JTextField();
        descValor.setEditable(false);

        totales.add(descLabel, "cell 1 1");
        totales.add(descValor, "cell 4 1, width :100:");

        valorNeto = new JTextField();
        valorNeto.setEditable(false);

        totales.add(netLabel, "cell 1 2");
        totales.add(valorNeto, "cell 4 2, width :100:");

        totales.add(ivaLabel, "cell 1 3");
        totales.add(ivaPorciento, "cell 2 3,width :30:");
        totales.add(porciento, "cell 3 3");
        totales.add(ivaValor, "cell 4 3,width :100:");

        totales.add(totalLabel, "cell 1 4" );
        totalValor = new JTextField();
        totalValor.setEditable(false);
        totales.add(totalValor, "cell 4 4,width :100:");
    }

    public void scrollDown() {
        JViewport vp = scroll.getViewport();
        Rectangle rect = vp.getBounds();
        rect.setLocation((int) rect.getX(), (int) rect.getY() + 100);
        vp.scrollRectToVisible(rect);
    }

    public void scrollUp() {
        JViewport vp = scroll.getViewport();
        Rectangle rect = vp.getBounds();
        rect.setLocation((int) rect.getX(),0);
        vp.scrollRectToVisible(rect);
    }


    /*
     *  Do the event when shift is updated. ie update the total
     *  and change the cursor to next line
     */
    public void shiftEvent(ItemPanel item) {
        current = reverseItem.get(item);
        getFocus();
    }

    public void getFocus() {
        int threshold = items.size() - 1;
        current++;
        System.out.println("current " + current + " thres " + threshold);
        if (current < threshold) {
            //dont need to make new one
            System.out.println("didnt add new");
            ItemPanel next = items.get(current);
            next.focus();
        }
        else if (current == threshold /*&& items.get(threshold).getProdCont() == null */ ) {
            items.get(threshold).focus();
        }
        else {
            //allocate new ones
            Item nuevoItem = new Item();
            documento.getRef().addItem(nuevoItem);
            addItemPanel(nuevoItem);

            content.revalidate();
            items.get(items.size() - 1).focus();
        }
    }

    public void setCurrent(ItemPanel item) {
        current = reverseItem.get(item).intValue();
    }

    public void addItemPanel(Item item) {

        System.out.println("ItemContainer::addItemPanel " + 
                (item.getProducto() == null ? "null" : item.getProducto().getCodigo()));
        ItemPanel itemPanel = itemFactory.make(this, item);
        items.add(itemPanel);
        itemPanel.addItemLister(itemObserver);
        reverseItem.put(itemPanel, items.size() - 1);
        content.add(itemPanel, "wrap");
    }

    public void clear() {
        //save the first itemPanel
        Item item = new Item();
        items.clear();
        content.removeAll();
        content.repaint();
        documento.setRef(new Documento());
        documento.getRef().addItem(item);
        addItemPanel(item);
        valorBruto.setText("");
        descValor.setText("");
        valorNeto.setText("");
        ivaValor.setText("");
        totalValor.setText("");
        scrollUp();
    }

    @Override
    public void onDataChanged() {
        Documento doc = documento.getRef();
        valorBruto.setText(displayAsMoney(doc.getSubtotal()));
        descValor.setText(displayAsMoney(doc.getDescuento()));
        valorNeto.setText(displayAsMoney(doc.getTotalNeto()));
        ivaValor.setText(displayAsMoney(doc.getIva()));
        totalValor.setText(displayAsMoney(doc.getTotal()));
    }

    public Documento getDocumento() {
        return documento.getRef();
    }

    public void update(Documento doc) {
        System.out.println("ItemContainer::update");
        documento.setRef(doc);
        content.removeAll();
        items.clear();
        reverseItem.clear();
        for (Item i : doc.getItems()) {
            addItemPanel(i);
        }
        addItemPanel(new Item());
        content.revalidate();
        items.get(items.size() - 1).focus();
        itemObserver.onDataChanged();

        documento.notifyListeners();
    }

    public void setMessage(String messageText) {
        message.setText(messageText);
    }

    public void triggerSearchOnLastItem() {
        ItemPanel last = items.get(items.size() - 1);
        last.showSearchDialog();
    }

    private class TotalController implements BaseModel.Listener {
        @Override
        public void onDataChanged() {
            System.out.println("TotalController::OnItemChanged");
            Documento doc = ItemContainer.this.documento.getRef();
            int subtotal = 0;
            int descuentoIndividual = 0;
            for (Item i : doc.getItems()) {
                subtotal += i.getSubtotal();
                descuentoIndividual += i.getDescuento();
                System.out.println("inner loop " + subtotal);
            }
            doc.setSubtotal(subtotal);
            doc.setDescuentoIndividual(descuentoIndividual);
        }
    }
}
