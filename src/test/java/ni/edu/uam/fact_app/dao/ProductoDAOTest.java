package ni.edu.uam.fact_app.dao;

import ni.edu.uam.fact_app.models.Categoria;
import ni.edu.uam.fact_app.models.Producto;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductoDAOTest {

    private static final ProductoDAO dao = new ProductoDAO();
    private static final CategoriaDAO catDao = new CategoriaDAO();

    private static Categoria categoria;

    @BeforeAll
    static void setUp() {
        categoria = new Categoria(null, "TestJUnit_CatProd", true);
        catDao.guardar(categoria);
        assertNotNull(categoria.getId());
    }

    @AfterAll
    static void tearDown() {
        if (categoria != null && categoria.getId() != null) {
            catDao.eliminar(categoria.getId());
        }
    }

    private Producto nuevo(String codigo) {
        return new Producto(
                null,
                codigo,
                "Producto " + codigo,
                categoria,
                new BigDecimal("99.50"),
                10,
                null,
                true
        );
    }

    @Test @Order(1)
    void guardarYListarConJoin() {
        Producto p = nuevo("TEST-JUNIT-001");
        dao.guardar(p);
        assertNotNull(p.getId());

        List<Producto> lista = dao.listar();
        Producto recuperado = lista.stream()
                .filter(x -> x.getId().equals(p.getId()))
                .findFirst()
                .orElse(null);

        assertNotNull(recuperado);
        assertNotNull(recuperado.getCategoria());
        assertEquals("TestJUnit_CatProd", recuperado.getCategoria().getNombre());

        dao.eliminar(p.getId());
    }

    @Test @Order(2)
    void buscarPorId() {
        Producto p = nuevo("TEST-JUNIT-002");
        dao.guardar(p);

        Producto encontrado = dao.buscar(p.getId());
        assertNotNull(encontrado);
        assertEquals("TEST-JUNIT-002", encontrado.getCodigo());
        assertEquals(0, encontrado.getPrecioVenta().compareTo(new BigDecimal("99.50")));
        assertNotNull(encontrado.getCategoria());

        dao.eliminar(p.getId());
    }

    @Test @Order(3)
    void actualizar() {
        Producto p = nuevo("TEST-JUNIT-003");
        dao.guardar(p);

        p.setNombre("Producto Modificado");
        p.setExistencia(99);
        p.setPrecioVenta(new BigDecimal("150.00"));
        dao.actualizar(p);

        Producto recargado = dao.buscar(p.getId());
        assertEquals("Producto Modificado", recargado.getNombre());
        assertEquals(99, recargado.getExistencia());
        assertEquals(0, recargado.getPrecioVenta().compareTo(new BigDecimal("150.00")));

        dao.eliminar(p.getId());
    }

    @Test @Order(4)
    void eliminar() {
        Producto p = nuevo("TEST-JUNIT-004");
        dao.guardar(p);
        int id = p.getId();

        dao.eliminar(id);

        assertNull(dao.buscar(id));
    }
}