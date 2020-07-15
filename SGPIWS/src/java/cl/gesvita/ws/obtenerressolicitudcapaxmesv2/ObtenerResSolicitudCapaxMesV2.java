package cl.gesvita.ws.obtenerressolicitudcapaxmesv2;

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
 * Servlet implementation class ObtenerResSolicitudCapaxMesV2
 */
@WebServlet("/WSObtenerResSolicitudCapaxMesV2/ObtenerResSolicitudCapaxMesV2")
public class ObtenerResSolicitudCapaxMesV2 extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(ObtenerResSolicitudCapaxMesV2.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ObtenerResSolicitudCapaxMesV2() {
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
            String campos = "  id_empresa , fech_ingreso_mes "
                    + ",sum(cantidad_disponible) cantidad_disponible"
                    + ",sum(cantidad_utilizado) cantidad_utilizado ";
            String continuacion;
            String extratabla = "";
            Map<String, String> mapa= new HashMap<>();
            continuacion = " group by id_empresa , fech_ingreso_mes"
                    + " order by fech_ingreso_mes";
//            String[] camposytipos = {"ID_EMPRESA","I","ID_PROYECTO_TEMPLATE","/I","ID_SUCURSAL"
//                    ,"/I","TIPO_RUBRO","/S","TIPO_ESPACIO","/S","FECH_INGRESO_MES","D"
//                    ,"CANTIDAD_DISPONIBLE","+I","CANTIDAD_UTILIZADO","+I"};

            // IO (): ID_EMPRESA, FECH_INGRESO_MES
            // I  (/): ID_PROYECTO_TEMPLATE , COMUNA_SUCURSAL , ID_SUCURSAL , TIPO_RUBRO , TIPO_ESPACIO
            // O  (+): CANTIDAD_DISPONIBLE , CANTIDAD_UTILIZADO
            String[] camposytipos = {
                                        "ID_EMPRESA","I","ID_PROYECTO_TEMPLATE","/I"
                    ,"COMUNA_SUCURSAL","/S","ID_SUCURSAL","/I"
                    ,"TIPO_RUBRO","/S","TIPO_ESPACIO","/S","FECH_INGRESO_MES","D"
                    ,"CANTIDAD_DISPONIBLE","+I","CANTIDAD_UTILIZADO","+I"
            };
            // Llamada a la BD
            String json = ObtenerLib.getDefaultObtenerResoultSetExt(logger, connection,
                    camposytipos, "VW_RES_SOLICITUDCAPAXMES" , jsonObj,extratabla,campos,continuacion, mapa);
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
