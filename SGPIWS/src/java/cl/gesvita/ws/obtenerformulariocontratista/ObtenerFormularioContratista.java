package cl.gesvita.ws.obtenerformulariocontratista;

import cl.gesvita.ws.obtener.exception.GesvitaException;
import cl.gesvita.ws.obtener.lib.ObtenerLib;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.log4j.Logger;

/**
 * Servlet implementation class ObtenerFormularioLocalidades
 */
@WebServlet("/WSObtenerFormularioContratista/ObtenerFormularioContratista")
public class ObtenerFormularioContratista extends HttpServlet {

    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(ObtenerFormularioContratista.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ObtenerFormularioContratista() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     * response)
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


        String salida[] = new String[3];  
      
        try {


            String fieldName = "id_empresa";
            String id_usuario = leerCampo(jsonObj,fieldName);
            
            // ,\"id\":\""+ id_usuario + "\"
            salida[0] = "{\"resultadoEjecucion\":\"OK\",\"contratista\":{\"1\":{\"id\":\"1001\",\"name\":\"Empresa 1\"}}}";
            salida[1] = "{\"resultadoEjecucion\":\"OK\",\"contratista\":{\"1\":{\"id\":\"1002\",\"name\":\"Empresa 2\"}}}";
            salida[2] = "{\"resultadoEjecucion\":\"OK\",\"contratista\":{\"1\":{\"id\":\"1003\",\"name\":\"Empresa 3\"}}}";
            
            
            if (!validaFormatoRut(id_usuario))
                throw new GesvitaException("Error en formato de campo '" + fieldName+ "'"); // , '" + id_usuario+ "'
            
            int rot3 = Integer.parseInt(id_usuario.substring(0,id_usuario.length()-1)) % 3;
            
            out.print(salida[rot3]);
            
        } catch (GesvitaException ex) {
            logger.error(ex.getMessage());
            out.print("{\"resultadoEjecucion\":\"NOK\",\"mensaje\":\"" + ex.getMessage() + "\",\"mensajeTec\":\"" + ex.getMessage() + "\"}");
            return;
        }
        
        
            
        // Realizar la consulta
//        try {
//            // Preparar la consulta
//            connection = datasource.getConnection();
//            String[] camposytipos = {
//"ID_EMPRESA","I","NOMBRE_EMPRESA","S","ID_PAIS","I","NOMBRE_PAIS","S","RUT_EMPRESA","S","RAZON_SOCIAL","S","GIRO_EMPRESA","S","DIRECCION_EMPRESA","S","REPRESENTANTE_LEGAL","S","FONO_CONTACTO","S","NOMBRE_CONTACTO","S","ROL_CONTACTO","S","EMAIL_EMPRESA","S","LATITUD_EMPRESA","S","LONGITUD_EMPRESA","S","ESTADO_EMPRESA","S","ID_EMPRESA_PADRE","I","NOMBRE_EMPRESA_PADRE","S","ZONA_HORARIA","S","ID_TIPO_CAMBIO","I","ID_REPRESENTANTE_LEGAL","I","NOMBRE_USUARIO_REPRESENTANTE","S","ID_USUARIO_CREACION","I","NOMBRE_USUARIO_CREACION","S","FECHA_CREACION","D","FECHA_CREACION_FMT","S","FECHA_MODIFICACION","D","FECHA_MODIFICACION_FMT","S","COMUNA_EMPRESA","S","DIRECCION_ADMINISTRATIVA","S","COMUNA_ADMINISTRATIVA","S","OBSERVACION_CONTACTO","S"
//            };
//            // Llamada a la BD
//            String json = ObtenerLib.getDefaultObtenerResoultSetExt(logger, connection,
//                    camposytipos, "VW_PROYECTO_EMPRESA" , jsonObj);
//            // Imprimir la salida
//            out.print(json);
//        } catch (SQLException e) {
//            logger.error("Error durante la consulta a la base de datos. ",e.fillInStackTrace());
//            out.print("{\"resultadoEjecucion\":\"NOK\",\"mensaje\":\"Ocurrio un error\",\"mensajeTec\":\"SQLException\"}");
//        } catch (GesvitaException ex) {
//            logger.error("Error de entrada : " + ex.getMessage());
//            String salidaNOK = "{\"resultadoEjecucion\":\"NOK\",\"mensaje\":\"Ocurrio un error\",\"mensajeTec\":\"Error en consulta a BD\"}";
//            out.print(salidaNOK);
//        } finally {
//            try {
//                if (connection!= null && !connection.isClosed()){
//                    connection.close();
//                }
//            } catch (SQLException sqle) {
//                logger.error(sqle.getMessage(), sqle.fillInStackTrace());
//            }
//            try {
//                datasource.close();
//            } catch (Exception e) {
//                logger.error(e.getMessage(), e.fillInStackTrace());
//            }
//        }
    }

    private boolean validaFormatoRut( String in ) {
        Pattern pattern = Pattern.compile("^[0-9]+[0-9kK]{1}$");
        Matcher matcher = pattern.matcher(in);
        return (matcher.matches() == true); 
    }
    
    private String leerCampo(JsonObject json, String field) throws  GesvitaException {
        JsonElement data = json.get(field);
        if (data == null || data instanceof JsonArray  || data instanceof JsonObject ) {
            throw new GesvitaException("Campo '" + field + "' era esparado como cadena");
        }
        
        String s = data.toString();
        if (s == null) return "";
        if (s.length() >2 && ( (s.startsWith("\"") && s.endsWith("\""))
                             || (s.startsWith("\'") && s.endsWith("\'")) ) )
            return s.substring(1,s.length()-1);
        else
            return s;
    }
}
