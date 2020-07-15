package cl.gesvita.ws.obtenerresproyectodashboardjpv2;

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
import java.util.HashMap;
import java.util.Map;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.log4j.Logger;

/**
 * Servlet implementation class ObtenerResProyectoDashboardJpV2
 */
@WebServlet("/WSObtenerResProyectoDashboardJpV2/ObtenerResProyectoDashboardJpV2")
public class ObtenerResProyectoDashboardJpV2 extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(ObtenerResProyectoDashboardJpV2.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ObtenerResProyectoDashboardJpV2() {
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
            
            String campos = "  id_usuario_jefe_proyecto "
                    + ",0 pctje_ahorro_capex"
                    + ",sum(case id_estado when 2 then 1 else 0 end)  cant_proyectos_ejecucion"
                    + ",avg(nvl(AVANCE_REAL_PROYECTO,0))    prom_avance_real_proyecto "
                    + ",avg(nvl(AVANCE_PLAN_PORTIEMPO,0))   prom_avance_plan_portiempo "
                    + ",avg(nvl(AVANCE_PLAN_PORTIEMPO,0) - nvl(AVANCE_REAL_PROYECTO,0) )   prom_desvio_proyecto "
                    ;
            String continuacion;
            String extratabla = "";
            Map<String, String> mapa= new HashMap<>();
            continuacion = " {{WHEREAND}} PPLVIEW.ID_ESTADO = 2"
                         + " GROUP BY id_usuario_jefe_proyecto";

            // IO () : ID_USUARIO_JEFE_PROYECTO
            // I  (/): FECHA_PLAN_INI_DATE , ID_PROYECTO_TEMPLATE
            // O  (+): PCTJE_AHORRO_CAPEX , CANT_PROYECTOS_EJECUCION, PROM_AVANCE_REAL_PROYECTO
            //         ,PROM_AVANCE_PLAN_PORTIEMPO , PROM_DESVIO_PROYECTO

            String[] camposytipos = {
                                        "ID_USUARIO_JEFE_PROYECTO","I","FECHA_PLAN_INI_DATE","/D"
                    ,"PCTJE_AHORRO_CAPEX","+I","ID_PROYECTO_TEMPLATE","/I"
                    ,"CANT_PROYECTOS_EJECUCION","+I","PROM_AVANCE_REAL_PROYECTO","+I"
                    ,"PROM_AVANCE_PLAN_PORTIEMPO","+I","PROM_DESVIO_PROYECTO","+I"
            };
            // Llamada a la BD
            String json = ObtenerLib.getDefaultObtenerResoultSetExt(logger, connection,
                    camposytipos, "VW_PROYECTO_SETUP" , jsonObj,extratabla,campos,continuacion, mapa);
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
