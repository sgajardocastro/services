package cl.gesvita.ws.instanciaproyecto;

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
 * Servlet implementation class InstanciaProyecto
 */
@WebServlet("/WSInstanciaProyecto/InstanciaProyecto")
public class InstanciaProyecto extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(InstanciaProyecto.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public InstanciaProyecto() {
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
		Clob clob = null;
		
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
            stmt = conn.prepareCall("{call PKG_PROYECTO_INSTANCIAR.INSTANCIAPROYECTO(?,?,to_date(?, \'DD/MM/YYYY\'),?,?,?,?)}");
                        clob = conn.createClob();
                        stmt.setFloat(1, Float.parseFloat(readFieldString(jsonObj,"in_id_proyecto_template")));
                        stmt.setString(2, readFieldString(jsonObj,"in_nombre_instanciaproy"));
                        stmt.setString(3, readFieldString(jsonObj,"in_fecha_inicio"));
                        stmt.registerOutParameter(4, Types.NUMERIC);
                        stmt.registerOutParameter(5, Types.NUMERIC);
                        stmt.registerOutParameter(6, Types.NUMERIC);
                        stmt.registerOutParameter(7, Types.VARCHAR);
                        stmt.execute();
                        dataRetornar.put("out_id_proyecto_new", stmt.getInt(4));
                        dataRetornar.put("out_cant_nuevastareas", stmt.getInt(5));
                        dataRetornar.put("out_codigo", stmt.getInt(6));
                        dataRetornar.put("out_mensaje", stmt.getString(7).toString());
                sendData(response, out , dataRetornar);

        } catch (GesvitaException ex) {
            logger.error(ex.fillInStackTrace());
            dataRetornar.put("out_codigo", 9999);
            dataRetornar.put("out_mensaje", "Parámetro JSON no esperado");
            sendData(response, out, dataRetornar);
        } catch (SQLException e) {
            logger.error(e.fillInStackTrace());
            dataRetornar.put("out_codigo", 9998);
            dataRetornar.put("out_mensaje", "SQLException");
			sendData(response, out , dataRetornar);
        } catch (Exception e) {
            logger.error(e.fillInStackTrace());
            dataRetornar.put("out_codigo", 9999);
            dataRetornar.put("out_mensaje", "Exception");
			sendData(response, out , dataRetornar);
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
                } catch (Exception e) {
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
