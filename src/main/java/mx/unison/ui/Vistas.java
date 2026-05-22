package mx.unison.ui;

import mx.unison.db.Database;
import mx.unison.model.Usuario;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.CardLayout;

/** Ventana unica de la aplicacion; toda navegacion ocurre con CardLayout. */
public class Vistas extends JFrame {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel container = new JPanel(cardLayout);
    private final Database db = new Database();
    private Usuario usuarioActual;
    private Home home;
    private PanelProductos productos;
    private AlmacenesPanel almacenes;

    public Vistas() {
        setTitle("Sistema de Inventario - Unison");
        setSize(1120, 740);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        Login login = new Login(db, this::showHome);
        home = new Home(() -> showPanel("PRODUCTOS"), () -> showPanel("ALMACENES"));
        productos = new PanelProductos(db, () -> showPanel("INICIO"), () -> usuarioActual);
        almacenes = new AlmacenesPanel(db, () -> showPanel("INICIO"), () -> usuarioActual);

        container.add(login, "LOGIN");
        container.add(home, "INICIO");
        container.add(productos, "PRODUCTOS");
        container.add(almacenes, "ALMACENES");

        add(container);
        cardLayout.show(container, "LOGIN");
    }

    private void showHome(Usuario usuario) {
        usuarioActual = usuario;
        home.setUsuario(usuario);
        productos.reload();
        almacenes.reload();
        cardLayout.show(container, "INICIO");
    }

    private void showPanel(String name) {
        productos.reload();
        almacenes.reload();
        cardLayout.show(container, name);
    }
}
