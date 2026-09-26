package ni.edu.uam.fact_app.util;

import ni.edu.uam.fact_app.dao.CategoriaDAO;
import ni.edu.uam.fact_app.models.Categoria;

import java.util.List;

public class CategoriaValidador implements Validador<Categoria> {

    private final CategoriaDAO dao;

    public CategoriaValidador() {
        this(new CategoriaDAO());
    }

    public CategoriaValidador(CategoriaDAO dao) {
        this.dao = dao;
    }

    @Override
    public ResultadoValidacion validar(Categoria categoria) {
        if (categoria == null || categoria.getNombre() == null || categoria.getNombre().isBlank()) {
            return ResultadoValidacion.error("El nombre de la categoría no puede estar vacío.");
        }

        String nombreNuevo = categoria.getNombre().trim().toLowerCase();
        List<Categoria> existentes = dao.listar();   // ← antes: DataRepository.getCategorias()

        for (Categoria cat : existentes) {
            if (categoria.getId() != null && categoria.getId().equals(cat.getId())) {
                continue;
            }

            String existente = cat.getNombre().toLowerCase();

            if (existente.equals(nombreNuevo)) {
                return ResultadoValidacion.error(
                        "Ya existe una categoría registrada con el nombre: '" + cat.getNombre() + "'.");
            }

            boolean esPluralOSingular = existente.equals(nombreNuevo + "s")
                    || nombreNuevo.equals(existente + "s");
            boolean esSubcadena = (nombreNuevo.length() > 3 && existente.length() > 3)
                    && (existente.contains(nombreNuevo) || nombreNuevo.contains(existente));

            if (esPluralOSingular || esSubcadena) {
                return ResultadoValidacion.advertencia(
                        "Existe una categoría similar llamada '" + cat.getNombre() + "'. ¿Desea continuar?");
            }
        }

        return ResultadoValidacion.exito();
    }
}