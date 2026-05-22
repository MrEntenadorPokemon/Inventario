package mx.unison.service;

import mx.unison.model.Producto;

import java.util.List;

/** Operaciones de negocio reutilizables para calculos de inventario. */
public class InventoryService {
    /**
     * Calcula el valor total de inventario sumando cantidad por precio.
     *
     * @param productos registros actualmente cargados desde SQLite.
     * @return valor monetario total del inventario.
     */
    public double calcularValorTotal(List<Producto> productos) {
        return productos.stream()
                .mapToDouble(p -> p.cantidad * p.precio)
                .sum();
    }
}
