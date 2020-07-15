package cl.gesvita.ws.obtener.bean;

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
public class EntradaCapex {
    double id;
    String partida;
    long presupuesto;
    long gastoreal;
    long saldo;
    double porcentaje;
}
