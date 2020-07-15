package cl.gesvita.ws.obtenerresproyectorendimientoxareav2;

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
 * Servlet implementation class ObtenerResProyectoRendimientoxAreaV2
 */
@WebServlet("/WSObtenerResProyectoRendimientoxAreaV2/ObtenerResProyectoRendimientoxAreaV2")
public class ObtenerResProyectoRendimientoxAreaV2 extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(ObtenerResProyectoRendimientoxAreaV2.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ObtenerResProyectoRendimientoxAreaV2() {
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
            
            String campos = " id_empresa "
                    + ",id_area "
                    + ",nombre_area "
                    + ",sum(valor_capex)        valor_capex"
                    + ",max(unidad_monto_capex) unidad_monto_capex"
                    + ",sum(valor_saldo)        valor_saldo"
                    + ",max(unidad_valor_saldo) unidad_valor_saldo"
                    + ",sum(pctje_saldo_capex)  pctje_saldo_capex"
                    + ",sum(valor_ahorro_capex) valor_ahorro_capex"
                    + ",max(unidad_ahorro_saldo) unidad_ahorro_saldo"
                    + ",sum(pctje_ahorro_capex) pctje_ahorro_capex"
                    + ",sum(cant_proyectos_ejecucion)  cant_proyectos_ejecucion"
                    + ",sum(sum_avance_plan_portiempo) / sum(cantidad_proyectos)  prom_avance_plan_portiempo" 
                    + ",sum(sum_avance_real_proyecto)  / sum(cantidad_proyectos)   prom_avance_real_proyecto "
                    + ",sum(sum_desvio_proyecto)  / sum(cantidad_proyectos) prom_desvio_proyecto" 
                    + ",sum(valor_presupuesto)  valor_presupuesto"
                    + ",sum(valor_gasto_real)  valor_gasto_real"
                    + ",max(unidad_valor_gasto) unidad_valor_gasto"
                    + ",sum(MONTO_CCOSTOCMOV)  MONTO_CCOSTOCMOV"
                    + ",max(unidad_MONTO_CCOSTOCMOV) unidad_MONTO_CCOSTOCMOV"
                    + ",sum(CANTIDADOC_APROBADAS)  CANTIDADOC_APROBADAS"
                    + ",sum(CANTIDADOC_PENDIENTES)  CANTIDADOC_PENDIENTES"
                    ;
            String continuacion;
            String extratabla = "";
            Map<String, String> mapa= new HashMap<>();
          //  continuacion = " {{WHEREAND}} PPLVIEW.ID_ESTADO = 2"
          //               + " GROUP BY id_empresa";        
            continuacion = " GROUP BY id_empresa,id_area,nombre_area";

            // IO () : ID_EMPRESA , ID_AREA
            // I  (/): ID_PROYECTO_TEMPLATE
            // O  (+): NOMBRE_AREA , VALOR_CAPEX ,UNIDAD_MONTO_CAPEX , VALOR_SALDO
            //        ,UNIDAD_VALOR_SALDO , PCTJE_SALDO_CAPEX , VALOR_AHORRO_CAPEX
            //        ,UNIDAD_AHORRO_SALDO , PCTJE_AHORRO_CAPEX , CANT_PROYECTOS_EJECUCION
            //        ,PROM_AVANCE_PLAN_PORTIEMPO , PROM_AVANCE_REAL_PROYECTO
            //        ,VALOR_PRESUPUESTO, VALOR_GASTO_REAL , MONTO_CCOSTOCMOV
            //        ,UNIDAD_MONTO_CCOSTOCMOV, CANTIDADOC_APROBADAS
            //        ,CANTIDADOC_PENDIENTES

            String[] camposytipos = {
                     "ID_EMPRESA","I"
                    ,"ID_AREA","I"
                    ,"NOMBRE_AREA","+S"
                    ,"ID_PROYECTO_TEMPLATE","/I"
                    ,"VALOR_CAPEX","+I","UNIDAD_MONTO_CAPEX","+S"
                    ,"VALOR_SALDO","+I","UNIDAD_VALOR_SALDO","+S"
                    ,"PCTJE_SALDO_CAPEX","+I","VALOR_AHORRO_CAPEX","+I"
                    ,"UNIDAD_AHORRO_SALDO","+S","PCTJE_AHORRO_CAPEX","+I"
                    ,"CANT_PROYECTOS_EJECUCION","+I","PROM_AVANCE_PLAN_PORTIEMPO","+I"
                    ,"PROM_AVANCE_REAL_PROYECTO","+I","PROM_DESVIO_PROYECTO","+I"
                    ,"VALOR_PRESUPUESTO","+I","VALOR_GASTO_REAL","+I"
                    ,"MONTO_CCOSTOCMOV","+I","UNIDAD_MONTO_CCOSTOCMOV","+S"
                    ,"CANTIDADOC_APROBADAS","+I","CANTIDADOC_PENDIENTES","+I"
            };
            // Llamada a la BD
            String json = ObtenerLib.getDefaultObtenerResoultSetExt(logger, connection,
                    camposytipos, "VW_RES_PROYECTO_RENDPYXAREA" , jsonObj,extratabla,campos,continuacion, mapa);
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