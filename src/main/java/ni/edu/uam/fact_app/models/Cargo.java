package ni.edu.uam.fact_app.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.ref.PhantomReference;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Cargo {
    private Integer id;
    private String nombre;
    private  String descripcion;
}
