package cl.gesvita.ws.obtenerreporteencuestacovid;

import cl.gesvita.ws.obtener.exception.GesvitaException;
import cl.gesvita.ws.obtener.lib.ObtenerLib;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.JsonObject;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.log4j.Logger;

/**
 * Servlet implementation class ObtenerReporteEncuestaCovid
 */
@WebServlet("/WSObtenerReporteEncuestaCovid/ObtenerReporteEncuestaCovid")
public class ObtenerReporteEncuestaCovid extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(ObtenerReporteEncuestaCovid.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ObtenerReporteEncuestaCovid() {
        super();
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        DataSource datasource;
        Connection connection = null;
        JsonObject jsonObj;
        response.setHeader("access-control-allow-origin", "*");
        try {
            // Inicializar Log4J
            ObtenerLib.setLogParam(this.getClass());
            // Obtener Data Source
            datasource = ObtenerLib.getDataSource(logger);
        } catch (GesvitaException ex) {
            logger.error(ex.getMessage());
            out.print("{\"resultadoEjecucion\":\"NOK\",\"mensaje\":\"Ocurrio un error\",\"mensajeTec\":\"Error en conexión a BD\"}");
            return;
        }
        try {
            // Leer la entrada
            jsonObj = ObtenerLib.readInput(logger, request);
        } catch (GesvitaException ex) {
            logger.error(ex.getMessage());
            out.print("{\"resultadoEjecucion\":\"NOK\",\"mensaje\":\"Ocurrio un error\",\"mensajeTec\":\"Error de parseo\"}");
            return;
        }
        // Realizar la consulta
        try {
            // Preparar la consulta
            connection = datasource.getConnection();
            String[] camposytipos = {
"CODI_USUARIO","S","NOMBRE_PERSONA","S","RUT_USUARIO","S","NOMBRE_ENCUESTA","S","NMRO_ENCUESTA","I","ESTADO_ENCUESTA","S","FECHA_EJECUCION","D","HA_TENIDO_COVID","S","CUANDO_COVID","S","LUGAR_EXAMEN","S","CHARLA_QB2","S","CUANDO_QB2","S","CONTACTO_CONFIRMADO_COVID","S","FECHA_CONTACTO_CONFIRMADO","S","RELACION_CONTACTO_CONFIRMADO","S","TUVO_SOSPECHA_COVID","S","HIZO_EXMAEN","S","CERCANO_ESPERA_RESULTADO","S","QUIEN_ESPERA","S","REALIZO_CUARENTENA","S","MOTIVO_CUARENTENA","S","VACUNA_2020","S","FECHA_VACUNA","S","LUGAR_VACUNA","S","CONVIVE_ANCIANOS_MENORES","S","FECHA_CONVIVE","S","QUIEN_CONVIVE","S","CNTCTO_AREA_SALUD","S","FECHA_CNTCTO_SALUD","S","QUIEN_CNTCTO_SALUD","S","ENF_CRONICA","S","QUE_ENFERMEDAD","S","TOMA_MEDICAMENTOS","S","QUE_MEDICAMENTOS","S","SALIDO_PAIS","S","LUGAR_SALIDA","S","CONDUCTAS_PREVENTIVAS","S","PORQUE","S","A_TOS","S","A_CUANDO","S","B_FIEBRE","S","B_CUANDO","S","C_DIFICULTAD_RESPIRATORIA","S","C_CUANDO","S","D_DOLOR_CABEZA","S","D_CUANDO","S","E_DOLOR_GARGANTA","S","E_CUANDO","S","F_SECRESION_NAZAL","S","F_CUANDO","S","G_DOLOR_MUS_ART","S","G_CUANDO","S","H_NAUS_VOMIT","S","H_CUANDO","S","I_PERDIDA_OLFATO","S","I_CUANDO","S","J_PERDIDA_GUSTO","S","J_CUANDO","S","K_MANCHAS_PIEL","S","K_CUANDO","S","ATENCIO_MEDICAMENTOS","S","DETALLE","S","N_SERIE","S"
            };
            // Llamada a la BD
            String json = ObtenerLib.getDefaultObtenerResoultSetExt(logger, connection,
                    camposytipos, "VW_REPORTE_ENCUESTA_COVID" , jsonObj);
            // Imprimir la salida
            out.print(json);
        } catch (SQLException e) {
            logger.error("Error durante la consulta a la base de datos. ",e.fillInStackTrace());
            out.print("{\"resultadoEjecucion\":\"NOK\",\"mensaje\":\"Ocurrio un error\",\"mensajeTec\":\"SQLException\"}");
        } catch (GesvitaException ex) {
            logger.error("Error de entrada : " + ex.getMessage());
            String salidaNOK = "{\"resultadoEjecucion\":\"NOK\",\"mensaje\":\"Ocurrio un error\",\"mensajeTec\":\"Error en consulta a BD\"}";
            out.print(salidaNOK);
        } finally {
            try {
                if (connection!= null && !connection.isClosed()){
                    connection.close();
                }
            } catch (SQLException sqle) {
                logger.error(sqle.getMessage(), sqle.fillInStackTrace());
            }
            try {
                datasource.close();
            } catch (Exception e) {
                logger.error(e.getMessage(), e.fillInStackTrace());
            }
        }
    }
}
