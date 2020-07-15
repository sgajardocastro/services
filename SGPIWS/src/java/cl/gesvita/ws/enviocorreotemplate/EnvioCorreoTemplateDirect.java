package cl.gesvita.ws.enviocorreotemplate;

import cl.gesvita.util.ClobUtil;
import cl.gesvita.ws.enviocorreo.BusinessEnviarCorreo;
import cl.gesvita.ws.obtener.exception.GesvitaException;
import cl.gesvita.ws.obtener.lib.ObtenerLib;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

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

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.log4j.Logger;

/**
 * Servlet implementation class EnvioCorreoTemplateDirect
 */
@WebServlet("/WSEnvioCorreoTemplateDirect/EnvioCorreoTemplateDirect")
public class EnvioCorreoTemplateDirect extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(EnvioCorreoTemplateDirect.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public EnvioCorreoTemplateDirect() {
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

        Connection conn = null;
        CallableStatement stmt = null;
        Map<String, Object> dataRetornar = new HashMap<>();

        response.setContentType("application/json");

        PrintWriter out = response.getWriter();
        DataSource datasource;
        JsonObject jsonObj;
        Clob clob;
        String retu;
        String fileComplet = "";
        String subjectNotificacion= "";
        int codigo_retorno = -1;
		
        try {
            // Inicializar Log4J
            ObtenerLib.setLogParam(this.getClass());
            // Obtener Data Source
            datasource = ObtenerLib.getDataSource(logger);
        } catch (GesvitaException ex) {
            logger.error(ex.fillInStackTrace());
            dataRetornar.put("out_codigo", 9999);
            dataRetornar.put("out_mensaje", "Error en conexión a BD");
            sendData(response, out, dataRetornar);
            return;
        }
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
        try {
            conn = datasource.getConnection();
            stmt = conn.prepareCall("{call PKG_MONITOR_UTILITIES.CREATE_MAIL_BODY(?,?,?,?,?,?)}");
            clob = conn.createClob();
            // in_id_notificacion 
            stmt.setFloat(1, Float.parseFloat(readFieldString(jsonObj,"id_notificacion")));
            // in_nombre_destinatario
            stmt.setString(2, readFieldString(jsonObj,"nombredestinatario"));
            stmt.registerOutParameter(3, Types.CLOB);
            stmt.registerOutParameter(4, Types.VARCHAR);
            stmt.registerOutParameter(5, Types.NUMERIC);
            stmt.registerOutParameter(6, Types.VARCHAR);
            stmt.execute();
            fileComplet = ClobUtil.clobToString(stmt.getClob(3));
            retu = stmt.getString(6);
            codigo_retorno = stmt.getInt(5);
            subjectNotificacion = stmt.getString(4);
            
//            dataRetornar.put("out_subject", stmt.getString(4).toString());
//            dataRetornar.put("out_codigo", stmt.getInt(5));
//            dataRetornar.put("out_mensaje", stmt.getString(6).toString());
//            sendData(response, out , dataRetornar);

        } catch (GesvitaException ex) {
            logger.error(ex.fillInStackTrace());
            dataRetornar.put("out_codigo", 9999);
            dataRetornar.put("out_mensaje", "Parámetro JSON no esperado");
            sendData(response, out, dataRetornar);
            return;
        } catch (SQLException e) {
            logger.error(e.fillInStackTrace());
            dataRetornar.put("out_codigo", 9998);
            dataRetornar.put("out_mensaje", "SQLException");
            sendData(response, out , dataRetornar);
            return;
        } catch (Exception e) {
            logger.error(e.fillInStackTrace());
            dataRetornar.put("out_codigo", 9999);
            dataRetornar.put("out_mensaje", "Exception");
	    sendData(response, out , dataRetornar);
            return;
        } finally {
            if (datasource != null) {
                try {
                    datasource.close();
                } catch (Exception e) {
                    logger.error(e.fillInStackTrace());
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    logger.error(e.fillInStackTrace());
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    logger.error(e.fillInStackTrace());
                }
            }
        }
        
        if (codigo_retorno == 0) {
            //llamar a WS de envio de correo
            try {
                BusinessEnviarCorreo envi = new BusinessEnviarCorreo();
                String subj = "Notificación: [" + subjectNotificacion + "]";
                envi.Enviar(readFieldString(jsonObj,"varmail"), subj, fileComplet);
                dataRetornar.put("out_codigo", "OK");
                dataRetornar.put("codigoError", 0);
                dataRetornar.put("out_mensaje", "Correo enviado");
                dataRetornar.put("mensajeEjecucion", "Correo enviado");
                sendData(response, out , dataRetornar);
            } catch (Exception ee) {
                logger.error(ee.fillInStackTrace());
                dataRetornar.put("codigoError", 2);
                dataRetornar.put("out_mensaje", "No se ha podido enviar el correo");
                dataRetornar.put("mensajeEjecucion", "Correo no enviado");
                sendData(response, out , dataRetornar);
            }
        } else {
            dataRetornar.put("codigoError", 1);
            logger.error(retu);
            dataRetornar.put("mensajeEjecucion", "No existe información a enviar");
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

