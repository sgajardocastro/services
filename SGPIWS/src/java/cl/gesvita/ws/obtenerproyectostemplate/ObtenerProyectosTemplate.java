package cl.gesvita.ws.obtenerproyectostemplate;

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
 * Servlet implementation class ObtenerProyectosTemplate
 */
@WebServlet("/WSObtenerProyectosTemplate/ObtenerProyectosTemplate")
public class ObtenerProyectosTemplate extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(ObtenerProyectosTemplate.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ObtenerProyectosTemplate() {
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
"ID_PROYECTO","I","NOMBRE_PROYECTO","S","FECHA_PLAN_INI","D","FECHA_PLAN_FIN","D","FECHA_REAL_INI","D","FECHA_REAL_FIN","D","ID_PROGRAMA","I","ID_USUARIO_JEFE_PROYECTO","I","ID_USUARIO_SPONSOR","I","ID_TIPO","I","ID_MAGNITUD","I","ID_PRIORIDAD","I","ID_SPONSOR_AREA","I","ID_CENTRO_COSTO","I","ID_ESTADO","I","ID_EMPRESA","I","ID_PPTO","I","ID_CONTROL_CAMBIO","I","CANTIDAD_RIESGO","I","ID_USUARIO_CREACION","I","ID_TIPO_TEMPLATE","I","FECHA_CREACION","D","FECHA_MODIFICACION","D","ID_COMPORTAMIENTO","I","CODI_GRUPO","S","ID_AREA","I","OBJETIVO_PROYECTO","S","OBSERVACION_PROYECTO","S","ID_EMPRESA_CLIENTE","I","ID_AREA_CLIENTE","I","ID_USUARIO_CLIENTE","I","CODI_PROYECTO","S","ID_PROYECTO_PADRE","I","ID_REQUERIMIENTO","I","ID_SUCURSAL","I","TIPO_PROYECTO_NEGOCIO","S","DATA_EXTENDIDA","S","ID_CONTRATO","I","ID_PROYECTO_TEMPLATE","I","ID_FORMULARIO_PROYECTO","I","OBSERVACION_ACTUAL","S","AVANCE_REAL_PROYECTO","F","AVANCE_PLAN_PROYECTO","F","ID_USUARIO_MODIFICACION","I"
            };
            // Llamada a la BD
            String json = ObtenerLib.getDefaultObtenerResoultSetExt(logger, connection,
                    camposytipos, "VW_PROYECTOS_TEMPLATE" , jsonObj);
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
