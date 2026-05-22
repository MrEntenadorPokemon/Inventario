package mx.unison.ui;

import mx.unison.db.Database;
import mx.unison.model.Usuario;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.util.function.Consumer;

/** Vista de inicio de sesion sin registro ni persistencia de sesion. */
public class Login extends JPanel {
    public Login(Database db, Consumer<Usuario> onLogin) {
        setLayout(new GridBagLayout());
        setBackground(UiStyles.FONDO);

        JPanel card = new JPanel();
        card.setPreferredSize(new Dimension(360, 390));
        card.setBackground(java.awt.Color.WHITE);
        card.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(UiStyles.AZUL, 2, true),
                javax.swing.BorderFactory.createEmptyBorder(24, 28, 24, 28)));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Inicio de sesion");
        title.setFont(UiStyles.TITULO);
        title.setForeground(UiStyles.AZUL);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField user = new JTextField();
        JPasswordField pass = new JPasswordField();
        UiStyles.input(user);
        UiStyles.input(pass);

        JButton loginBtn = new JButton("Iniciar sesion");
        UiStyles.secondaryButton(loginBtn);
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.setMaximumSize(new Dimension(180, 38));
        loginBtn.addActionListener(e -> {
            Usuario usr = db.authenticate(user.getText(), new String(pass.getPassword()));
            if (usr != null) {
                onLogin.accept(usr);
            } else {
                JOptionPane.showMessageDialog(this, "Credenciales invalidas", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        card.add(title);
        card.add(Box.createRigidArea(new Dimension(0, 28)));
        card.add(label("Usuario"));
        card.add(user);
        card.add(Box.createRigidArea(new Dimension(0, 14)));
        card.add(label("Contrasena"));
        card.add(pass);
        card.add(Box.createRigidArea(new Dimension(0, 28)));
        card.add(loginBtn);
        add(card);
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UiStyles.TEXTO);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }
}
