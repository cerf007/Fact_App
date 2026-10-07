package ni.edu.uam.fact_app.dao;

import ni.edu.uam.fact_app.models.Categoria;
import ni.edu.uam.fact_app.models.Producto;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductoDAOTest {

    private static final ProductoDAO dao = new ProductoDAO();
    private static final CategoriaDAO catDao = new CategoriaDAO();

    private static Categoria categoria;

    @BeforeAll
    static void setUp() throws SQLException {
        categoria = new Categoria(null, "TestJUnit_CatProd", true);
        catDao.guardar(categoria);
        assertNotNull(categoria.getId());
    }

    @AfterAll
    static void tearDown() throws SQLException {
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
    void guardarYListarConJoin() throws SQLException {
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
    void buscarPorId() throws SQLException {
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
    void actualizar() throws SQLException {
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
    void eliminar() throws SQLException {
        Producto p = nuevo("TEST-JUNIT-004");
        dao.guardar(p);
        int id = p.getId();

        dao.eliminar(id);

        assertNull(dao.buscar(id));
    }


    @Test @Order(5)
    void existeCodigoExcluyendoPropioId() throws SQLException {
        Producto p = nuevo("TEST-JUNIT-005");
        dao.guardar(p);

        assertTrue(dao.existeCodigo("TEST-JUNIT-005", null));

        assertFalse(dao.existeCodigo("TEST-JUNIT-005", p.getId()));

        assertFalse(dao.existeCodigo("TEST-JUNIT-NO-EXISTE", null));

        dao.eliminar(p.getId());
    }

    @Test @Order(6)
    void existeNombreExcluyendoPropioId() throws SQLException {
        Producto p = nuevo("TEST-JUNIT-006");
        dao.guardar(p);

        assertTrue(dao.existeNombre("Producto TEST-JUNIT-006", null));
        assertFalse(dao.existeNombre("Producto TEST-JUNIT-006", p.getId()));

        dao.eliminar(p.getId());
    }
}