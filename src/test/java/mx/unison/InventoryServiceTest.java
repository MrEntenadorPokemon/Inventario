package mx.unison;

import mx.unison.model.Producto;
import mx.unison.service.InventoryService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InventoryServiceTest {
    @Test
    void calculaValorTotalDelInventario() {
        Producto a = new Producto();
        a.cantidad = 3;
        a.precio = 20.5;
        Producto b = new Producto();
        b.cantidad = 2;
        b.precio = 10;

        assertEquals(81.5, new InventoryService().calcularValorTotal(List.of(a, b)));
    }
}
