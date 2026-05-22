package mx.unison.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTable;
import java.awt.Color;
import java.awt.Font;

/** Estilos compartidos para mantener identidad visual Unison en toda la aplicacion. */
final class UiStyles {
    static final Color AZUL = new Color(0x00529e);
    static final Color AZUL_OSCURO = new Color(0x015294);
    static final Color DORADO = new Color(0xf8bb00);
    static final Color NEGRO_BOTON = new Color(0x111111);
    static final Color FONDO = new Color(0xf5f7fb);
    static final Font TEXTO = new Font("Segoe UI", Font.PLAIN, 14);
    static final Font TITULO = new Font("Segoe UI", Font.BOLD, 24);

    private UiStyles() {
    }

    static void button(JButton button) {
        button.setFont(TEXTO);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBackground(NEGRO_BOTON);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1, true));
    }

    static void secondaryButton(JButton button) {
        button(button);
        button.setBackground(DORADO);
        button.setForeground(Color.BLACK);
    }

    static void input(JComponent input) {
        input.setFont(TEXTO);
        input.setBorder(BorderFactory.createLineBorder(AZUL_OSCURO, 1, true));
    }

    static void table(JTable table) {
        table.setFont(TEXTO);
        table.getTableHeader().setFont(TEXTO.deriveFont(Font.BOLD));
        table.getTableHeader().setBackground(AZUL);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setRowHeight(28);
    }
}
