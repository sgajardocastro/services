package cl.gesvita.ws.subirpresupuestocc.bean;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Columna {
    private String campodb;
    private String nombre;
    private String tipo;
    private String defaultValue;
    private boolean obligatorio;
}
