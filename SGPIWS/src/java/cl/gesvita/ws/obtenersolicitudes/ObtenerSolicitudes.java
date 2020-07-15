package cl.gesvita.ws.obtenersolicitudes;

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
 * Servlet implementation class ObtenerSolicitudes
 */
@WebServlet("/WSObtenerSolicitudes/ObtenerSolicitudes")
public class ObtenerSolicitudes extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(ObtenerSolicitudes.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ObtenerSolicitudes() {
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
            String[] camposytipos = {"ID_SOLICITUD","I","ID_EMPRESA","I","NOMBRE_SOLICITUD","S","ID_TIPOSOLICITUD","I",
                "NOMBRE_TIPO_SOLICITUD","S","ID_REQUERIMIENTO","I","NOMBRE_REQUERIMIENTO","S","DESCRIPCION_SOLICITUD","S",
                "ID_ESTADOSOLICITUD","I","NOMBRE_ESTADOSOLICITUD","S","ID_USUARIO_CREACION","I","NOMBRE_USUARIO_CREACION","S",
                "ID_USUARIO_JEFE_PROYECTO","I","NOMBRE_JEFEPROYECTO","S","ID_EMPRESA_CLIENTE","I","NOMBRE_EMPRESA_CLIENTE","S",
                "ID_AREA_CLIENTE","I","NOMBRE_AREA_CLIENTE","S","ID_USUARIO_CLIENTE","I","NOMBRE_USUARIO_CLIENTE","S",
                "ID_PRIORIDAD","I","NOMBRE_PRIORIDAD","S","ID_PROYECTO_PADRE","I","NOMBRE_PROYECTO_PADRE","S","ID_AREA","I",
                "NOMBRE_AREA","S","ID_SUCURSAL","I","NOMBRE_SUCURSAL","S","FECHA_CREACION","S","FECHA_PLAN_INI","S",
                "FECHA_PLAN_FIN","S","FECHA_PLAN_INI_DATE","D","FECHA_PLAN_FIN_DATE","D","FECHA_REAL_INI_DATE","D",
                "FECHA_REAL_FIN_DATE","D","DATA_EXTENDIDA","S","ID_FORMULARIO_PROYECTO","I","URL_FORMULARIO_PROYECTO","S",
                "CENTRO_COSTO_SUCURSAL","S","ID_TIPO","I","NOMBRE_TIPO","S","CODI_GRUPO_PROYECTO","S","NOMBRE_GRUPO_PROYECTO","S",
                "AVANCE_REAL_PROYECTO","I","AVANCE_PLAN_PORTIEMPO","I","KPI_PROYECTO_AVANCE_STATUS","I"};
            String json = ObtenerLib.getDefaultObtenerResoultSet(logger, connection, camposytipos, "VW_PROYECTO_SOLICITUD" , m);
//            stmt = connection.createStatement();
//            rs = stmt.executeQuery("SELECT * FROM VW_PROYECTO_SOLICITUD WHERE "+
//"DECODE('"+m.get("nombre_prioridad")+"', 'null', '*', NOMBRE_PRIORIDAD) = DECODE('"+m.get("nombre_prioridad")+"', 'null', '*', '"+m.get("nombre_prioridad")+"') AND DECODE('"+m.get("id_proyecto_padre")+"', 'null', '*', ID_PROYECTO_PADRE) = DECODE('"+m.get("id_proyecto_padre")+"', 'null', '*', "+m.get("id_proyecto_padre")+") AND DECODE('"+m.get("nombre_proyecto_padre")+"', 'null', '*', NOMBRE_PROYECTO_PADRE) = DECODE('"+m.get("nombre_proyecto_padre")+"', 'null', '*', '"+m.get("nombre_proyecto_padre")+"') AND DECODE('"+m.get("id_area")+"', 'null', '*', ID_AREA) = DECODE('"+m.get("id_area")+"', 'null', '*', "+m.get("id_area")+") AND DECODE('"+m.get("nombre_area")+"', 'null', '*', NOMBRE_AREA) = DECODE('"+m.get("nombre_area")+"', 'null', '*', '"+m.get("nombre_area")+"') AND DECODE('"+m.get("id_sucursal")+"', 'null', '*', ID_SUCURSAL) = DECODE('"+m.get("id_sucursal")+"', 'null', '*', "+m.get("id_sucursal")+") AND DECODE('"+m.get("nombre_sucursal")+"', 'null', '*', NOMBRE_SUCURSAL) = DECODE('"+m.get("nombre_sucursal")+"', 'null', '*', '"+m.get("nombre_sucursal")+"') AND DECODE('"+m.get("fecha_creacion")+"', 'null', '*', FECHA_CREACION) = DECODE('"+m.get("fecha_creacion")+"', 'null', '*', '"+m.get("fecha_creacion")+"') AND DECODE('"+m.get("fecha_plan_ini")+"', 'null', '*', FECHA_PLAN_INI) = DECODE('"+m.get("fecha_plan_ini")+"', 'null', '*', '"+m.get("fecha_plan_ini")+"') AND DECODE('"+m.get("fecha_plan_fin")+"', 'null', '*', FECHA_PLAN_FIN) = DECODE('"+m.get("fecha_plan_fin")+"', 'null', '*', '"+m.get("fecha_plan_fin")+"') AND DECODE('"+m.get("fecha_plan_ini_date")+"', 'null', '*', FECHA_PLAN_INI_DATE) = DECODE('"+m.get("fecha_plan_ini_date")+"', 'null', '*', to_date('"+m.get("fecha_plan_ini_date")+"', 'dd/mm/yyyy')) AND DECODE('"+m.get("fecha_plan_fin_date")+"', 'null', '*', FECHA_PLAN_FIN_DATE) = DECODE('"+m.get("fecha_plan_fin_date")+"', 'null', '*', to_date('"+m.get("fecha_plan_fin_date")+"', 'dd/mm/yyyy')) AND DECODE('"+m.get("fecha_real_ini_date")+"', 'null', '*', FECHA_REAL_INI_DATE) = DECODE('"+m.get("fecha_real_ini_date")+"', 'null', '*', to_date('"+m.get("fecha_real_ini_date")+"', 'dd/mm/yyyy')) AND DECODE('"+m.get("fecha_real_fin_date")+"', 'null', '*', FECHA_REAL_FIN_DATE) = DECODE('"+m.get("fecha_real_fin_date")+"', 'null', '*', to_date('"+m.get("fecha_real_fin_date")+"', 'dd/mm/yyyy')) AND DECODE('"+m.get("data_extendida")+"', 'null', '*', DATA_EXTENDIDA) = DECODE('"+m.get("data_extendida")+"', 'null', '*', '"+m.get("data_extendida")+"') AND DECODE('"+m.get("id_formulario_proyecto")+"', 'null', '*', ID_FORMULARIO_PROYECTO) = DECODE('"+m.get("id_formulario_proyecto")+"', 'null', '*', "+m.get("id_formulario_proyecto")+") AND DECODE('"+m.get("url_formulario_proyecto")+"', 'null', '*', URL_FORMULARIO_PROYECTO) = DECODE('"+m.get("url_formulario_proyecto")+"', 'null', '*', '"+m.get("url_formulario_proyecto")+"') AND DECODE('"+m.get("centro_costo_sucursal")+"', 'null', '*', CENTRO_COSTO_SUCURSAL) = DECODE('"+m.get("centro_costo_sucursal")+"', 'null', '*', '"+m.get("centro_costo_sucursal")+"') AND DECODE('"+m.get("id_tipo")+"', 'null', '*', ID_TIPO) = DECODE('"+m.get("id_tipo")+"', 'null', '*', "+m.get("id_tipo")+") AND DECODE('"+m.get("nombre_tipo")+"', 'null', '*', NOMBRE_TIPO) = DECODE('"+m.get("nombre_tipo")+"', 'null', '*', '"+m.get("nombre_tipo")+"') AND DECODE('"+m.get("codi_grupo_proyecto")+"', 'null', '*', CODI_GRUPO_PROYECTO) = DECODE('"+m.get("codi_grupo_proyecto")+"', 'null', '*', '"+m.get("codi_grupo_proyecto")+"') AND DECODE('"+m.get("nombre_grupo_proyecto")+"', 'null', '*', NOMBRE_GRUPO_PROYECTO) = DECODE('"+m.get("nombre_grupo_proyecto")+"', 'null', '*', '"+m.get("nombre_grupo_proyecto")+"') AND DECODE('"+m.get("id_solicitud")+"', 'null', '*', ID_SOLICITUD) = DECODE('"+m.get("id_solicitud")+"', 'null', '*', "+m.get("id_solicitud")+") AND DECODE('"+m.get("id_empresa")+"', 'null', '*', ID_EMPRESA) = DECODE('"+m.get("id_empresa")+"', 'null', '*', "+m.get("id_empresa")+") AND DECODE('"+m.get("nombre_solicitud")+"', 'null', '*', NOMBRE_SOLICITUD) = DECODE('"+m.get("nombre_solicitud")+"', 'null', '*', '"+m.get("nombre_solicitud")+"') AND DECODE('"+m.get("id_tiposolicitud")+"', 'null', '*', ID_TIPOSOLICITUD) = DECODE('"+m.get("id_tiposolicitud")+"', 'null', '*', "+m.get("id_tiposolicitud")+") AND DECODE('"+m.get("nombre_tipo_solicitud")+"', 'null', '*', NOMBRE_TIPO_SOLICITUD) = DECODE('"+m.get("nombre_tipo_solicitud")+"', 'null', '*', '"+m.get("nombre_tipo_solicitud")+"') AND DECODE('"+m.get("id_requerimiento")+"', 'null', '*', ID_REQUERIMIENTO) = DECODE('"+m.get("id_requerimiento")+"', 'null', '*', "+m.get("id_requerimiento")+") AND DECODE('"+m.get("nombre_requerimiento")+"', 'null', '*', NOMBRE_REQUERIMIENTO) = DECODE('"+m.get("nombre_requerimiento")+"', 'null', '*', '"+m.get("nombre_requerimiento")+"') AND DECODE('"+m.get("descripcion_solicitud")+"', 'null', '*', DESCRIPCION_SOLICITUD) = DECODE('"+m.get("descripcion_solicitud")+"', 'null', '*', '"+m.get("descripcion_solicitud")+"') AND DECODE('"+m.get("id_estadosolicitud")+"', 'null', '*', ID_ESTADOSOLICITUD) = DECODE('"+m.get("id_estadosolicitud")+"', 'null', '*', "+m.get("id_estadosolicitud")+") AND DECODE('"+m.get("nombre_estadosolicitud")+"', 'null', '*', NOMBRE_ESTADOSOLICITUD) = DECODE('"+m.get("nombre_estadosolicitud")+"', 'null', '*', '"+m.get("nombre_estadosolicitud")+"') AND DECODE('"+m.get("id_usuario_creacion")+"', 'null', '*', ID_USUARIO_CREACION) = DECODE('"+m.get("id_usuario_creacion")+"', 'null', '*', "+m.get("id_usuario_creacion")+") AND DECODE('"+m.get("nombre_usuario_creacion")+"', 'null', '*', NOMBRE_USUARIO_CREACION) = DECODE('"+m.get("nombre_usuario_creacion")+"', 'null', '*', '"+m.get("nombre_usuario_creacion")+"') AND DECODE('"+m.get("id_usuario_jefe_proyecto")+"', 'null', '*', ID_USUARIO_JEFE_PROYECTO) = DECODE('"+m.get("id_usuario_jefe_proyecto")+"', 'null', '*', "+m.get("id_usuario_jefe_proyecto")+") AND DECODE('"+m.get("nombre_jefeproyecto")+"', 'null', '*', NOMBRE_JEFEPROYECTO) = DECODE('"+m.get("nombre_jefeproyecto")+"', 'null', '*', '"+m.get("nombre_jefeproyecto")+"') AND DECODE('"+m.get("id_empresa_cliente")+"', 'null', '*', ID_EMPRESA_CLIENTE) = DECODE('"+m.get("id_empresa_cliente")+"', 'null', '*', "+m.get("id_empresa_cliente")+") AND DECODE('"+m.get("nombre_empresa_cliente")+"', 'null', '*', NOMBRE_EMPRESA_CLIENTE) = DECODE('"+m.get("nombre_empresa_cliente")+"', 'null', '*', '"+m.get("nombre_empresa_cliente")+"') AND DECODE('"+m.get("id_area_cliente")+"', 'null', '*', ID_AREA_CLIENTE) = DECODE('"+m.get("id_area_cliente")+"', 'null', '*', "+m.get("id_area_cliente")+") AND DECODE('"+m.get("nombre_area_cliente")+"', 'null', '*', NOMBRE_AREA_CLIENTE) = DECODE('"+m.get("nombre_area_cliente")+"', 'null', '*', '"+m.get("nombre_area_cliente")+"') AND DECODE('"+m.get("id_usuario_cliente")+"', 'null', '*', ID_USUARIO_CLIENTE) = DECODE('"+m.get("id_usuario_cliente")+"', 'null', '*', "+m.get("id_usuario_cliente")+") AND DECODE('"+m.get("nombre_usuario_cliente")+"', 'null', '*', NOMBRE_USUARIO_CLIENTE) = DECODE('"+m.get("nombre_usuario_cliente")+"', 'null', '*', '"+m.get("nombre_usuario_cliente")+"') AND DECODE('"+m.get("id_prioridad")+"', 'null', '*', ID_PRIORIDAD) = DECODE('"+m.get("id_prioridad")+"', 'null', '*', "+m.get("id_prioridad")+")");

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
