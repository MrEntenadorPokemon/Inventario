package mx.unison;

import mx.unison.db.Database;
import mx.unison.model.Producto;
import mx.unison.model.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseIntegrationTest {
    @TempDir
    Path tempDir;

    @Test
    void autenticaYGuardaDatosEnSqlite() {
        Database db = new Database(tempDir.resolve("inventario-test.db"));
        Usuario admin = db.authenticate("ADMIN", "admin23");

        assertNotNull(admin);
        assertEquals("ADMIN", admin.rol);

        int almacenId = db.insertAlmacen("Central", "Hermosillo", admin.nombre);
        Producto producto = new Producto();
        producto.nombre = "Laptop";
        producto.descripcion = "Equipo de prueba";
        producto.cantidad = 5;
        producto.precio = 1000;
        producto.almacenId = almacenId;

        int productoId = db.insertProducto(producto, admin.nombre);

        assertTrue(productoId > 0);
        assertEquals("Central", db.findProducto(productoId).almacenNombre);
        assertEquals(1, db.listProductos().size());
    }
}
