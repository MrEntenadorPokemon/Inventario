package mx.unison.db;

import mx.unison.model.Almacen;
import mx.unison.model.Producto;
import mx.unison.model.Usuario;
import mx.unison.security.CryptoUtils;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Gestiona la conexion SQLite, migraciones basicas y operaciones CRUD. */
public class Database {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private final String url;

    public Database() {
        this(defaultDatabasePath());
    }

    public Database(Path databasePath) {
        this.url = "jdbc:sqlite:" + databasePath.toAbsolutePath();
        init();
    }

    private static Path defaultDatabasePath() {
        return Path.of(System.getProperty("user.dir"), "InventarioBD.db");
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(url);
    }

    /** Crea tablas y columnas requeridas sin depender de una ruta fija de Windows. */
    private void init() {
        try (Connection c = connect(); Statement st = c.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS usuarios (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "nombre TEXT NOT NULL UNIQUE, " +
                    "password TEXT NOT NULL, " +
                    "fecha_hora_ultimo_inicio TEXT, " +
                    "rol TEXT NOT NULL CHECK(rol IN ('ADMIN','PRODUCTOS','ALMACENES')))");
            st.execute("CREATE TABLE IF NOT EXISTS almacenes (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "nombre TEXT NOT NULL, " +
                    "ubicacion TEXT, " +
                    "fecha_hora_creacion TEXT DEFAULT CURRENT_TIMESTAMP, " +
                    "fecha_hora_ultima_modificacion TEXT, " +
                    "ultimo_usuario_en_modificar TEXT)");
            st.execute("CREATE TABLE IF NOT EXISTS productos (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "nombre TEXT NOT NULL, " +
                    "descripcion TEXT, " +
                    "cantidad INTEGER NOT NULL DEFAULT 0, " +
                    "precio REAL NOT NULL DEFAULT 0, " +
                    "departamento TEXT, " +
                    "almacen_id INTEGER, " +
                    "fecha_hora_creacion TEXT DEFAULT CURRENT_TIMESTAMP, " +
                    "fecha_hora_ultima_modificacion TEXT, " +
                    "ultimo_usuario_en_modificar TEXT, " +
                    "FOREIGN KEY(almacen_id) REFERENCES almacenes(id))");
            addColumnIfMissing(c, "productos", "almacen_id", "INTEGER");
            addColumnIfMissing(c, "productos", "fecha_hora_creacion", "TEXT DEFAULT CURRENT_TIMESTAMP");
            addColumnIfMissing(c, "productos", "fecha_hora_ultima_modificacion", "TEXT");
            addColumnIfMissing(c, "productos", "ultimo_usuario_en_modificar", "TEXT");
            addColumnIfMissing(c, "almacenes", "fecha_hora_creacion", "TEXT DEFAULT CURRENT_TIMESTAMP");
            addColumnIfMissing(c, "almacenes", "fecha_hora_ultima_modificacion", "TEXT");
            addColumnIfMissing(c, "almacenes", "ultimo_usuario_en_modificar", "TEXT");
            addColumnIfMissing(c, "usuarios", "fecha_hora_ultimo_inicio", "TEXT");
            if (columnExists(c, "productos", "almacen")) {
                st.execute("UPDATE productos SET almacen_id=almacen WHERE (almacen_id IS NULL OR almacen_id=0) AND almacen IS NOT NULL");
            }
            st.execute("UPDATE productos SET fecha_hora_creacion=datetime('now') WHERE fecha_hora_creacion IS NULL OR fecha_hora_creacion='' OR fecha_hora_creacion='0'");
            st.execute("UPDATE almacenes SET fecha_hora_creacion=datetime('now') WHERE fecha_hora_creacion IS NULL OR fecha_hora_creacion='' OR fecha_hora_creacion='0'");
            insertDefaultUser("ADMIN", "admin23", "ADMIN");
            insertDefaultUser("PRODUCTOS", "productos19", "PRODUCTOS");
            insertDefaultUser("ALMACENES", "almacenes11", "ALMACENES");
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo inicializar la base de datos", e);
        }
    }

    private void addColumnIfMissing(Connection c, String table, String column, String definition) throws SQLException {
        if (columnExists(c, table, column)) return;
        try (Statement st = c.createStatement()) {
            st.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
        }
    }

    private boolean columnExists(Connection c, String table, String column) throws SQLException {
        try (ResultSet rs = c.createStatement().executeQuery("PRAGMA table_info(" + table + ")")) {
            while (rs.next()) {
                if (column.equalsIgnoreCase(rs.getString("name"))) return true;
            }
        }
        return false;
    }

    private String now() {
        return LocalDateTime.now().format(FORMATTER);
    }

    private void insertDefaultUser(String nombre, String passPlain, String rol) throws SQLException {
        String sql = "INSERT OR IGNORE INTO usuarios(nombre, password, rol) VALUES(?, ?, ?)";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, CryptoUtils.md5(passPlain));
            ps.setString(3, rol);
            ps.executeUpdate();
        }
    }

    /** Valida credenciales y registra la fecha de ultimo inicio de sesion. */
    public Usuario authenticate(String nombre, String passwordPlain) {
        String sql = "SELECT nombre, rol FROM usuarios WHERE nombre=? AND password=?";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nombre.trim());
            ps.setString(2, CryptoUtils.md5(passwordPlain));
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;
            Usuario u = new Usuario();
            u.nombre = rs.getString("nombre");
            u.rol = rs.getString("rol");
            try (PreparedStatement upd = c.prepareStatement("UPDATE usuarios SET fecha_hora_ultimo_inicio=? WHERE nombre=?")) {
                upd.setString(1, now());
                upd.setString(2, u.nombre);
                upd.executeUpdate();
            }
            return u;
        } catch (SQLException e) {
            throw new IllegalStateException("Error al autenticar usuario", e);
        }
    }

    public List<Almacen> listAlmacenes() {
        List<Almacen> out = new ArrayList<>();
        String sql = "SELECT id, nombre, ubicacion, fecha_hora_creacion, fecha_hora_ultima_modificacion, ultimo_usuario_en_modificar FROM almacenes ORDER BY id";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Almacen a = new Almacen();
                a.id = rs.getInt("id");
                a.nombre = rs.getString("nombre");
                a.ubicacion = rs.getString("ubicacion");
                a.fechaHoraCreacion = rs.getString("fecha_hora_creacion");
                a.fechaHoraUltimaMod = rs.getString("fecha_hora_ultima_modificacion");
                a.ultimoUsuario = rs.getString("ultimo_usuario_en_modificar");
                out.add(a);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error al listar almacenes", e);
        }
        return out;
    }

    public Almacen findAlmacen(int id) {
        return listAlmacenes().stream().filter(a -> a.id == id).findFirst().orElse(null);
    }

    public int insertAlmacen(String nombre, String ubicacion, String usuario) {
        String sql = "INSERT INTO almacenes(nombre, ubicacion, fecha_hora_creacion, ultimo_usuario_en_modificar) VALUES(?,?,?,?)";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.setString(2, ubicacion);
            ps.setString(3, now());
            ps.setString(4, usuario);
            ps.executeUpdate();
            ResultSet g = ps.getGeneratedKeys();
            return g.next() ? g.getInt(1) : -1;
        } catch (SQLException e) {
            throw new IllegalStateException("Error al agregar almacen", e);
        }
    }

    public void updateAlmacen(int id, String nombre, String ubicacion, String usuario) {
        String sql = "UPDATE almacenes SET nombre=?, ubicacion=?, fecha_hora_ultima_modificacion=?, ultimo_usuario_en_modificar=? WHERE id=?";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, ubicacion);
            ps.setString(3, now());
            ps.setString(4, usuario);
            ps.setInt(5, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Error al modificar almacen", e);
        }
    }

    public void deleteAlmacen(int id) {
        try (Connection c = connect(); PreparedStatement clear = c.prepareStatement("UPDATE productos SET almacen_id=NULL WHERE almacen_id=?"); PreparedStatement del = c.prepareStatement("DELETE FROM almacenes WHERE id=?")) {
            clear.setInt(1, id);
            clear.executeUpdate();
            del.setInt(1, id);
            del.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Error al eliminar almacen", e);
        }
    }

    public List<Producto> listProductos() {
        List<Producto> out = new ArrayList<>();
        String sql = "SELECT p.id, p.nombre, p.descripcion, p.cantidad, p.precio, p.almacen_id, " +
                "COALESCE(a.nombre, '') AS almacen_nombre, p.fecha_hora_creacion, " +
                "p.fecha_hora_ultima_modificacion, p.ultimo_usuario_en_modificar " +
                "FROM productos p LEFT JOIN almacenes a ON p.almacen_id = a.id ORDER BY p.id";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Producto p = new Producto();
                p.id = rs.getInt("id");
                p.nombre = rs.getString("nombre");
                p.descripcion = rs.getString("descripcion");
                p.cantidad = rs.getInt("cantidad");
                p.precio = rs.getDouble("precio");
                p.almacenId = rs.getInt("almacen_id");
                p.almacenNombre = rs.getString("almacen_nombre");
                p.fechaCreacion = rs.getString("fecha_hora_creacion");
                p.fechaModificacion = rs.getString("fecha_hora_ultima_modificacion");
                p.ultimoUsuario = rs.getString("ultimo_usuario_en_modificar");
                out.add(p);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error al listar productos", e);
        }
        return out;
    }

    public Producto findProducto(int id) {
        return listProductos().stream().filter(p -> p.id == id).findFirst().orElse(null);
    }

    public int insertProducto(Producto prod, String usuario) {
        String sql = "INSERT INTO productos(nombre, descripcion, cantidad, precio, almacen_id, departamento, fecha_hora_creacion, ultimo_usuario_en_modificar) VALUES(?,?,?,?,?,?,?,?)";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindProducto(ps, prod);
            ps.setString(6, "General");
            ps.setString(7, now());
            ps.setString(8, usuario);
            ps.executeUpdate();
            ResultSet g = ps.getGeneratedKeys();
            return g.next() ? g.getInt(1) : -1;
        } catch (SQLException e) {
            throw new IllegalStateException("Error al agregar producto", e);
        }
    }

    public void updateProducto(Producto prod, String usuario) {
        String sql = "UPDATE productos SET nombre=?, descripcion=?, cantidad=?, precio=?, almacen_id=?, fecha_hora_ultima_modificacion=?, ultimo_usuario_en_modificar=? WHERE id=?";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            bindProducto(ps, prod);
            ps.setString(6, now());
            ps.setString(7, usuario);
            ps.setInt(8, prod.id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Error al modificar producto", e);
        }
    }

    private void bindProducto(PreparedStatement ps, Producto prod) throws SQLException {
        ps.setString(1, prod.nombre);
        ps.setString(2, prod.descripcion);
        ps.setInt(3, prod.cantidad);
        ps.setDouble(4, prod.precio);
        if (prod.almacenId > 0) ps.setInt(5, prod.almacenId);
        else ps.setNull(5, Types.INTEGER);
    }

    public void deleteProducto(int id) {
        String sql = "DELETE FROM productos WHERE id=?";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Error al eliminar producto", e);
        }
    }
}
