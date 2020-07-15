package cl.gesvita.ws.obtenertareasdetalle;

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
 * Servlet implementation class ObtenerTareasDetalleOld
 */
@WebServlet("/WSObtenerTareasDetalle/ObtenerTareasDetalleOld")
public class ObtenerTareasDetalleOld extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(ObtenerTareasDetalleOld.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ObtenerTareasDetalleOld() {
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
"ID_TAREA","I","ID_PROYECTO","I","FECHA_PLAN_INI","D","FECHA_PLAN_FIN","D","FECHA_REAL_INI","D","FECHA_REAL_FIN","D","ID_TAREA_PADRE","I","ID_TAREA_DEPENDENCIA","S","NOMBRE_TAREA","S","DESCRIPCION_TAREA","S","DURACION_PLANIFICADA","F","DURACION_REAL","F","ID_TIPO_TAREA","I","NOMBRE_TIPO","S","TIPO_ELEMENTO","S","ID_PRIORIDAD","I","NOMBRE_PRIORIDAD","S","ID_ESTADO","I","NOMBRE_ESTADO","S","ID_FORMULARIO","I","AVANCE_PLANIFICADO","F","AVANCE_REAL","F","ID_USUARIO_RESPONSABLE","I","CODI_USUARIO_RESPONSABLE","S","NOMBRE_USUARIO_RESPONSABLE","S","ID_AREA_RESPONSABLE","I","NOMBRE_AREA_RESPONSABLE","S","ID_AREA_PADRE_RESPONSABLE","I","NOMBRE_AREA_PADRE_RESPONSABLE","S","ID_USUARIO_EJECUTOR","I","CODI_USUARIO_EJECUTOR","S","NOMBRE_USUARIO_EJECUTOR","S","FECHA_CREACION","D","FECHA_MODIFICACION","D","DATA_EXTENDIDA","S","ID_USUARIO_CREACION","I","TAREA_INPUT","S","TAREA_OUTPUT","S","ID_BPM","S","DIAS_CUTOVERTAREA","S","MENSAJE_CUTOVER","S","COLOR_CUTOVER","S","SLA_PLAZOS_TAREA","I","PTCJE_DESVIO","I","CODI_TAREA","S","TIPO_TAREA_NEGOCIO","S","ID_TAREA_TEMPLATE","I","ID_PROYECTO_ENLACETEMPLATE","I","ID_PROYECTO_ENLACE","I","OBSERVACION_ACTUAL_TAREA","S","PORCENTAJE_ASIGNACION","F","PORCENTAJE_EN_TAREA_PADRE","F","ID_USUARIO_MODIFICACION","I"
            };
            // Llamada a la BD
            String json = ObtenerLib.getDefaultObtenerResoultSetExt(logger, connection,
                    camposytipos, "VW_PROYECTO_TAREADETALLE" , jsonObj);
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
