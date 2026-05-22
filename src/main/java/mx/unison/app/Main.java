package mx.unison.app;

import mx.unison.ui.Vistas;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Punto de entrada de la aplicacion de inventario. */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            Vistas vistas = new Vistas();
            vistas.setVisible(true);
        });
    }
}
