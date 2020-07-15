package cl.gesvita.ws.obtenernotificacioneshead;

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
 * Servlet implementation class ObtenerNotificacionesHead
 */
@WebServlet("/WSObtenerNotificacionesHead/ObtenerNotificacionesHead")
public class ObtenerNotificacionesHead extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(ObtenerNotificacionesHead.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ObtenerNotificacionesHead() {
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
"ID_NOTIFICACION","I","NOMBRE_ALERTA","S","TIPO_ALERTA","I","NOMBRE_TIPO","S","DESCRIPCION_TIPO","S","DESCRIPCION_ALERTA","S","MODULO_ORIGEN","S","SUBMODULO_ORIGEN","S","NOMBRE_USUARIO","S","FECHA_ALERTA","D","SEVERIDAD_ALERTA","S","ID_EVENTO","I","ID_ALERTA","I","FECHA_ALERTA_FMT","S","FECHA_EVENTO_FMT","S","FECHA_ENVIO_FMT","S","NOMBRE_PROYECTO","S","ID_PROYECTO","I","FECHA_PLAN_INI_FMT","S","FECHA_PLAN_FIN_FMT","S","FECHA_REAL_INI_FMT","S","FECHA_REAL_FIN_FMT","S","NOMBRE_JEFEPROYECTO","S","NOMBRE_AREA","S","DESCRIPCION_TIPO_PROYECTO","S","NOMBRE_MAGNITUD","S","NOMBRE_PRIORIDAD","S","NOMBRE_ESTADO_PROYECTO","S","NOMBRE_PRESUPUESTO","S","FECHA_CREACION","S","NOMBRE_TAREA","S","ID_TAREA","I","NOMBRE_ESTADO_TAREA","S","URL_FORMULARIO","S","ID_USUARIO_RESPONSABLE","I","CODI_USUARIO_RESPONSABLE","S","ID_USUARIO_EJECUTOR","I","CODI_USUARIO_EJECUTOR","S","OBSERVACION_ACTUAL_TAREA","S","AVANCE_REAL","I"
            };
            // Llamada a la BD
            String json = ObtenerLib.getDefaultObtenerResoultSetExt(logger, connection,
                    camposytipos, "VW_NOTIFICACION_OPERAHEAD" , jsonObj);
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
