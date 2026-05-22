package mx.unison.model;

/** Registro de la tabla productos con metadatos de auditoria. */
public class Producto {
    public int id;
    public String nombre;
    public String descripcion;
    public int cantidad;
    public double precio;
    public int almacenId;
    public String almacenNombre;
    public String fechaCreacion;
    public String fechaModificacion;
    public String ultimoUsuario;
}
