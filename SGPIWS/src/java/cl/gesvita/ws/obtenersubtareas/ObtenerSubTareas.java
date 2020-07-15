package cl.gesvita.ws.obtenersubtareas;

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
 * Servlet implementation class ObtenerSubTareas
 */
@WebServlet("/WSObtenerSubTareas/ObtenerSubTareas")
public class ObtenerSubTareas extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(ObtenerSubTareas.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ObtenerSubTareas() {
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
                                      "ID_PROYECTO","I","ID_TAREA","I","NOMBRE_TAREA","S","ID_TIPO_TAREA","I","CODI_TAREA","S",
                "ID_ESTADO","I","NOMBRE_ESTADO","S","TIPO_TAREA_NEGOCIO","S","ID_USUARIO_EJECUTOR","I",
                "CODI_USUARIO_EJECUTOR","S","NOMBRE_USUARIO_EJECUTOR","S","FECHA_PLAN_INI","D",
                "FECHA_PLAN_FIN","D","FECHA_REAL_INI","D","FECHA_REAL_FIN","D","FECHA_PLAN_INI_FMT","S",
                "FECHA_PLAN_FIN_FMT","S","FECHA_REAL_INI_FMT","S","FECHA_REAL_FIN_FMT","S","FECHA_MODIFICACION_FMT","S",
                "NOMBRE_USUARIO_MODIFICACION","S","ID_TAREA_PADRE","I","ID_TAREA_HIJO","I","NOMBRE_TAREA_HIJO","S",
                "TIPO_TAREA_NEGOCIO_HIJO","S","ID_TIPO_TAREA_HIJO","I","CODI_TAREA_HIJO","S","ID_ESTADO_HIJO","I",
                "NOMBRE_ESTADO_HIJO","S","ID_USUARIO_EJECUTOR_HIJO","I","CODI_USUARIO_EJECUTOR_HIJO","S",
                "NOMBRE_USUARIO_EJECUTOR_HIJO","S","FECHA_PLAN_INI_HIJO","D","FECHA_PLAN_FIN_HIJO","D",
                "FECHA_REAL_INI_HIJO","D","FECHA_REAL_FIN_HIJO","D","FECHA_PLAN_INI_FMT_HIJO","S","FECHA_PLAN_FIN_FMT_HIJO","S",
                "FECHA_REAL_INI_FMT_HIJO","S","FECHA_REAL_FIN_FMT_HIJO","S","FECHA_MODIFICACION_FMT_HIJO","S",
                "NOMBRE_USUARIO_MODIFICACION_HIJO","S","TAREA_OUTPUT_HIJO","S","DATA_EXTENDIDA_HIJO","S",
                "ID_TAREA_NIETO","I","NOMBRE_TAREA_NIETO","S",
                "CODI_TAREA_NIETO","S","ID_TIPO_TAREA_NIETO","I","ID_ESTADO_NIETO","I","NOMBRE_ESTADO_NIETO","S",
                "ID_USUARIO_EJECUTOR_NIETO","I","CODI_USUARIO_EJECUTOR_NIETO","S","NOMBRE_USUARIO_EJECUTOR_NIETO","S",
                "FECHA_PLAN_INI_NIETO","D","FECHA_PLAN_FIN_NIETO","D","FECHA_REAL_INI_NIETO","D","FECHA_REAL_FIN_NIETO","D",
                "FECHA_PLAN_INI_FMT_NIETO","S","FECHA_PLAN_FIN_FMT_NIETO","S","FECHA_REAL_INI_FMT_NIETO","S",
                "FECHA_REAL_FIN_FMT_NIETO","S","FECHA_MODIFICACION_FMT_NIETO","S","NOMBRE_USUARIO_MODIFICACION_NIETO","S",
                "TAREA_OUTPUT_NIETO","S","DATA_EXTENDIDA_NIETO","S"};
            // Llamada a la BD
            String json = ObtenerLib.getDefaultObtenerResoultSetExt(logger, connection,
                    camposytipos, "VW_PROYECTO_SUBTAREAS" , jsonObj);
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
