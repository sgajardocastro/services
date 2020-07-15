package cl.gesvita.ws.obtener.bean;
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
public class TipoParametro {
    private String campo;
    private String nombre;
    private String tipo;
    private String defaultValue;
    private boolean obligatorio;
}
