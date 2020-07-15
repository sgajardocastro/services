package cl.gesvita.ws.editarempresa;

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
 * Servlet implementation class EditarEmpresa
 */
@WebServlet("/WSEditarEmpresa/EditarEmpresa")
public class EditarEmpresa extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(EditarEmpresa.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public EditarEmpresa() {
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
            stmt = conn.prepareCall("{call PKG_MONITOR_UPDATE.PROYECTO_EMPRESA(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");
                        clob = conn.createClob();
                        stmt.setFloat(1, Float.parseFloat(readFieldString(jsonObj,"in_id_empresa")));
                        stmt.setString(2, readFieldString(jsonObj,"in_nombre_empresa"));
                        stmt.setFloat(3, Float.parseFloat(readFieldString(jsonObj,"in_id_pais")));
                        stmt.setString(4, readFieldString(jsonObj,"in_rut_empresa"));
                        stmt.setString(5, readFieldString(jsonObj,"in_razon_social"));
                        stmt.setString(6, readFieldString(jsonObj,"in_zona_horaria"));
                        stmt.setString(7, readFieldString(jsonObj,"in_estado_empresa"));
                        stmt.setFloat(8, Float.parseFloat(readFieldString(jsonObj,"in_id_tipo_cambio")));
                        stmt.setFloat(9, Float.parseFloat(readFieldString(jsonObj,"in_id_representante_legal")));
                        stmt.setString(10, readFieldString(jsonObj,"in_giro_empresa"));
                        stmt.setString(11, readFieldString(jsonObj,"in_direccion_empresa"));
                        stmt.setString(12, readFieldString(jsonObj,"in_nombre_contacto"));
                        stmt.setString(13, readFieldString(jsonObj,"in_rol_contacto"));
                        stmt.setString(14, readFieldString(jsonObj,"in_fono_contacto"));
                        stmt.setString(15, readFieldString(jsonObj,"in_email_empresa"));
                        stmt.setString(16, readFieldString(jsonObj,"in_latitud_empresa"));
                        stmt.setString(17, readFieldString(jsonObj,"in_longitud_empresa"));
                        stmt.setFloat(18, Float.parseFloat(readFieldString(jsonObj,"in_id_empresa_padre")));
                        stmt.setString(19, readFieldString(jsonObj,"in_comuna_empresa"));
                        stmt.setString(20, readFieldString(jsonObj,"in_direccion_administrativa"));
                        stmt.setString(21, readFieldString(jsonObj,"in_comuna_administrativa"));
                        stmt.setString(22, readFieldString(jsonObj,"in_observacion_contacto"));
                        stmt.registerOutParameter(23, Types.NUMERIC);
                        stmt.registerOutParameter(24, Types.VARCHAR);
                        stmt.execute();
                        dataRetornar.put("out_codigo", stmt.getInt(23));
                        dataRetornar.put("out_mensaje", stmt.getString(24).toString());
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
