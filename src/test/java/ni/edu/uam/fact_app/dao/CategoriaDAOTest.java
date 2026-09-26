package ni.edu.uam.fact_app.dao;

import ni.edu.uam.fact_app.models.Categoria;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CategoriaDAOTest {

    private static final CategoriaDAO dao = new CategoriaDAO();

    @Test @Order(1)
    void guardarYListar() {
        Categoria nueva = new Categoria(null, "Test_JUnit_Guardar", true);
        dao.guardar(nueva);
        assertNotNull(nueva.getId(), "Postgres debió asignar un id");

        List<Categoria> lista = dao.listar();
        assertTrue(lista.stream().anyMatch(c -> c.getId().equals(nueva.getId())));

        dao.eliminar(nueva.getId());
    }

    @Test @Order(2)
    void buscarPorId() {
        Categoria nueva = new Categoria(null, "Test_JUnit_Buscar", true);
        dao.guardar(nueva);

        Categoria encontrada = dao.buscar(nueva.getId());
        assertNotNull(encontrada);
        assertEquals("Test_JUnit_Buscar", encontrada.getNombre());

        dao.eliminar(nueva.getId());
    }

    @Test @Order(3)
    void actualizar() {
        Categoria nueva = new Categoria(null, "Test_JUnit_Actualizar", true);
        dao.guardar(nueva);

        nueva.setNombre("Test_JUnit_Actualizado");
        nueva.setActivo(false);
        dao.actualizar(nueva);

        Categoria recargada = dao.buscar(nueva.getId());
        assertEquals("Test_JUnit_Actualizado", recargada.getNombre());
        assertFalse(recargada.isActivo());

        dao.eliminar(nueva.getId());
    }

    @Test @Order(4)
    void eliminar() {
        Categoria nueva = new Categoria(null, "Test_JUnit_Eliminar", true);
        dao.guardar(nueva);
        int id = nueva.getId();

        dao.eliminar(id);

        assertNull(dao.buscar(id), "Después de eliminar, buscar() debe devolver null");
    }
}