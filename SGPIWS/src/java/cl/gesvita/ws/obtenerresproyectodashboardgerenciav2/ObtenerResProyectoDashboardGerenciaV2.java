package cl.gesvita.ws.obtenerresproyectodashboardgerenciav2;

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
 * Servlet implementation class ObtenerResProyectoDashboardGerenciaV2
 */
@WebServlet("/WSObtenerResProyectoDashboardGerenciaV2/ObtenerResProyectoDashboardGerenciaV2")
public class ObtenerResProyectoDashboardGerenciaV2 extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(ObtenerResProyectoDashboardGerenciaV2.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ObtenerResProyectoDashboardGerenciaV2() {
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
            
            String campos = " id_empresa"
                    + ",sum(valor_capex)        valor_capex"
                    + ",max(unidad_monto_capex) unidad_monto_capex"
                    + ",sum(valor_saldo)        valor_saldo"
                    + ",max(unidad_valor_saldo) unidad_valor_saldo"
                    + ",sum(pctje_saldo_capex)  pctje_saldo_capex"
                    + ",sum(valor_ahorro_capex) valor_ahorro_capex"
                    + ",max(unidad_ahorro_saldo) unidad_ahorro_saldo"
                    + ",sum(pctje_ahorro_capex) pctje_ahorro_capex"
                    + ",sum(CANT_PROYECTOS_NOINICIADOS)  CANT_PROYECTOS_NOINICIADOS"
                    + ",sum(cant_proyectos_ejecucion)  cant_proyectos_ejecucion"
                    + ",sum(CANT_PROYECTOS_FINALIZADOS)  CANT_PROYECTOS_FINALIZADOS"
                    + ",sum(sum_avance_plan_portiempo) / sum(cant_proyectos_ejecucion+CANT_PROYECTOS_FINALIZADOS)  prom_avance_plan_portiempo" 
                    + ",sum(sum_avance_real_proyecto)  / sum(cant_proyectos_ejecucion+CANT_PROYECTOS_FINALIZADOS)   prom_avance_real_proyecto "
                    + ",sum(sum_desvio_proyecto)  / sum(cant_proyectos_ejecucion+CANT_PROYECTOS_FINALIZADOS) prom_desvio_proyecto" 
                    + ",sum(valor_presupuesto)  valor_presupuesto"
                    + ",sum(valor_gasto_real)  valor_gasto_real"
                    ;
            String continuacion;
            String extratabla = "";
            Map<String, String> mapa= new HashMap<>();
          //  continuacion = " {{WHEREAND}} PPLVIEW.ID_ESTADO = 2"
          //               + " GROUP BY id_empresa";        
            continuacion = " GROUP BY id_empresa";

            // IO () : ID_USUARIO_JEFE_PROYECTO
            // I  (/): FECHA_PLAN_INI_DATE , ID_PROYECTO_TEMPLATE
            // O  (+): PCTJE_AHORRO_CAPEX , CANT_PROYECTOS_EJECUCION, PROM_AVANCE_REAL_PROYECTO
            //         ,PROM_AVANCE_PLAN_PORTIEMPO , PROM_DESVIO_PROYECTO

            String[] camposytipos = {
                     "ID_EMPRESA","I"
                    ,"ID_PROYECTO_TEMPLATE","/I"
                    ,"VALOR_CAPEX","+I","UNIDAD_MONTO_CAPEX","+S"
                    ,"VALOR_SALDO","+I","UNIDAD_VALOR_SALDO","+S"
                    ,"PCTJE_SALDO_CAPEX","+I","VALOR_AHORRO_CAPEX","+I"
                    ,"UNIDAD_AHORRO_SALDO","+S","PCTJE_AHORRO_CAPEX","+I"
                    ,"CANT_PROYECTOS_NOINICIADOS","+I"
                    ,"CANT_PROYECTOS_EJECUCION","+I"
                    ,"CANT_PROYECTOS_FINALIZADOS","+I"
                    ,"PROM_AVANCE_PLAN_PORTIEMPO","+I"
                    ,"PROM_AVANCE_REAL_PROYECTO","+I","PROM_DESVIO_PROYECTO","+I"
                    ,"VALOR_PRESUPUESTO","+I","VALOR_GASTO_REAL","+I"
            };
            // Llamada a la BD
            String json = ObtenerLib.getDefaultObtenerResoultSetExt(logger, connection,
                    camposytipos, "VW_RES_PROYECTO_DASHBOARDGERENCIA" , jsonObj,extratabla,campos,continuacion, mapa);
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