package cl.gesvita.ws.obtenertareasinbox;

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
 * Servlet implementation class ObtenerTareasInbox
 */
@WebServlet("/WSObtenerTareasInbox/ObtenerTareasInbox")
public class ObtenerTareasInbox extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(ObtenerTareasInbox.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ObtenerTareasInbox() {
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
"ID_EMPRESA","I","ID_PROYECTO","I","NOMBRE_PROYECTO","S","NOMBRE_SUCURSAL","S","ID_ESTADO_PROYECTO","I","FECHA_PLAN_INI_FMT_PROY","S","FECHA_PLAN_FIN_FMT_PROY","S","FECHA_PLAN_INI_DATE_PROY","D","FECHA_PLAN_FIN_DATE_PROY","D","FECHA_REAL_INI_DATE_PROY","D","FECHA_REAL_FIN_DATE_PROY","D","ID_TAREA","I","NOMBRE_TAREA","S","ETAPA","S","FECHA_PLAN_INI","S","FECHA_PLAN_FIN","S","ID_ESTADO","I","NOMBRE_ESTADO","S","NOMBRE_TIPO","S","URL_FORMULARIO","S","NOMBRE_FORMULARIO","S","ID_USUARIO_EJECUTOR","I","NOMBREEJECUTOR","S","ID_AREA","I","ID_USUARIO_RESPONSABLE","I","NOMBRERESPONSABLE","S","MENSAJE_CUTOVER","S","COLOR_CUTOVER","S","ID_TAREA_PADRE","I","CODI_TAREA","S","DATA_EXTENDIDA","S","FECHA_MODIFICACION","S","FECHA_MODIFICACION_FMT","S","DESCRIPCION_TAREA","S"
            };
            // Llamada a la BD
            String json = ObtenerLib.getDefaultObtenerResoultSetExt(logger, connection,
                    camposytipos, "VW_PROYECTO_TAREAINBOX" , jsonObj);
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
