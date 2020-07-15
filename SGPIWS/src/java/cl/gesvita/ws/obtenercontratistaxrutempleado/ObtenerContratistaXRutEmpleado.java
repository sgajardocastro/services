
package cl.gesvita.ws.obtenercontratistaxrutempleado;

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

@WebServlet("/WSObtenerContratistaXRutEmpleado/ObtenerContratistaXRutEmpleado")

public class ObtenerContratistaXRutEmpleado extends HttpServlet{
    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(ObtenerContratistaXRutEmpleado.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ObtenerContratistaXRutEmpleado() {
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


        try {


            String fieldName = "rut_empleado";
            String in_rut_empleado = leerCampo(jsonObj,fieldName);
            
            // ,\"id\":\""+ id_usuario + "\"
            String salida = "{\"id_empresa\":\"1257\",\"nombre_empresa\":\"PROESSA\",\"rut_empresa\":\"76619579-2\"}";
            
            
            if (!validaFormatoRut(in_rut_empleado))
                throw new GesvitaException("Error en formato de campo '" + fieldName+ "'"); // , '" + id_usuario+ "'
            
            out.print(salida);
            
        } catch (GesvitaException ex) {
            logger.error(ex.getMessage());
            out.print("{\"resultadoEjecucion\":\"NOK\",\"mensaje\":\"" + ex.getMessage() + "\",\"mensajeTec\":\"" + ex.getMessage() + "\"}");
            return;
        }
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
