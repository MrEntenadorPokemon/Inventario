package mx.unison.ui;

import mx.unison.db.Database;
import mx.unison.model.Almacen;
import mx.unison.model.Producto;
import mx.unison.model.Usuario;

import javax.swing.JButton;
import javax.swing.JComboBox;
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
import java.util.List;
import java.util.function.Supplier;

/** Panel de mantenimiento de productos con filtros y CRUD completo. */
public class PanelProductos extends JPanel {
    private final Database db;
    private final Runnable onGoBack;
    private final Supplier<Usuario> usuarioSupplier;
    private final DefaultTableModel model;
    private final JTable table;
    private final JTextField filter = new JTextField(20);

    public PanelProductos(Database db, Runnable onGoBack, Supplier<Usuario> usuarioSupplier) {
        this.db = db;
        this.onGoBack = onGoBack;
        this.usuarioSupplier = usuarioSupplier;
        setLayout(new BorderLayout(10, 10));
        setBackground(UiStyles.FONDO);
        model = new DefaultTableModel(new Object[]{"ID", "Nombre", "Descripcion", "Cantidad", "Precio", "Almacen", "Creado", "Ult.Mod", "Ult.Usuario"}, 0) {
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
        edit.addActionListener(e -> selectedProductoAction(this::openForm));
        del.addActionListener(e -> selectedProductoAction(this::deleteProducto));
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
        for (Producto p : db.listProductos()) {
            model.addRow(new Object[]{p.id, p.nombre, p.descripcion, p.cantidad, p.precio, p.almacenNombre, p.fechaCreacion, p.fechaModificacion, p.ultimoUsuario});
        }
        applyFilter();
    }

    private void applyFilter() {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(filter.getText())));
    }

    private void selectedProductoAction(java.util.function.Consumer<Producto> action) {
        int r = table.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto.");
            return;
        }
        int id = (int) model.getValueAt(table.convertRowIndexToModel(r), 0);
        action.accept(db.findProducto(id));
    }

    private void deleteProducto(Producto p) {
        int opt = JOptionPane.showConfirmDialog(this, "¿Seguro que desea eliminar el producto?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            db.deleteProducto(p.id);
            reload();
        }
    }

    private void openForm(Producto original) {
        Usuario usuario = usuarioSupplier.get();
        if (usuario == null || (!"ADMIN".equals(usuario.rol) && !"PRODUCTOS".equals(usuario.rol))) {
            JOptionPane.showMessageDialog(this, "Su rol no permite modificar productos.");
            return;
        }
        JTextField nombre = new JTextField(original == null ? "" : original.nombre);
        JTextField descripcion = new JTextField(original == null ? "" : original.descripcion);
        JTextField cantidad = new JTextField(original == null ? "0" : String.valueOf(original.cantidad));
        JTextField precio = new JTextField(original == null ? "0" : String.valueOf(original.precio));
        JComboBox<AlmacenItem> almacenes = new JComboBox<>();
        almacenes.addItem(new AlmacenItem(0, "Sin almacen"));
        for (Almacen a : db.listAlmacenes()) {
            almacenes.addItem(new AlmacenItem(a.id, a.nombre));
            if (original != null && original.almacenId == a.id) almacenes.setSelectedIndex(almacenes.getItemCount() - 1);
        }
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Nombre")); form.add(nombre);
        form.add(new JLabel("Descripcion")); form.add(descripcion);
        form.add(new JLabel("Cantidad")); form.add(cantidad);
        form.add(new JLabel("Precio")); form.add(precio);
        form.add(new JLabel("Almacen")); form.add(almacenes);
        int opt = JOptionPane.showConfirmDialog(this, form, original == null ? "Agregar producto" : "Modificar producto", JOptionPane.OK_CANCEL_OPTION);
        if (opt != JOptionPane.OK_OPTION) return;
        Producto p = original == null ? new Producto() : original;
        p.nombre = nombre.getText().trim();
        p.descripcion = descripcion.getText().trim();
        p.cantidad = Integer.parseInt(cantidad.getText().trim());
        p.precio = Double.parseDouble(precio.getText().trim());
        p.almacenId = ((AlmacenItem) almacenes.getSelectedItem()).id;
        if (original == null) db.insertProducto(p, usuario.nombre);
        else db.updateProducto(p, usuario.nombre);
        reload();
    }

    private JButton button(String text, boolean primary) {
        JButton button = new JButton(text);
        if (primary) UiStyles.button(button);
        else UiStyles.secondaryButton(button);
        return button;
    }

    private record AlmacenItem(int id, String nombre) {
        public String toString() { return nombre; }
    }
}
