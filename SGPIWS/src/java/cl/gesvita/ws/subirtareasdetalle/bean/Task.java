package cl.gesvita.ws.subirtareasdetalle.bean;

import java.io.Serializable;
import java.util.Date;
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
// @AllArgsConstructor
@ToString
public class Task implements Serializable {

    public Task(int linea, long iddb, String strId, String name, Date fechaInicio, Date fechaFin, String strPadre, String strPredecesoras) {
        this.linea = linea;
        this.iddb = iddb;
        this.strId = strId;
        this.name = name;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.strPadre = strPadre;
        this.strPredecesoras = strPredecesoras;
        this.numDependientes = 0;
    }

    private int linea;
    private long iddb;
    private long id;
    private String strId;
    private String name;
    private Date fechaInicio;
    private Date fechaFin;
    private String strPadre;
    private int padre;
    private String strPredecesoras;
    private int predecesoras[];
    private int numDependientes;
}