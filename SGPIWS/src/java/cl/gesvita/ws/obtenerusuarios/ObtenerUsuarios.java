package cl.gesvita.ws.obtenerusuarios;

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
 * Servlet implementation class ObtenerUsuarios
 */
@WebServlet("/WSObtenerUsuarios/ObtenerUsuarios")
public class ObtenerUsuarios extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(ObtenerUsuarios.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ObtenerUsuarios() {
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
            // BYSECURITY logger.info("Entrada:" + jsonObj.toString());
        } catch (GesvitaException ex) {
            logger.error(ex.getMessage());
            out.print("{\"resultadoEjecucion\":\"NOK\",\"mensaje\":\"Ocurrio un error\",\"mensajeTec\":\"Error de parseo\"}");
            return;
        }
        // Realizar la consulta
        try {
            // Preparar la consulta
            connection = datasource.getConnection();
            String[] camposytipos = {"ID_USUARIO", "I", "CODI_USUARIO", "-S", "NOMBRE_USUARIO", "-S",
                "RUT_USUARIO", "-S", "TELEFONO_USUARIO", "-S", "CELULAR_USUARIO", "-S", "CORREO_USUARIO", "-S",
                "ESTADO_USUARIO", "-S", "USUARIO", "S", "NOMBRE", "S", "APELLIDO_MATERNO", "S",
                "APELLIDO_PATERNO", "S", "RUT", "S", "TELEFONO", "S", "CELULAR", "S", "CORREO", "S",
                "ID_ROL", "I", "NOMBRE_ROL", "S", "CODI_GRUPO_BASE_ROL", "-S", "ID_PERFIL", "I",
                "CODI_GRUPO_BASE_PERFIL", "-S", "NOMBRE_PERFIL", "S", "ESTADO", "S", "ID_EMPRESA", "I",
                "NOMBRE_EMPRESA", "-S", "ID_AREA", "I", "NOMBRE_AREA", "+S", "PASSWORD_USUARIO", "-S",
                "PASSWORD", "-S", "OBSERVACION_USUARIO", "+S"};
//            String[] camposytipos = {
//                                   "ID_USUARIO","I","CODI_USUARIO","S","NOMBRE_USUARIO","S"
//              ,"RUT_USUARIO","S","TELEFONO_USUARIO","S","CELULAR_USUARIO","S","CORREO_USUARIO","S"
//                ,"ESTADO_USUARIO","S","USUARIO","S","NOMBRE","S","APELLIDO_MATERNO","S"
//                ,"APELLIDO_PATERNO","S","RUT","S","TELEFONO","S","CELULAR","S","CORREO","S"
//                ,"ID_ROL","I","NOMBRE_ROL","S","CODI_GRUPO_BASE_ROL","S","ID_PERFIL","I"
//                ,"CODI_GRUPO_BASE_PERFIL","S","NOMBRE_PERFIL","S","ESTADO","S","ID_EMPRESA","I"
//                ,"NOMBRE_EMPRESA","S","ID_AREA","I","NOMBRE_AREA","S","PASSWORD_USUARIO","S"
//                ,"PASSWORD","S","OBSERVACION_USUARIO","S"
//            };
            // Llamada a la BD
            String json = ObtenerLib.getDefaultObtenerResoultSetExt(logger, connection,
                    camposytipos, "VW_SEGURIDAD_USUARIO" , jsonObj);
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

