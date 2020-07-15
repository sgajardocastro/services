package cl.gesvita.ws.subirgantttiendasnuevas.bean;

import java.io.Serializable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Entrada implements Serializable {
    private static final long serialVersionUID = 1123761783612786378L;
    private Float correlativo;
    private long item;
    private String area;
    private Date fInicioEst;
    private Date fTerminoEst;
    private Date fInicioReal;
    private Date fTerminoReal;
    private long difDias;
    private long lineal;
    private long lado;
    private Float pEquipamiento;
    private Float pEquipamientoAccesorios;
    private Float pEquipamientoMoviliario;
    private Float pCarpinteria;
    private Float pGrafica;
    private Float pCargaProductos;
    private Float pMicromerchandising;
    private String comentarios;
}