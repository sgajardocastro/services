package cl.gesvita.ws.obtenercgeinspeccioneshallazgosv2;

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
 * Servlet implementation class ObtenerCgeInspeccionesHallazgosV2
 */
@WebServlet("/WSObtenerCgeInspeccionesHallazgosV2/ObtenerCgeInspeccionesHallazgosV2")
public class ObtenerCgeInspeccionesHallazgosV2 extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(ObtenerCgeInspeccionesHallazgosV2.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ObtenerCgeInspeccionesHallazgosV2() {
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
"ID_INSPECCION","I","ID_HALLAZGO","I","NOMBRE_INSPECION","S","ID_USUARIO","I","FECHA_INSPECCION","D","NOMBRE_PREVENCIONISTA","S","RUT_PREVENCIONISTA","S","RUT_CONTRATISTA","S","NOMBRE_CONTRATISTA","S","TIPO_EMPRESA","S","NOMBRE_MANDANTE","S","RUT_MANDANTE","S","REGION","S","ZONAL","S","DELEGACION","S","LATITUD","S","LONGITUD","S","TIPO_ACTIVIDAD","S","DESCRIPCION_ACTIVIDAD","S","HALLAZGO","S","MEDIDA_APLICADA","S","FECHA_PROPUESTA_CIERRE","S","FECHA_EFECTIVA_CIERRE","S","OBSERVACION","S","TIPO_HALLAZGO","S","POTENCIAL_HALLAZGO","S","CONTROL_ASOCIADO","S","ESTADO","S","PARALIZACION","S","TIPO_PARALIZACION","S","JEFE_FAENA","S","HORA","S","CODIGO_DE_OBRA","S"
            };
            // Llamada a la BD
            String json = ObtenerLib.getDefaultObtenerResoultSetExt(logger, connection,
                    camposytipos, "VW_INSPECCIONES_HALLAZGOS_V2" , jsonObj);
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
