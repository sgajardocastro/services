package cl.gesvita.ws.obtenerressolicitudefectividadxclientev2;

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
 * Servlet implementation class ObtenerResSolicitudEfectividadxClienteV2
 */
@WebServlet("/WSObtenerResSolicitudEfectividadxClienteV2/ObtenerResSolicitudEfectividadxClienteV2")
public class ObtenerResSolicitudEfectividadxClienteV2 extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(ObtenerResSolicitudEfectividadxClienteV2.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ObtenerResSolicitudEfectividadxClienteV2() {
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
                          + ",id_empresa_cliente "
                          + ",sum(CANTIDAD_APROBADOS) CANTIDAD_APROBADOS"
                          + ",sum(CANTIDAD_RECHAZADOS) CANTIDAD_RECHAZADOS"
                          + ",( (sum(CANTIDAD_APROBADOS)) / (sum(CANTIDAD_APROBADOS) + sum(CANTIDAD_RECHAZADOS)) ) ptcje_efectividad"
                          + ",sum(monto_aprobados) monto_aprobados "
                          + ",sum(monto_rechazados) monto_rechazados "
                          ;

            String continuacion;
            String extratabla = "";
            Map<String, String> mapa= new HashMap<>();
          //  continuacion = " {{WHEREAND}} PPLVIEW.ID_ESTADO = 2"
          //               + " GROUP BY id_empresa";        
            continuacion = " GROUP BY id_empresa,id_empresa_cliente";

            // IO () : ID_EMPRESA, ID_EMPRESA_CLIENTE
            // I  (/): ID_PROYECTO_TEMPLATE , ID_USUARIO_CREACION
            // O  (+): CANTIDAD_APROBADOS , CANTIDAD_RECHAZADOS, PTCJE_EFECTIVIDAD
            //         ,MONTO_APROBADOS , MONTO_RECHAZADOS


            String[] camposytipos = {
            "ID_EMPRESA","I","ID_EMPRESA_CLIENTE","I","ID_PROYECTO_TEMPLATE","/I","ID_USUARIO_CREACION","/I"
            ,"CANTIDAD_APROBADOS","+I","CANTIDAD_RECHAZADOS","+I","PTCJE_EFECTIVIDAD","+I","MONTO_APROBADOS","+I","MONTO_RECHAZADOS","+I"
            };
            // Llamada a la BD
            String json = ObtenerLib.getDefaultObtenerResoultSetExt(logger, connection,
                    camposytipos, "VW_RES_SOLICITUDEFECTXCLIEN" , jsonObj,extratabla,campos,continuacion, mapa);
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
