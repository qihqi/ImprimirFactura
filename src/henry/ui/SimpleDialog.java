package henry.ui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import java.awt.Font;

@SuppressWarnings("serial")
public class SimpleDialog extends JDialog {
    private final JPanel contentPanel = new JPanel();
    private JTextArea displayText = new JTextArea(3, 15);

    public SimpleDialog() {
        super(null, Dialog.ModalityType.APPLICATION_MODAL);
        
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 333, 225);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setLayout(new FlowLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
    
        displayText.setLineWrap(true);
        displayText.setEditable(false);
        displayText.setFont(new Font("Dialog", Font.BOLD, 20));
        contentPanel.add(displayText);

        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
        getContentPane().add(buttonPane, BorderLayout.SOUTH);
        
        JButton okButton = new JButton("OK");
        okButton.setActionCommand("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SimpleDialog.this.setVisible(false);
            }
        });
    
        buttonPane.add(okButton);
        getRootPane().setDefaultButton(okButton);
    }

    void setText(String s) {
        displayText.setText(s);
        contentPanel.revalidate();
    }
}
