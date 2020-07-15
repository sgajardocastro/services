package cl.gesvita.ws.insertarsolicitudproyecto2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;

import oracle.jdbc.OracleTypes;
import oracle.sql.CLOB;

import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import java.text.SimpleDateFormat;

import java.util.Date;
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
import com.google.gson.JsonParser;
import com.google.gson.stream.MalformedJsonException;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.util.Properties;
import java.util.regex.PatternSyntaxException;



/**
 * Servlet implementation class InsertarSolicitudProyecto2
 */
@WebServlet("/WSInsertarSolicitudProyectoBAK/InsertarSolicitudProyectoBAK")
public class InsertarSolicitudProyecto2 extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private class MalFormedInput extends Exception {
        public MalFormedInput(String msg) { super(msg); }
    }
    
    static Logger logger = Logger.getLogger(InsertarSolicitudProyecto2.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public InsertarSolicitudProyecto2() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String sEstado1 = "";
        Connection conn = null;
        CallableStatement stmt = null;
        PreparedStatement stmtp = null;
        String contenidoArray="";
        Gson gson = new Gson();
        Map<String, Object> dataRetornar = new HashMap<String, Object>();
        Clob clob = null;

        response.setContentType("application/json");
        String requestS = "";

        PrintWriter out = response.getWriter();
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        Properties prop = new Properties();
        Properties propsLog4 = new Properties();
        PoolProperties p = new PoolProperties();

        InputStream inputStreamLog4 = this.getClass().getResourceAsStream("log4j.properties");
        propsLog4.load(inputStreamLog4);
        PropertyConfigurator.configure(propsLog4);
        String archivoPro = System.getProperty("catalina.base") + "/webapps/propiedades/db.properties";

        prop.load(new FileInputStream(archivoPro));

        p.setUrl(prop.getProperty("urlConexion"));
        p.setDriverClassName(prop.getProperty("driverClass"));
        p.setUsername(prop.getProperty("userName"));
        p.setPassword(prop.getProperty("password"));
        p.setJmxEnabled(Boolean.parseBoolean(prop.getProperty("jmxEnabled")));
        p.setTestWhileIdle(Boolean.parseBoolean(prop.getProperty("testWhileIdle")));
        p.setTestOnBorrow(Boolean.parseBoolean(prop.getProperty("testOnBorrow")));
        p.setValidationQuery(prop.getProperty("validationQuery"));
        p.setTestOnReturn(Boolean.parseBoolean(prop.getProperty("testOnReturn")));
        p.setValidationInterval(Long.parseLong(prop.getProperty("validationInterval")));
        p.setTimeBetweenEvictionRunsMillis(Integer.parseInt(prop.getProperty("timeBetweenEvictionRunsMillis")));
        p.setMaxActive(Integer.parseInt(prop.getProperty("maxActive")));
        p.setInitialSize(Integer.parseInt(prop.getProperty("initialSize")));
        p.setMaxWait(Integer.parseInt(prop.getProperty("maxWait")));
        p.setRemoveAbandonedTimeout(Integer.parseInt(prop.getProperty("removeAbandonedTimeout")));
        p.setMinEvictableIdleTimeMillis(Integer.parseInt(prop.getProperty("minEvictableIdleTimeMillis")));
        p.setMinIdle(Integer.parseInt(prop.getProperty("minIdle")));
        p.setLogAbandoned(Boolean.parseBoolean(prop.getProperty("logAbandoned")));
        p.setRemoveAbandoned(Boolean.parseBoolean(prop.getProperty("removeAbandoned")));
        p.setJdbcInterceptors(
                "org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"+
                "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");

        DataSource datasource = new DataSource();
        datasource.setPoolProperties(p);
        Properties propSharepoint = readArchivoProperties();

        Map<String, Object> dataIndividual = null;
        Map<String, Object> dataGrupal = null;

//        Map<String, String> m = new HashMap<String, String>();
        JsonObject input = null;

        try {
            StringBuilder sb = new StringBuilder();
            String s;

            while ((s = request.getReader().readLine()) != null) {
                sb.append(s);
            }
            //obtener json a array
            requestS = sb.toString();
            
            JsonParser parser = new JsonParser();
            input = (JsonObject) parser.parse(requestS);
            
            
        // int buscarEstado1 = requestS.toLowerCase().indexOf("in_data_adjuntos");
          // BYSECURITY logger.info("JSON cargado");
        } catch (NullPointerException e1) {
            logger.error("Ha ocurrido un error de tipo NullPointer. " + e1.getMessage());
        } catch (IllegalArgumentException e2) {
            logger.error("Ha ocurrido un error de tipo IllegalArgument. " + e2.getMessage());
        } catch (IndexOutOfBoundsException e3) {
            logger.error("Ha ocurrido un error de tipo IndexOutOfBounds. "+e3.getMessage());
        } catch (MalformedJsonException ex) {
            logger.error("Ha ocurrido un error de tipo MalformedJsonException. " + ex.getMessage());
            out.print("{\"resultadoEjecucion\":\"NOK\",\"mensaje\":\"Ocurrio un error\",\"mensajeTec\":\"Json mal formado\"}");
            return;
//        } catch (Exception e4){
//            logger.error("Ha ocurrido un error. " + e4.getMessage());
        }
        long id_cache = 0;
        long id_solicitud = 0;
        try {
            conn = datasource.getConnection();
            stmt = conn.prepareCall("{call PKG_MONITOR_INSERT.PROYECTO_SOLICITUD(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");
            clob = conn.createClob();
            stmt.setFloat(1, readInputFloat( input , "in_id_tiposolicitud"));
            stmt.setFloat(2, readInputFloat( input , "in_id_subtipo"));
            stmt.setString(3, readInputString( input, "in_descripcion_solicitud"));
            stmt.setFloat(4, readInputFloat( input , "in_id_estadosolicitud"));
            stmt.setFloat(5, readInputFloat( input , "in_id_usuario_creacion"));
            stmt.setFloat(6, readInputFloat( input , "in_id_empresa_cliente"));
            stmt.setFloat(7, readInputFloat( input , "in_id_area_cliente"));
            stmt.setString(8, readInputString( input, "in_nombre_proyecto_cliente"));
            stmt.setFloat(9, readInputFloat( input , "in_id_usuario_contacto"));
            stmt.setString(10, readInputString( input, "in_flag_focosolicitud"));
            stmt.setFloat(11, readInputFloat( input , "in_id_proyecto"));
            stmt.setFloat(12, readInputFloat( input , "in_id_sucursal"));
            stmt.setFloat(13, readInputFloat( input , "in_id_tarea"));
            //stmt.setString(14, sEstado1.replaceAll("\\s",""));
            stmt.setString(14, input.get("in_data_adjuntos").toString().trim());
            stmt.registerOutParameter(15, Types.NUMERIC);
            stmt.registerOutParameter(16, Types.NUMERIC);
            stmt.registerOutParameter(17, Types.VARCHAR);
            stmt.execute();
            id_solicitud = stmt.getInt(15);
            dataRetornar.put("out_id_solicitud", id_solicitud);
            dataRetornar.put("out_codigo", stmt.getInt(16));
            dataRetornar.put("out_mensaje", stmt.getString(17).toString());
            String json = gson.toJson(dataRetornar);
            // Recuperar ID_CACHE
            try {
                stmtp = conn.prepareStatement("SELECT TO_NUMBER(ID_DOCUMENTO_ENREPOSITORIO) as ID_CACHE" +
                           " FROM tb_proyecto_documento" +
                           " WHERE id_proyecto = ?" +
                           " AND TIPO_DOCUMENTO = 'CARPETA'" +
                           " AND REGEXP_LIKE( ID_DOCUMENTO_ENREPOSITORIO, '^\\d+$' )");
                stmtp.setLong(1, id_solicitud);
                ResultSet rs = stmtp.executeQuery();
                if (rs.next()){
                    id_cache = rs.getLong(1);
                }
            } catch (SQLException e) {
                    String cl = e.getClass().toString();
                    logger.error(cl,e.fillInStackTrace());
            }
            // Lanzar la creación de las carpetas
            callAPP(propSharepoint, id_cache);
            response.setHeader("access-control-allow-origin", "*");
            out.print(json);
        } catch (Exception e) {
            logger.error(e.fillInStackTrace());
        } finally {
            if (stmt != null){
                try {
                    stmt.close();
                } catch (SQLException e) {
                    logger.error(e.fillInStackTrace());
                }
            }
            if (conn != null){
                try {
                    conn.close();
                } catch (SQLException e) {
                    logger.error(e.fillInStackTrace());
                }
            }
            if (datasource != null) {
                try {
                    datasource.close();
                }
                catch (Exception e) {
                    logger.error(e.fillInStackTrace());
                }
            }
        }
        if (id_cache != 0) {
            
        }
    }

    private String readInputString( JsonObject input , String param ) throws MalFormedInput {
            JsonElement value = input.get(param);
            if (value instanceof JsonArray) {
                throw new MalFormedInput("Expected a String not a JSONArray for field: " + param);
            } else if (value instanceof JsonObject) {
                throw new MalFormedInput("Expected a String not a JSONObject for field: " + param);
            } else {
                String result = value.toString();
                if (result.startsWith("\"") && result.endsWith("\"")) {
                    result = result.substring(1,result.length()-1);
                }
                if (result.startsWith("'") && result.endsWith("'")) {
                    result = result.substring(1,result.length()-1);
                }
                return result;
            }
    }

    private float readInputFloat( JsonObject input , String param ) throws MalFormedInput {
        String str = "";
        try {
            str = readInputString(input, param);
           return Float.parseFloat(str);
        } catch (NumberFormatException e) {
            throw new MalFormedInput("Expected a Number for field: " + param + " (" + str + ")");
        }
    }
    
    private Properties readArchivoProperties() {
        String archivoPro;
        Properties prop = new Properties();
        try {
            archivoPro = System.getProperty("catalina.base") + "/webapps/propiedades/archivo.properties";
            prop.load(new FileInputStream(archivoPro));
        } catch (IOException e) {
            try {
                archivoPro = System.getProperty("catalina.base") + "/webapps/propiedades/archivos.properties";
                prop.load(new FileInputStream(archivoPro));
            } catch (IOException e2) {
                logger.error("Error durante el proceso del archivo: ", e.fillInStackTrace());
                logger.error("Error durante el proceso del archivo: ", e2.fillInStackTrace());
            }
        }
        return prop;
    }
    
    private void callAPP(Properties prop, long id_cache) {
        if (id_cache != 0) {
            try {
                String app = prop.getProperty("appsharepoint.app");
                if (app != null) {
                    String cmd[] = {"" + app,
                        "" + id_cache}; // , " >/dev/null 2>" + prop.getProperty("appsharepoint.log");
                    // BYSECURITY logger.info("Calling : " + cmd.toString());
                    Process p = Runtime.getRuntime().exec(cmd);
                    // BYSECURITY logger.info("Called");
                }
            } catch (IOException ex) {
                logger.error("Error al lanzar proceso de copia a sharepoint");
            }
        }
    }

}
