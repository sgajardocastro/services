package cl.gesvita.ws.subirtareasdetalle.bean;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author Felipe
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Warning implements Serializable {

    private int linea;
    private String mensaje;
}