package cl.gesvita.ws.obtenermeltinformeventas;

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
 * Servlet implementation class ObtenerMeltInformeVentas
 */
@WebServlet("/WSObtenerMeltInformeVentas/ObtenerMeltInformeVentas")
public class ObtenerMeltInformeVentas extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(ObtenerMeltInformeVentas.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ObtenerMeltInformeVentas() {
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
"ID_INFORME","I","ESTADO","S","CREADO_POR","S","SUCURSAL","S","FECHA_INGRESO","D","FECHA_INFORME","D","MODIFICADO_POR","S","FECHA_MODIFICACION","D","MONTO_ALOHA","I","MONTO_VENTA","I","MONTO_ALOHA_VENTA","I","B_ELETRONICAS","I","B_MANUALES","I","B_ANULADA","I","FACTURA","I","MONTO_RECAUDADO","I","RECD_EFECTIVO","I","RECD_CHEQUE","I","RECD_TBK_DEB","I","RECD_TBK_CRD","I","RECD_SODEX","I","RECD_WEBPAY","I","RECD_UBER_EATS","I","RECD_CUPON_CORTES","I","FALTANTE_DOCUMENTADO","I","REC_MENOS_VENTA","I"
            };
            // Llamada a la BD
            String json = ObtenerLib.getDefaultObtenerResoultSetExt(logger, connection,
                    camposytipos, "VW_INFORME_VENTAS" , jsonObj);
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
