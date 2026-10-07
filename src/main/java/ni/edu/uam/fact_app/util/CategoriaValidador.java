package ni.edu.uam.fact_app.util;

import ni.edu.uam.fact_app.dao.CategoriaDAO;
import ni.edu.uam.fact_app.models.Categoria;

import java.sql.SQLException;
import java.util.List;

public class CategoriaValidador implements Validador<Categoria> {

    private final CategoriaDAO dao;

    public CategoriaValidador() { this(new CategoriaDAO()); }
    public CategoriaValidador(CategoriaDAO dao) { this.dao = dao; }

    @Override
    public ResultadoValidacion validar(Categoria categoria) throws SQLException {
        if (categoria == null || categoria.getNombre() == null || categoria.getNombre().isBlank()) {
            return ResultadoValidacion.error(
                    "El nombre de la categoría no puede estar vacío.",
                    ResultadoValidacion.Campo.NOMBRE);
        }

        String nombreNuevo = categoria.getNombre().trim();
        String nombreLower = nombreNuevo.toLowerCase();

        if (dao.existeNombre(nombreNuevo, categoria.getId())) {
            return ResultadoValidacion.error(
                    "Ya existe una categoría registrada con el nombre: '" + nombreNuevo + "'.",
                    ResultadoValidacion.Campo.NOMBRE);
        }

        List<Categoria> existentes = dao.listar();
        for (Categoria cat : existentes) {
            if (categoria.getId() != null && categoria.getId().equals(cat.getId())) continue;

            String existente = cat.getNombre().toLowerCase();
            boolean esPluralOSingular = existente.equals(nombreLower + "s")
                    || nombreLower.equals(existente + "s");
            boolean esSubcadena = (nombreLower.length() > 3 && existente.length() > 3)
                    && (existente.contains(nombreLower) || nombreLower.contains(existente));

            if (esPluralOSingular || esSubcadena) {
                return ResultadoValidacion.advertencia(
                        "Existe una categoría similar llamada '" + cat.getNombre()
                                + "'. ¿Desea continuar?",
                        ResultadoValidacion.Campo.NOMBRE);
            }
        }

        return ResultadoValidacion.exito();
    }
}