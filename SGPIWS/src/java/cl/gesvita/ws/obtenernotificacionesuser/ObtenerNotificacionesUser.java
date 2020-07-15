package cl.gesvita.ws.obtenernotificacionesuser;

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
 * Servlet implementation class ObtenerNotificacionesUser
 */
@WebServlet("/WSObtenerNotificacionesUser/ObtenerNotificacionesUser")
public class ObtenerNotificacionesUser extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(ObtenerNotificacionesUser.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ObtenerNotificacionesUser() {
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
                "ID_ALERTA_ENVIO","I","ID_NOTIFICACION","I","ID_USUARIO","I","ID_DACCION","I",
                "CODI_GRUPO_USUARIO","S","CORREO_USUARIO","S","FECHA_ENVIO_USUARIO","D",
                "FECHA_LECTURA_USUARIO","D","ESTADO_LECTURA_USUARIO","S","FECHA_ULTIMACONSULTA","D",
                "FECHA_ENVIO_USUARIO_FMT","S","FECHA_LECTURA_USUARIO_FMT","S","FECHA_ULTIMACONSULTA_FMT","S",
                "FECHA_ENVIO_USUARIO_FMT_DIA","S","FECHA_LECTURA_USUARIO_FMT_DIA","S",
                "FECHA_ULTIMACONSULTA_FMT_DIA","S","NOMBRE_ALERTA","S","TIPO_ALERTA","I",
                "NOMBRE_TIPO","S","DESCRIPCION_TIPO","S","DESCRIPCION_ALERTA","S",
                "MODULO_ORIGEN","S","SUBMODULO_ORIGEN","S","NOMBRE_USUARIO","S","FECHA_ALERTA","D",
                "FECHA_EVENTO","D","SEVERIDAD_ALERTA","S","ID_EVENTO","I","ID_ALERTA","I",
                "FECHA_ALERTA_FMT","S","FECHA_EVENTO_FMT","S","FECHA_ENVIO_FMT","S","NOMBRE_PROYECTO","S",
                "ID_PROYECTO","I","FECHA_PLAN_INI_FMT","S","FECHA_PLAN_FIN_FMT","S","FECHA_REAL_INI_FMT","S",
                "FECHA_REAL_FIN_FMT","S","NOMBRE_JEFEPROYECTO","S","NOMBRE_AREA","S",
                "DESCRIPCION_TIPO_PROYECTO","S","NOMBRE_MAGNITUD","S","NOMBRE_PRIORIDAD","S",
                "NOMBRE_ESTADO_PROYECTO","S","NOMBRE_PRESUPUESTO","S","FECHA_CREACION","S",
                "NOMBRE_TAREA","S","ID_TAREA","I","NOMBRE_ESTADO_TAREA","S","URL_FORMULARIO",
                "S","ID_USUARIO_RESPONSABLE","I","CODI_USUARIO_RESPONSABLE","S","ID_USUARIO_EJECUTOR",
                "I","CODI_USUARIO_EJECUTOR","S","OBSERVACION_ACTUAL_TAREA","S","AVANCE_REAL","I",
                "BODYMAIL_ENVIO","S","ID_EVENTO_FIRSTOCURRENCIA","I","FECHA_CREACION_FIRSTOCURRENCIA","D",
                "FECHA_CREACION_FIRSTOCURRENCIA_FMT","S","OCURRENCIA_EVENTO","I","DESCRIPCION_GRUPO_ENVIO","S",
                "NOMBRE_USUARIO_ENVIO","S"
            };
            // Llamada a la BD
            String json = ObtenerLib.getDefaultObtenerResoultSetExt(logger, connection,
                    camposytipos, "VW_NOTIFICACION_OPERAUSER" , jsonObj);
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
