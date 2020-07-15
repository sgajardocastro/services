package cl.gesvita.ws.obtenerinformacionproyecto;

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
 * Servlet implementation class ObtenerInformacionProyecto
 */
@WebServlet("/WSObtenerInformacionProyecto/ObtenerInformacionProyecto")
public class ObtenerInformacionProyecto extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(ObtenerInformacionProyecto.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ObtenerInformacionProyecto() {
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
                "ID_PROYECTO","I","CODI_PROYECTO","S","ID_PROYECTO_PADRE","I","NOMBRE","S","OBJETIVO","S",
                "FECHA_PLAN_INI","S","FECHA_PLAN_FIN","S","FECHA_ORDER","D","FECHA_REAL_INI","S",
                "FECHA_REAL_FIN","S","NOMBRE_USUARIO","S","NOMBRE_JEFEPROYECTO","S","NOMBRE_SPONSOR","S",
                "NOMBRE_AREA","S","ID_TIPO","I","DESCRIPCION_TIPO","S","ID_MAGNITUD","I","NOMBRE_MAGNITUD","S",
                "ID_PRIORIDAD","I","NOMBRE_PRIORIDAD","S","ID_ESTADO","I","NOMBRE_ESTADO","S",
                "NOMBRE_EMPRESA","S","NOMBRE_PRESUPUESTO","S","OBSERVACION_PROYECTO","S","OBSERVACION","S",
                "DIAS_CUTOVER","S","DIAS_CUTOVER_NUMERO","I","ID_USUARIO_JEFE_PROYECTO","I",
                "NOMBRE_PROGRAMA","S","ID_EMPRESA","I","ID_PAIS","I","FECHA_REAL_INI_DATE","D",
                "FECHA_REAL_FIN_DATE","D","FECHA_PLAN_INI_DATE","D","FECHA_PLAN_FIN_DATE","D",
                "NOMBRE_TIPO","S","ID_PPTO","I","ID_USUARIO_CREACION","I","NOMBRE_USUARIOCREACION","S",
                "ID_COMPORTAMIENTO","I","ID_AREA","I","ID_TIPO_TEMPLATE","I",
                "ID_PROYECTO_TEMPLATE","I","FECHA_MODIFICACION_FMT","S",
                "FECHA_CREACION","D","FECHA_CREACION_FMT","S"
            };
            // Llamada a la BD
            String json = ObtenerLib.getDefaultObtenerResoultSetExt(logger, connection,
                    camposytipos, "VW_PROYECTO_SETUP_FICHA" , jsonObj);
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
