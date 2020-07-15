package cl.gesvita.ws.obtenersolicitudrequerimientos;

import cl.gesvita.ws.obtener.lib.ObtenerLib;
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

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.util.Properties;
import java.util.regex.PatternSyntaxException;


/**
 * Servlet implementation class ObtenerSolicitudRequerimientos
 */
@WebServlet("/WSObtenerSolicitudRequerimientos/ObtenerSolicitudRequerimientos")
public class ObtenerSolicitudRequerimientos extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(ObtenerSolicitudRequerimientos.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ObtenerSolicitudRequerimientos() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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

        Map<String, Object> dataIndividual = null;
        Map<String, Object> dataGrupal = null;
        Connection connection = null;
        Statement  stmt = null;
        ResultSet rs = null;
        Gson gson = new Gson();
        int totalRegistro=0;

        Map<String, String> m = new HashMap<String, String>();
        try {
            StringBuilder sb = new StringBuilder();

        String s;
        while ((s = request.getReader().readLine()) != null) {
            sb.append(s);
        }
        //obtener json a array
        requestS = sb.toString();

        if (requestS.length() > 0)
        {
            requestS = requestS.substring(0, sb.length() - 1);
            requestS = requestS.substring(1);
        }
        String[] splitStr = null;

        int iTot = 0;
        requestS = requestS.replace("\"", "");
        requestS = requestS.replace("\'", "");
        if (requestS.length() > 0)
        {
            if (requestS.contains(","))
            {
                splitStr = requestS.split(",");
                for (String val : splitStr)
                {
                    String[] srSpl = val.split(":");
                    m.put(srSpl[0].trim().toString(), srSpl[1].trim().toString());
                }
            }
            else
            {
                String[] srSpl = requestS.split(":");
                m.put(srSpl[0].trim().toString(), srSpl[1].trim().toString());
            }
        }
          // BYSECURITY logger.info("JSON cargado");
        }
        catch (NullPointerException e1)
        {
            logger.error("Ha ocurrido un error de tipo NullPointer. " + e1.getMessage());
        }
        catch (IllegalArgumentException e2)
        {
            logger.error("Ha ocurrido un error de tipo IllegalArgument. " + e2.getMessage());
        }
        catch (IndexOutOfBoundsException e3)
        {
            logger.error("Ha ocurrido un error de tipo IndexOutOfBounds. "+e3.getMessage());
        }
        catch (Exception e4)
        {
            logger.error("Ha ocurrido un error. " + e4.getMessage());
        }

        try {
            connection = datasource.getConnection();
            String[] camposytipos = {"ID_EMPRESA","I","NOMBRE_EMPRESA","S","ID_EMPRESA_PADRE","I","ID_AREA","I",
                "NOMBRE_AREA","S","ID_TIPOSOLICITUD","I","NOMBRE_SOLICITUD","S","ID_REQUERIMIENTO","I",
                "NOMBRE_REQUERIMIENTO","S","DESCRIPCION_REQUERIMIENTO","S","URL_FORMULARIO","S",
                "NOMBRE_IMAGEN_REQUERIMIENTO","S","NOMBRE_CONTEXTO_ORIGEN","S","TIPO_RECURSO_ORIGEN","S",
                "ID_POSICION_RELEVANCIA","I","DATA_CONFIGURACION","S"};
            String json = ObtenerLib.getDefaultObtenerResoultSet(logger, connection, camposytipos, "VW_SOLICITUD_REQUERIMIENTOS" , m);
//            stmt = connection.createStatement();
//            rs = stmt.executeQuery("SELECT * FROM VW_SOLICITUD_REQUERIMIENTOS WHERE "+
//"DECODE('"+m.get("id_empresa")+"', 'null', '*', ID_EMPRESA) = DECODE('"+m.get("id_empresa")+"', 'null', '*', "+m.get("id_empresa")+") AND DECODE('"+m.get("nombre_empresa")+"', 'null', '*', NOMBRE_EMPRESA) = DECODE('"+m.get("nombre_empresa")+"', 'null', '*', '"+m.get("nombre_empresa")+"') AND DECODE('"+m.get("id_empresa_padre")+"', 'null', '*', ID_EMPRESA_PADRE) = DECODE('"+m.get("id_empresa_padre")+"', 'null', '*', "+m.get("id_empresa_padre")+") AND DECODE('"+m.get("id_area")+"', 'null', '*', ID_AREA) = DECODE('"+m.get("id_area")+"', 'null', '*', "+m.get("id_area")+") AND DECODE('"+m.get("nombre_area")+"', 'null', '*', NOMBRE_AREA) = DECODE('"+m.get("nombre_area")+"', 'null', '*', '"+m.get("nombre_area")+"') AND DECODE('"+m.get("id_tiposolicitud")+"', 'null', '*', ID_TIPOSOLICITUD) = DECODE('"+m.get("id_tiposolicitud")+"', 'null', '*', "+m.get("id_tiposolicitud")+") AND DECODE('"+m.get("nombre_solicitud")+"', 'null', '*', NOMBRE_SOLICITUD) = DECODE('"+m.get("nombre_solicitud")+"', 'null', '*', '"+m.get("nombre_solicitud")+"') AND DECODE('"+m.get("id_requerimiento")+"', 'null', '*', ID_REQUERIMIENTO) = DECODE('"+m.get("id_requerimiento")+"', 'null', '*', "+m.get("id_requerimiento")+") AND DECODE('"+m.get("nombre_requerimiento")+"', 'null', '*', NOMBRE_REQUERIMIENTO) = DECODE('"+m.get("nombre_requerimiento")+"', 'null', '*', '"+m.get("nombre_requerimiento")+"') AND DECODE('"+m.get("descripcion_requerimiento")+"', 'null', '*', DESCRIPCION_REQUERIMIENTO) = DECODE('"+m.get("descripcion_requerimiento")+"', 'null', '*', '"+m.get("descripcion_requerimiento")+"') AND DECODE('"+m.get("url_formulario")+"', 'null', '*', URL_FORMULARIO) = DECODE('"+m.get("url_formulario")+"', 'null', '*', '"+m.get("url_formulario")+"') AND DECODE('"+m.get("nombre_imagen_requerimiento")+"', 'null', '*', NOMBRE_IMAGEN_REQUERIMIENTO) = DECODE('"+m.get("nombre_imagen_requerimiento")+"', 'null', '*', '"+m.get("nombre_imagen_requerimiento")+"') AND DECODE('"+m.get("nombre_contexto_origen")+"', 'null', '*', NOMBRE_CONTEXTO_ORIGEN) = DECODE('"+m.get("nombre_contexto_origen")+"', 'null', '*', '"+m.get("nombre_contexto_origen")+"') AND DECODE('"+m.get("tipo_recurso_origen")+"', 'null', '*', TIPO_RECURSO_ORIGEN) = DECODE('"+m.get("tipo_recurso_origen")+"', 'null', '*', '"+m.get("tipo_recurso_origen")+"') AND DECODE('"+m.get("id_posicion_relevancia")+"', 'null', '*', ID_POSICION_RELEVANCIA) = DECODE('"+m.get("id_posicion_relevancia")+"', 'null', '*', "+m.get("id_posicion_relevancia")+") AND DECODE('"+m.get("data_configuracion")+"', 'null', '*', DATA_CONFIGURACION) = DECODE('"+m.get("data_configuracion")+"', 'null', '*', '"+m.get("data_configuracion")+"')");

            response.setHeader("access-control-allow-origin", "*");
            out.print(json);

        } catch (SQLException e) {
            //System.out.println("Connection Failed! Check output console");
            logger.error("Error durante la consulta a la base de datos. "+e.getMessage());
            //e.printStackTrace();
        } finally {
            try {
                if (connection!= null && !connection.isClosed()){
                    connection.close();
                    connection= null;
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
