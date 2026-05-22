package mx.unison.ui;

import mx.unison.db.Database;
import mx.unison.model.Almacen;
import mx.unison.model.Usuario;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.function.Supplier;

/** Panel de mantenimiento de almacenes con filtros y CRUD completo. */
public class AlmacenesPanel extends JPanel {
    private final Database db;
    private final Runnable onGoBack;
    private final Supplier<Usuario> usuarioSupplier;
    private final DefaultTableModel model;
    private final JTable table;
    private final JTextField filter = new JTextField(20);

    public AlmacenesPanel(Database db, Runnable onGoBack, Supplier<Usuario> usuarioSupplier) {
        this.db = db;
        this.onGoBack = onGoBack;
        this.usuarioSupplier = usuarioSupplier;
        setLayout(new BorderLayout(10, 10));
        setBackground(UiStyles.FONDO);
        model = new DefaultTableModel(new Object[]{"ID", "Nombre", "Ubicacion", "Creado", "Ult.Mod", "Ult.Usuario"}, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(model);
        UiStyles.table(table);
        initTop();
        add(new JScrollPane(table), BorderLayout.CENTER);
        reload();
    }

    private void initTop() {
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        top.setBackground(UiStyles.FONDO);
        JButton back = button("Regresar", false);
        JButton add = button("Agregar", true);
        JButton edit = button("Modificar", true);
        JButton del = button("Eliminar", true);
        back.addActionListener(e -> onGoBack.run());
        add.addActionListener(e -> openForm(null));
        edit.addActionListener(e -> selectedAlmacenAction(this::openForm));
        del.addActionListener(e -> selectedAlmacenAction(this::deleteAlmacen));
        filter.getDocument().addDocumentListener((SimpleDocumentListener) e -> applyFilter());
        UiStyles.input(filter);
        top.add(back);
        top.add(new JLabel("Filtro"));
        top.add(filter);
        top.add(add);
        top.add(edit);
        top.add(del);
        add(top, BorderLayout.NORTH);
    }

    public void reload() {
        model.setRowCount(0);
        for (Almacen a : db.listAlmacenes()) {
            model.addRow(new Object[]{a.id, a.nombre, a.ubicacion, a.fechaHoraCreacion, a.fechaHoraUltimaMod, a.ultimoUsuario});
        }
        applyFilter();
    }

    private void applyFilter() {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(filter.getText())));
    }

    private void selectedAlmacenAction(java.util.function.Consumer<Almacen> action) {
        int r = table.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un almacen.");
            return;
        }
        int id = (int) model.getValueAt(table.convertRowIndexToModel(r), 0);
        action.accept(db.findAlmacen(id));
    }

    private void deleteAlmacen(Almacen a) {
        int opt = JOptionPane.showConfirmDialog(this, "¿Seguro que desea eliminar el almacen?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            db.deleteAlmacen(a.id);
            reload();
        }
    }

    private void openForm(Almacen original) {
        Usuario usuario = usuarioSupplier.get();
        if (usuario == null || (!"ADMIN".equals(usuario.rol) && !"ALMACENES".equals(usuario.rol))) {
            JOptionPane.showMessageDialog(this, "Su rol no permite modificar almacenes.");
            return;
        }
        JTextField nombre = new JTextField(original == null ? "" : original.nombre);
        JTextField ubicacion = new JTextField(original == null ? "" : original.ubicacion);
        UiStyles.input(nombre);
        UiStyles.input(ubicacion);
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Nombre")); form.add(nombre);
        form.add(new JLabel("Ubicacion")); form.add(ubicacion);
        int opt = JOptionPane.showConfirmDialog(this, form, original == null ? "Agregar almacen" : "Modificar almacen", JOptionPane.OK_CANCEL_OPTION);
        if (opt != JOptionPane.OK_OPTION) return;
        if (original == null) db.insertAlmacen(nombre.getText().trim(), ubicacion.getText().trim(), usuario.nombre);
        else db.updateAlmacen(original.id, nombre.getText().trim(), ubicacion.getText().trim(), usuario.nombre);
        reload();
    }

    private JButton button(String text, boolean primary) {
        JButton button = new JButton(text);
        if (primary) UiStyles.button(button);
        else UiStyles.secondaryButton(button);
        return button;
    }
}
