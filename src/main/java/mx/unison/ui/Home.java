package mx.unison.ui;

import mx.unison.model.Usuario;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

/** Vista inicial con navegacion interna hacia productos y almacenes. */
public class Home extends JPanel {
    private final JLabel userLabel = new JLabel();

    public Home(Runnable onOpenProductos, Runnable onOpenAlmacenes) {
        setLayout(new BorderLayout());
        setBackground(UiStyles.FONDO);

        JPanel center = new JPanel();
        center.setBackground(UiStyles.FONDO);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(javax.swing.BorderFactory.createEmptyBorder(70, 60, 60, 60));

        JLabel logo = new JLabel("UNISON");
        logo.setFont(UiStyles.TITULO.deriveFont(34f));
        logo.setForeground(UiStyles.DORADO);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Sistema Basico de Inventario");
        title.setFont(UiStyles.TITULO);
        title.setForeground(UiStyles.AZUL);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        userLabel.setFont(UiStyles.TEXTO);
        userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnProd = new JButton("Productos");
        JButton btnAlm = new JButton("Almacenes");
        UiStyles.button(btnProd);
        UiStyles.button(btnAlm);
        btnProd.setMaximumSize(new Dimension(220, 38));
        btnAlm.setMaximumSize(new Dimension(220, 38));
        btnProd.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnAlm.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnProd.addActionListener(e -> onOpenProductos.run());
        btnAlm.addActionListener(e -> onOpenAlmacenes.run());

        center.add(logo);
        center.add(Box.createRigidArea(new Dimension(0, 12)));
        center.add(title);
        center.add(Box.createRigidArea(new Dimension(0, 8)));
        center.add(userLabel);
        center.add(Box.createRigidArea(new Dimension(0, 32)));
        center.add(btnProd);
        center.add(Box.createRigidArea(new Dimension(0, 12)));
        center.add(btnAlm);
        add(center, BorderLayout.CENTER);
    }

    public void setUsuario(Usuario usuario) {
        String alumno = "ADMIN".equals(usuario.nombre) ? "ADMIN" : "----";
        userLabel.setText("Alumno: " + alumno + " | Usuario: " + usuario.nombre + " | Rol: " + usuario.rol);
    }
}
