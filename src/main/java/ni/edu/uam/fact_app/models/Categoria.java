package ni.edu.uam.fact_app.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Categoria {
    private String id;
    private String nombre;
    private  boolean activa;

    @Override
    public String toString() {
        return nombre;
    }

}
