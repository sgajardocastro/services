package cl.gesvita.ws.actualizarproyecto;

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
 * Servlet implementation class ActualizarProyecto
 */
@WebServlet("/WSActualizarProyecto/ActualizarProyecto")
public class ActualizarProyecto extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(ActualizarProyecto.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ActualizarProyecto() {
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
		Clob clob2 = null;
		
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
            stmt = conn.prepareCall("{call PKG_MONITOR_UPDATE.PROYECTO_SETUP(?,?,?,?,?,?,?,?,?,?,?,?,?,to_date(?, \'DD/MM/YYYY\'),to_date(?, \'DD/MM/YYYY\'),to_date(?, \'DD/MM/YYYY\'),to_date(?, \'DD/MM/YYYY\'),?,?,?,?,?)}");
                        clob = conn.createClob();
                        clob2 = conn.createClob();
                        stmt.setFloat(1, Float.parseFloat(readFieldString(jsonObj,"in_id_proyecto")));
                        stmt.setFloat(2, Float.parseFloat(readFieldString(jsonObj,"in_id_estado")));
                        stmt.setString(3, readFieldString(jsonObj,"in_nombre_proyecto"));
                        stmt.setString(4, readFieldString(jsonObj,"in_id_usuario_jefe_proyecto"));
                        stmt.setFloat(5, Float.parseFloat(readFieldString(jsonObj,"in_id_programa")));
                        stmt.setFloat(6, Float.parseFloat(readFieldString(jsonObj,"in_id_empresa")));
                        clob.setString(1, readFieldString(jsonObj,"in_objetivo_proyecto"));
                        stmt.setClob(7, clob);
                        stmt.setFloat(8, Float.parseFloat(readFieldString(jsonObj,"in_id_tipo")));
                        stmt.setFloat(9, Float.parseFloat(readFieldString(jsonObj,"in_id_magnitud")));
                        stmt.setFloat(10, Float.parseFloat(readFieldString(jsonObj,"in_id_prioridad")));
                        stmt.setFloat(11, Float.parseFloat(readFieldString(jsonObj,"in_id_sponsor_area")));
                        stmt.setFloat(12, Float.parseFloat(readFieldString(jsonObj,"in_id_usuario_sponsor")));
                        stmt.setFloat(13, Float.parseFloat(readFieldString(jsonObj,"in_id_ppto")));
                        stmt.setString(14, readFieldString(jsonObj,"in_fecha_plan_ini"));
                        stmt.setString(15, readFieldString(jsonObj,"in_fecha_plan_fin"));
                        stmt.setString(16, readFieldString(jsonObj,"in_fecha_real_ini"));
                        stmt.setString(17, readFieldString(jsonObj,"in_fecha_real_fin"));
                        clob2.setString(1, readFieldString(jsonObj,"in_observacion_proyecto"));
                        stmt.setClob(18, clob2);
                        stmt.setFloat(19, Float.parseFloat(readFieldString(jsonObj,"in_id_comportamiento")));
                        stmt.setFloat(20, Float.parseFloat(readFieldString(jsonObj,"in_id_area")));
                        stmt.registerOutParameter(21, Types.NUMERIC);
                        stmt.registerOutParameter(22, Types.VARCHAR);
                        stmt.execute();
                        dataRetornar.put("out_codigo", stmt.getInt(21));
                        dataRetornar.put("out_mensaje", stmt.getString(22).toString());
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
        JsonElement value = jsonObj.get(field.toLowerCase());
        if (value instanceof JsonArray) {
            throw new GesvitaException("Field: " + field + " is not spected as JsonArray");
        } else if (value instanceof JsonObject) {
            throw new GesvitaException("Field: " + field + " is not spected as JsonObject");
        } else {
            String salida = value.toString();
            if (salida.startsWith("\"") && salida.endsWith("\""))
                salida = salida.substring(1, salida.length()-1);
            else if (salida.startsWith("'") && salida.endsWith("'"))
                salida = salida.substring(1, salida.length()-1);
            return salida;
        }
    }
}
