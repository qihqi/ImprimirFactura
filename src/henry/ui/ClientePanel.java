package henry.ui;

import lombok.Getter;
import lombok.Setter;
import henry.api.FacturaInterface;
import henry.api.SearchEngine;
import henry.model.BaseModel;
import henry.model.Cliente;
import henry.model.Observable;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class ClientePanel extends JPanel implements BaseModel.Listener {

	private static final long serialVersionUID = 8993493064487143324L;
    private JButton buscar;
	private JLabel label;
	private JTextField codigo;
	private JTextField nombre;
	private JCheckBox general;

    private Observable<Cliente> cliente;
    private FacturaInterface api;

    private SearchDialog<Cliente> searchDialog;

    private class LoadCliente implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String cod = codigo.getText().trim();
            loadCliente(cod);
        }
    }

    private class LoadGeneral implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            loadCliente("NA");
        }
    }

    public void showSearchDialog() {
        searchDialog.setVisible(true);
        Cliente result = searchDialog.getResult();
        bindCliente(result);
    }

	private void initUI() {

		buscar = new JButton();
		buscar.setText("Bus");
        buscar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showSearchDialog();
            }
        });

		label = new JLabel();
		label.setText("Cliente");

		codigo = new JTextField();
		codigo.setText("");
		codigo.addActionListener(new LoadCliente());

		nombre = new JTextField();
		nombre.setText("");

		general = new JCheckBox();
		general.setText("Cliente General");
        general.addActionListener(new LoadGeneral());

		setLayout(new MigLayout("", "[][][][]", "[]"));

		add(label, "cell 0 0");
		add(codigo, "cell 1 0, width 50:200:");
		add(buscar, "cell 3 0,gapx unrelated");
		add(nombre, "cell 4 0, width 100:500:");
		add(general, "cell 1 1");
	}

	public ClientePanel(FacturaInterface api, SearchDialog<Cliente> dialog) {
        this.cliente = new Observable<>();
        this.cliente.addListener(this);
        this.searchDialog = dialog;
        this.api = api;
		initUI();
	}

    @Override
    public void onDataChanged() {
        Cliente c = cliente.getRef();
        if (c != null) {
            codigo.setText(c.getCodigo());
            nombre.setText(c.toString());
        } else {
            clear();
        }
    }

    private void loadCliente(String cod) {
        System.out.println("ClientePanel::loadCliente");
        try {
            bindCliente(api.getClientePorCodigo(cod));
        } catch (FacturaInterface.NotFoundException e) {
            codigo.requestFocus();
            codigo.selectAll();
        }
    }

    public void bindCliente(Cliente newCliente) {
        if (newCliente == null) {
            return;
        }
        cliente.setRef(newCliente);
        cliente.notifyListeners();
    }

    public Cliente getCliente() {
        return cliente.getRef();
    }

    public void clear() {
        codigo.setText("");
        nombre.setText("");
    }
}
