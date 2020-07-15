package cl.gesvita.ws.obtenertareaslazy;

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
 * Servlet implementation class ObtenerTareasLazy
 */
@WebServlet("/WSObtenerTareasLazy/ObtenerTareasLazy")
public class ObtenerTareasLazy extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(ObtenerTareasLazy.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ObtenerTareasLazy() {
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
            // Campos agregados el 7Ene2019
            // "CODI_GRUPO_PROYECTO","S","ID_AREA","I","ID_REQUERIMIENTO","I","AVANCE_REAL_PROYECTO","I","AVANCE_PLAN_PORTIEMPO","I","KPI_PROYECTO_AVANCE_STATUS","I",
            // "FECHA_REAL_INI","D","FECHA_REAL_FIN","D",
            // "ID_TAREA","I", /* Campo ingresado manual*/
            
            String[] camposytipos = {
//                "ID_PROYECTO","I","ID_EMPRESA","I","NOMBRE_PROYECTO","S","CODI_PROYECTO","S","ID_ESTADO_SOLICITUD","I",
//                "ID_PROYECTO_TEMPLATE","I","NOMBRE_JEFEPROYECTO","S","ID_USUARIO_JEFE_PROYECTO","I",
//                "NOMBRE_REQUERIMIENTO","S","ID_TIPO_PROYECTO","I","FECHA_PLAN_INIPROY_DATE","D",
//                "FECHA_PLAN_FINPROY_DATE","D","FECHA_REAL_INIPROY_DATE","D","FECHA_REAL_FINPROY_DATE","D",
//                "NOMBRE_TAREA","S","CODI_TAREA","S","ID_TIPO_TAREA","I","ID_ESTADO","I","NOMBRE_ESTADO","S",
//                "DATA_EXTENDIDA","S","ID_USUARIO_EJECUTOR","I"

                "ID_PROYECTO","I","ID_EMPRESA","I","NOMBRE_PROYECTO","S","CODI_PROYECTO","S","CODI_GRUPO_PROYECTO","S","ID_ESTADO_SOLICITUD","I",
                "ID_TAREA","I",
                "ID_PROYECTO_TEMPLATE","I","ID_AREA","I","ID_REQUERIMIENTO","I","NOMBRE_JEFEPROYECTO","S","ID_USUARIO_JEFE_PROYECTO","I",
                "NOMBRE_REQUERIMIENTO","S","ID_TIPO_PROYECTO","I","FECHA_PLAN_INIPROY_DATE","D",
                "FECHA_PLAN_FINPROY_DATE","D","FECHA_REAL_INIPROY_DATE","D","FECHA_REAL_FINPROY_DATE","D",
                "AVANCE_REAL_PROYECTO","I","AVANCE_PLAN_PORTIEMPO","I","KPI_PROYECTO_AVANCE_STATUS","I",
                "NOMBRE_TAREA","S","CODI_TAREA","S","ID_TIPO_TAREA","I","ID_ESTADO","I","NOMBRE_ESTADO","S",
                "FECHA_REAL_INI","D","FECHA_REAL_FIN","D",
                "DATA_EXTENDIDA","S","ID_USUARIO_EJECUTOR","I"


            };
            // Llamada a la BD
            String json = ObtenerLib.getDefaultObtenerResoultSetExt(logger, connection,
                    camposytipos, "VW_PROYECTO_TAREALAZY" , jsonObj);
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
