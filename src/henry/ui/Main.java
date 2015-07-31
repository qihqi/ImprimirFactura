package henry.ui;

import henry.model.Documento;

import javax.swing.JFrame;
import java.awt.EventQueue;

@SuppressWarnings("serial")
public class Main extends JFrame {
    private LoginPane contentPane;
    private static String configpath;

    public static void main(String[] args) {
        if (args.length > 0) {
            configpath = args[0];
        }
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    Main frame = new Main();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        System.out.println("main is exiting");
    }

    public Main() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 393, 200);
        contentPane = new LoginPane(configpath);
        //aqui le puse el listener
        setContentPane(contentPane);
    }
}
