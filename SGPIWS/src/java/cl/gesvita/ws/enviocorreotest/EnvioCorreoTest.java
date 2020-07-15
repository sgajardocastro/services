package cl.gesvita.ws.enviocorreotest;

//import cl.gesvita.util.ClobUtil;
import cl.gesvita.ws.enviocorreo.BusinessEnviarCorreo;
import cl.gesvita.ws.obtener.exception.GesvitaException;
import cl.gesvita.ws.obtener.lib.ObtenerLib;
import java.io.IOException;
import java.io.PrintWriter;
//import java.sql.CallableStatement;
//import java.sql.Clob;
//import java.sql.Connection;
//import java.sql.SQLException;
//import java.sql.Types;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

//import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.log4j.Logger;

/**
 * Servlet implementation class EnvioCorreoTest
 */
@WebServlet("/WSEnvioCorreoTest/EnvioCorreoTest")
public class EnvioCorreoTest extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(EnvioCorreoTest.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public EnvioCorreoTest() {
        super();
    }

    /**
     * @param request
     * @param response
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     * response)
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

//        Connection conn = null;
//        CallableStatement stmt = null;
        Map<String, Object> dataRetornar = new HashMap<>();

        response.setContentType("application/json");

        PrintWriter out = response.getWriter();
//        DataSource datasource;
        JsonObject jsonObj;
//        Clob clob;
//        String retu;
//        String fileComplet = "";
//        String subjectNotificacion= "";
//        int codigo_retorno = -1;
		
//        try {
//            // Inicializar Log4J
//            ObtenerLib.setLogParam(this.getClass());
//            // Obtener Data Source
//            // datasource = ObtenerLib.getDataSource(logger);
//        } catch (GesvitaException ex) {
//            logger.error(ex.fillInStackTrace());
//            dataRetornar.put("out_codigo", 9999);
//            dataRetornar.put("out_mensaje", "Error en conexión a BD");
//            sendData(response, out, dataRetornar);
//            return;
//        }
        try {
            // Leer la entrada
            jsonObj = ObtenerLib.readInput(logger, request);
        } catch (GesvitaException ex) {
            logger.error(ex.getMessage());
            dataRetornar.put("out_codigo", 9999);
            dataRetornar.put("out_mensaje", "Error de parseo");
            sendData(response, out, dataRetornar);
            return;
        }
        
        //llamar a WS de envio de correo
        try {
            BusinessEnviarCorreo envi = new BusinessEnviarCorreo();
            String para = "cruzrazeto@gmail.com";
            String subj = "Prueba Correo: EnvioCorreoTest";
            String texto = "Prueba Correo: EnvioCorreoTest";
            
            envi.Enviar(readFieldString(jsonObj,"varmail"), subj, texto);
            dataRetornar.put("out_codigo", "OK");
            dataRetornar.put("codigoError", 0);
            dataRetornar.put("out_mensaje", "Correo enviado");
            dataRetornar.put("mensajeEjecucion", "Correo enviado");
            sendData(response, out , dataRetornar);
        } catch (Exception ee) {
            logger.error(ee.fillInStackTrace());
            dataRetornar.put("codigoError", 2);
            dataRetornar.put("out_mensaje", "No se ha podido enviar el correo");
            dataRetornar.put("error", ee.fillInStackTrace());
            dataRetornar.put("mensajeEjecucion", "Correo no enviado");
            sendData(response, out , dataRetornar);
        }
    }

    private void sendData(HttpServletResponse response, PrintWriter out, Map<String, Object> dataRetornar) {
        Gson gson = new Gson();
        String json = gson.toJson(dataRetornar);
        response.setHeader("access-control-allow-origin", "*");
        out.print(json);
    }

    private String readFieldString(JsonObject jsonObj, String field) throws GesvitaException {
        String fieldlow = field.toLowerCase();
        if (!jsonObj.has(fieldlow))
            throw new GesvitaException("Expected field: '" + field + "' is not present in the input");
        JsonElement value = jsonObj.get(fieldlow);
        String salida = value.toString();
        if (!(value instanceof JsonArray) && !(value instanceof JsonObject)) {
            if (salida.startsWith("\"") && salida.endsWith("\""))
                salida = salida.substring(1, salida.length()-1);
            else if (salida.startsWith("'") && salida.endsWith("'"))
                salida = salida.substring(1, salida.length()-1);
        }
        return salida;
    }
}

