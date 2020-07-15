package cl.gesvita.ws.insertarsolicitudproyectov2;

import cl.gesvita.ws.obtener.exception.GesvitaException;
import cl.gesvita.ws.obtener.lib.ObtenerLib;
import cl.gesvita.ws.subirarchivoshp.util.CallProcess;
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
 * Servlet implementation class InsertarSolicitudProyecto
 */
@WebServlet("/WSInsertarSolicitudProyectoV2/InsertarSolicitudProyectoV2")
public class InsertarSolicitudProyectoV2 extends HttpServlet {

    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(InsertarSolicitudProyectoV2.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public InsertarSolicitudProyectoV2() {
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
            stmt = conn.prepareCall("{call PKG_MONITOR_INSERT.PROYECTO_SOLICITUD(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");
            clob = conn.createClob();
            stmt.setFloat(1, Float.parseFloat(readFieldString(jsonObj, "in_id_tiposolicitud")));
            stmt.setFloat(2, Float.parseFloat(readFieldString(jsonObj, "in_id_subtipo")));
            stmt.setString(3, readFieldString(jsonObj, "in_descripcion_solicitud"));
            stmt.setFloat(4, Float.parseFloat(readFieldString(jsonObj, "in_id_estadosolicitud")));
            stmt.setFloat(5, Float.parseFloat(readFieldString(jsonObj, "in_id_usuario_creacion")));
            stmt.setFloat(6, Float.parseFloat(readFieldString(jsonObj, "in_id_empresa_cliente")));
            stmt.setFloat(7, Float.parseFloat(readFieldString(jsonObj, "in_id_area_cliente")));
            stmt.setString(8, readFieldString(jsonObj, "in_nombre_proyecto_cliente"));
            stmt.setFloat(9, Float.parseFloat(readFieldString(jsonObj, "in_id_usuario_contacto")));
            stmt.setString(10, readFieldString(jsonObj, "in_flag_focosolicitud"));
            stmt.setFloat(11, Float.parseFloat(readFieldString(jsonObj, "in_id_proyecto")));
            stmt.setFloat(12, Float.parseFloat(readFieldString(jsonObj, "in_id_sucursal")));
            stmt.setFloat(13, Float.parseFloat(readFieldString(jsonObj, "in_id_tarea")));
            clob.setString(1, readFieldString(jsonObj, "in_data_adjuntos"));
            stmt.setClob(14, clob);
            stmt.registerOutParameter(15, Types.NUMERIC);
            stmt.registerOutParameter(16, Types.NUMERIC);
            stmt.registerOutParameter(17, Types.VARCHAR);
            stmt.execute();
            long id_solicitud = stmt.getLong(15);
            dataRetornar.put("out_id_solicitud", id_solicitud);
            int id_code = stmt.getInt(16);
            dataRetornar.put("out_codigo", id_code);
            dataRetornar.put("out_mensaje", stmt.getString(17).toString());
            
            // Lanzar al proceso que copia a sharepoint
            if (id_code == 0) {
                long id_cache = CallProcess.getIdCacheCarpeta(logger, conn, id_solicitud);
                // BYSECURITY logger.info("Invocar llamada con id " + id_solicitud + ", id_cache = " + id_cache);
                CallProcess.callCopy(logger, id_cache);
            }

            sendData(response, out, dataRetornar);

        } catch (GesvitaException ex) {
            logger.error(ex.fillInStackTrace());
            dataRetornar.put("out_codigo", 9999);
            dataRetornar.put("out_mensaje", "Parámetro JSON no esperado");
            sendData(response, out, dataRetornar);
        } catch (SQLException e) {
            logger.error(e.fillInStackTrace());
            dataRetornar.put("out_codigo", 9998);
            dataRetornar.put("out_mensaje", "SQLException");
            sendData(response, out, dataRetornar);
        } catch (Exception e) {
            logger.error(e.fillInStackTrace());
            dataRetornar.put("out_codigo", 9999);
            dataRetornar.put("out_mensaje", "Exception");
            sendData(response, out, dataRetornar);
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
            JsonArray x = (JsonArray) value;
            return x.toString();
            // throw new GesvitaException("Field: " + field + " is not spected as JsonArray");
        } else if (value instanceof JsonObject) {
            JsonObject x = (JsonObject) value;
            return x.toString();
        } else {
            String salida = value.toString();
            if (salida.startsWith("\"") && salida.endsWith("\"")) {
                salida = salida.substring(1, salida.length() - 1);
            } else if (salida.startsWith("'") && salida.endsWith("'")) {
                salida = salida.substring(1, salida.length() - 1);
            }
            return salida;
        }
    }
}
