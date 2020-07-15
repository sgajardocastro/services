package cl.gesvita.ws.obtenerdocumentosproyecto;

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
 * Servlet implementation class ObtenerDocumentosProyecto
 */
@WebServlet("/WSObtenerDocumentosProyecto/ObtenerDocumentosProyecto")
public class ObtenerDocumentosProyecto extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(ObtenerDocumentosProyecto.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ObtenerDocumentosProyecto() {
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
"ID_DOCUMENTO","I","NOMBRE_DOCUMENTO","S","TITULO_DOCUMENTO","S","DESCRIPCION_DOCUMENTO","S","TIPO_DOCUMENTO","S","CLASE_DOCUMENTO","S","VERSION_DOCUMENTO","S","ID_ESTADO_DOCUMENTO","I","NOMBRE_ESTADO_DOCUMENTO","S","REPOSITORIO_DOCUMENTO","S","UBICACION_DOCUMENTO","S","METADATA_DOCUMENTO","S","ID_DOCUMENTO_ENREPOSITORIO","S","ID_USUARIO_CREACION","I","CODI_USUARIO_CREACION","S","NOMBRE_USUARIO_CREACION","S","ID_USUARIO_MODIFICACION","I","CODI_USUARIO_MODIFICACION","S","NOMBRE_USUARIO_MODIFICACION","S","ID_USUARIO_ULTIMACONSULTA","I","CODI_USUARIO_ULTIMACONSULTA","S","NOMBRE_USUARIO_ULTIMACONSULTA","S","ID_USUARIO_ULTIMODOWNLOAD","I","CODI_USUARIO_ULTIMODOWNLOAD","S","NOMBRE_USUARIO_ULTIMODOWNLOAD","S","FECHA_CREACION","D","FECHA_MODIFICACION","D","FECHA_ULTIMACONSULTA","D","FECHA_ULTIMODOWNLOAD","D","FECHA_APROBACION","D","FECHA_CREACION_FMT","S","FECHA_MODIFICACION_FMT","S","FECHA_ULTIMACONSULTA_FMT","S","FECHA_ULTIMODOWNLOAD_FMT","S","FECHA_APROBACION_FMT","S","ID_PROYECTO","I","ID_TAREA","I"
            };
            // Llamada a la BD
            String json = ObtenerLib.getDefaultObtenerResoultSetExt(logger, connection,
                    camposytipos, "VW_PROYECTO_DOCUMENTO" , jsonObj);
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
