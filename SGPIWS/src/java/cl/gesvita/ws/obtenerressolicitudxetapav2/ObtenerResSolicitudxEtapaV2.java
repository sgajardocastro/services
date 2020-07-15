package cl.gesvita.ws.obtenerressolicitudxetapav2;

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
 * Servlet implementation class ObtenerResSolicitudxEtapaV2
 */
@WebServlet("/WSObtenerResSolicitudxEtapaV2/ObtenerResSolicitudxEtapaV2")
public class ObtenerResSolicitudxEtapaV2 extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(ObtenerResSolicitudxEtapaV2.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ObtenerResSolicitudxEtapaV2() {
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
                    + ",ID_USUARIO_CREACION "
                    + ",ID_ESTADO "
                    + ",NOMBRE_ETAPA "
                    + ",sum(monto_etapa)   monto_etapa"
                    + ",sum(cantidad_etapa)   cantidad_etapa"
                    ;

            String continuacion;
            String extratabla= "";
            Map<String, String> mapa= new HashMap<>();
          //  continuacion = " {{WHEREAND}} PPLVIEW.ID_ESTADO = 2"
          //               + " GROUP BY id_empresa";        
            continuacion = " GROUP BY id_empresa,ID_USUARIO_CREACION,ID_ESTADO,NOMBRE_ETAPA";

            // IO (): ID_EMPRESA, ID_USUARIO_CREACION,ID_ESTADO,NOMBRE_ETAPA
            // I  (/): FECH_INGRESO_MES
            // O  (+): MONTO_ETAPA , CANTIDAD_ETAPA
            String[] camposytipos = {
                "ID_EMPRESA","I","ID_USUARIO_CREACION","I","FECH_INGRESO_MES","/D"
                ,"ID_ESTADO","I","NOMBRE_ETAPA","S","MONTO_ETAPA","+I","CANTIDAD_ETAPA","+I"
            };
           // Llamada a la BD
            String json = ObtenerLib.getDefaultObtenerResoultSetExt(logger, connection,
                    camposytipos, "VW_RES_SOLICITUDXETAPA" , jsonObj,extratabla,campos,continuacion,mapa);
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
                          
