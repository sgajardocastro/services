package cl.gesvita.ws.obtenersolicitudesv2;

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

import java.util.ArrayList;
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
 * Servlet implementation class ObtenerSolicitudesV2
 */
@WebServlet("/WSObtenerSolicitudesV2/ObtenerSolicitudesV2")
public class ObtenerSolicitudesV2 extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(ObtenerSolicitudesV2.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ObtenerSolicitudesV2() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // EN USO ????
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
		
/*		DECODE('"+m.get("nombre_tipo")+"', 'null', '*', NOMBRE_TIPO) = DECODE('"+m.get("nombre_tipo")+"', 'null', '*', '"+m.get("nombre_tipo")+"')*/

        try {
            connection = datasource.getConnection();
            String[] camposytipos = {"ID_SOLICITUD","I","ID_EMPRESA","I","NOMBRE_SOLICITUD","S","ID_TIPOSOLICITUD","I","NOMBRE_TIPO_SOLICITUD","S","ID_REQUERIMIENTO","I","NOMBRE_REQUERIMIENTO","S","DESCRIPCION_SOLICITUD","S","ID_ESTADOSOLICITUD","I","NOMBRE_ESTADOSOLICITUD","S","ID_USUARIO_CREACION","I","NOMBRE_USUARIO_CREACION","S","ID_USUARIO_JEFE_PROYECTO","I","NOMBRE_JEFEPROYECTO","S","ID_EMPRESA_CLIENTE","I","NOMBRE_EMPRESA_CLIENTE","S","ID_AREA_CLIENTE","I","NOMBRE_AREA_CLIENTE","S","ID_USUARIO_CLIENTE","I","NOMBRE_USUARIO_CLIENTE","S","ID_PRIORIDAD","I","NOMBRE_PRIORIDAD","S","ID_PROYECTO_PADRE","I","NOMBRE_PROYECTO_PADRE","S","ID_AREA","I","NOMBRE_AREA","S","ID_SUCURSAL","I","NOMBRE_SUCURSAL","S","FECHA_CREACION","S","FECHA_PLAN_INI","S","FECHA_PLAN_FIN","S","FECHA_PLAN_INI_DATE","D","FECHA_PLAN_FIN_DATE","D","FECHA_REAL_INI_DATE","D","FECHA_REAL_FIN_DATE","D","DATA_EXTENDIDA","S","ID_FORMULARIO_PROYECTO","I","URL_FORMULARIO_PROYECTO","S","CENTRO_COSTO_SUCURSAL","S","ID_TIPO","I","NOMBRE_TIPO","S","CODI_GRUPO_PROYECTO","S","NOMBRE_GRUPO_PROYECTO","S","AVANCE_REAL_PROYECTO","I","AVANCE_PLAN_PORTIEMPO","I","KPI_PROYECTO_AVANCE_STATUS","I"};
            String json = ObtenerLib.getDefaultObtenerResoultSet(logger, connection, camposytipos, "VW_PROYECTO_SOLICITUD" , m);
//            stmt = connection.createStatement();
//            rs = stmt.executeQuery("SELECT * FROM VW_PROYECTO_SOLICITUD WHERE "+
//"DECODE('"+m.get("id_solicitud")+"', 'null', '*', ID_SOLICITUD) = DECODE('"+m.get("id_solicitud")+"', 'null', '*', "+m.get("id_solicitud")+")"
//        + " AND DECODE('"+m.get("id_tiposolicitud")+"', 'null', '*', ID_TIPOSOLICITUD) = DECODE('"+m.get("id_tiposolicitud")+"', 'null', '*', "+m.get("id_tiposolicitud")+")"
//        + " AND DECODE('"+m.get("id_subtipo")+"', 'null', '*', ID_SUBTIPO) = DECODE('"+m.get("id_subtipo")+"', 'null', '*', "+m.get("id_subtipo")+")"
//        + " AND DECODE('"+m.get("nombre_subtipo")+"', 'null', '*', NOMBRE_SUBTIPO) = DECODE('"+m.get("nombre_subtipo")+"', 'null', '*', '"+m.get("nombre_subtipo")+"')"
//        + " AND DECODE('"+m.get("descripcion_solicitud")+"', 'null', '*', DESCRIPCION_SOLICITUD) = DECODE('"+m.get("descripcion_solicitud")+"', 'null', '*', '"+m.get("descripcion_solicitud")+"')"
//        + " AND DECODE('"+m.get("id_estadosolicitud")+"', 'null', '*', ID_ESTADOSOLICITUD) = DECODE('"+m.get("id_estadosolicitud")+"', 'null', '*', "+m.get("id_estadosolicitud")+")"
//        + " AND DECODE('"+m.get("nombre_estadosolicitud")+"', 'null', '*', NOMBRE_ESTADOSOLICITUD) = DECODE('"+m.get("nombre_estadosolicitud")+"', 'null', '*', '"+m.get("nombre_estadosolicitud")+"')"
//        + " AND DECODE('"+m.get("id_usuario_creacion")+"', 'null', '*', ID_USUARIO_CREACION) = DECODE('"+m.get("id_usuario_creacion")+"', 'null', '*', "+m.get("id_usuario_creacion")+")"
//        + " AND DECODE('"+m.get("nombre_usuario")+"', 'null', '*', NOMBRE_USUARIO) = DECODE('"+m.get("nombre_usuario")+"', 'null', '*', '"+m.get("nombre_usuario")+"')"
//        + " AND DECODE('"+m.get("registro_de_oportunidad")+"', 'null', '*', REGISTRO_DE_OPORTUNIDAD) = DECODE('"+m.get("registro_de_oportunidad")+"', 'null', '*', '"+m.get("registro_de_oportunidad")+"')"
//        + " AND DECODE('"+m.get("construccion_cotizacion")+"', 'null', '*', CONSTRUCCION_COTIZACION) = DECODE('"+m.get("construccion_cotizacion")+"', 'null', '*', '"+m.get("construccion_cotizacion")+"')"
//        + " AND DECODE('"+m.get("cierre_cotizacion")+"', 'null', '*', CIERRE_COTIZACION) = DECODE('"+m.get("cierre_cotizacion")+"', 'null', '*', '"+m.get("cierre_cotizacion")+"')"
//        + " AND DECODE('"+m.get("id_empresa_cliente")+"', 'null', '*', ID_EMPRESA_CLIENTE) = DECODE('"+m.get("id_empresa_cliente")+"', 'null', '*', "+m.get("id_empresa_cliente")+")"
//        + " AND DECODE('"+m.get("nombre_empresa")+"', 'null', '*', NOMBRE_EMPRESA) = DECODE('"+m.get("nombre_empresa")+"', 'null', '*', '"+m.get("nombre_empresa")+"')"
//        + " AND DECODE('"+m.get("nombre_proyecto_cliente")+"', 'null', '*', NOMBRE_PROYECTO_CLIENTE) = DECODE('"+m.get("nombre_proyecto_cliente")+"', 'null', '*', '"+m.get("nombre_proyecto_cliente")+"')"
//        + " AND DECODE('"+m.get("id_usuario_contacto")+"', 'null', '*', ID_USUARIO_CONTACTO) = DECODE('"+m.get("id_usuario_contacto")+"', 'null', '*', "+m.get("id_usuario_contacto")+")"
//        + " AND DECODE('"+m.get("nombrecontacto")+"', 'null', '*', NOMBRECONTACTO) = DECODE('"+m.get("nombrecontacto")+"', 'null', '*', '"+m.get("nombrecontacto")+"')"
//        + " AND DECODE('"+m.get("flag_focosolicitud")+"', 'null', '*', FLAG_FOCOSOLICITUD) = DECODE('"+m.get("flag_focosolicitud")+"', 'null', '*', '"+m.get("flag_focosolicitud")+"')"
//        + " AND DECODE('"+m.get("id_proyecto")+"', 'null', '*', ID_PROYECTO) = DECODE('"+m.get("id_proyecto")+"', 'null', '*', "+m.get("id_proyecto")+")"
//        + " AND DECODE('"+m.get("id_tarea")+"', 'null', '*', ID_TAREA) = DECODE('"+m.get("id_tarea")+"', 'null', '*', "+m.get("id_tarea")+")"
//        + " AND DECODE('"+m.get("nombre_tarea")+"', 'null', '*', NOMBRE_TAREA) = DECODE('"+m.get("nombre_tarea")+"', 'null', '*', '"+m.get("nombre_tarea")+"')"
//        + " AND DECODE('"+m.get("fecha_limite")+"', 'null', '*', FECHA_LIMITE) = DECODE('"+m.get("fecha_limite")+"', 'null', '*', to_date('"+m.get("fecha_limite")+"', 'dd/mm/yyyy'))"
//        + " AND DECODE('"+m.get("fecha_limite_fmt")+"', 'null', '*', FECHA_LIMITE_FMT) = DECODE('"+m.get("fecha_limite_fmt")+"', 'null', '*', '"+m.get("fecha_limite_fmt")+"')"
//        + " AND DECODE('"+m.get("fecha_creacion")+"', 'null', '*', FECHA_CREACION) = DECODE('"+m.get("fecha_creacion")+"', 'null', '*', to_date('"+m.get("fecha_creacion")+"', 'dd/mm/yyyy'))"
//        + " AND DECODE('"+m.get("fecha_creacion_fmt")+"', 'null', '*', FECHA_CREACION_FMT) = DECODE('"+m.get("fecha_creacion_fmt")+"', 'null', '*', '"+m.get("fecha_creacion_fmt")+"')"
//        + " AND DECODE('"+m.get("fecha_cierre_fmt")+"', 'null', '*', FECHA_CIERRE_FMT) = DECODE('"+m.get("fecha_cierre_fmt")+"', 'null', '*', '"+m.get("fecha_cierre_fmt")+"')");

//                dataIndividual = new HashMap<String, Object>();
//               	dataIndividual.put("ID_SOLICITUD", rs.getString("ID_SOLICITUD"));
//               	dataIndividual.put("ID_TIPOSOLICITUD", rs.getString("ID_TIPOSOLICITUD"));
//               //dataIndividual.put("NOMBRE_TIPO", rs.getString("NOMBRE_TIPO"));
//               	dataIndividual.put("ID_SUBTIPO", rs.getString("ID_SUBTIPO"));
//               	dataIndividual.put("NOMBRE_SUBTIPO", rs.getString("NOMBRE_SUBTIPO"));
//               	dataIndividual.put("DESCRIPCION_SOLICITUD", rs.getString("DESCRIPCION_SOLICITUD"));
//               	dataIndividual.put("ID_ESTADOSOLICITUD", rs.getString("ID_ESTADOSOLICITUD"));
//               	dataIndividual.put("NOMBRE_ESTADOSOLICITUD", rs.getString("NOMBRE_ESTADOSOLICITUD"));
//               	dataIndividual.put("ID_USUARIO_CREACION", rs.getString("ID_USUARIO_CREACION"));
//               	dataIndividual.put("NOMBRE_USUARIO", rs.getString("NOMBRE_USUARIO"));
//               	dataIndividual.put("REGISTRO_DE_OPORTUNIDAD", rs.getString("REGISTRO_DE_OPORTUNIDAD"));
//               	dataIndividual.put("CONSTRUCCION_COTIZACION", rs.getString("CONSTRUCCION_COTIZACION"));
//               	dataIndividual.put("CIERRE_COTIZACION", rs.getString("CIERRE_COTIZACION"));
//               	dataIndividual.put("ID_EMPRESA_CLIENTE", rs.getString("ID_EMPRESA_CLIENTE"));
//               	dataIndividual.put("NOMBRE_EMPRESA", rs.getString("NOMBRE_EMPRESA"));
//               	dataIndividual.put("NOMBRE_PROYECTO_CLIENTE", rs.getString("NOMBRE_PROYECTO_CLIENTE"));
//               	dataIndividual.put("ID_USUARIO_CONTACTO", rs.getString("ID_USUARIO_CONTACTO"));
//               	dataIndividual.put("NOMBRECONTACTO", rs.getString("NOMBRECONTACTO"));
//               	dataIndividual.put("FLAG_FOCOSOLICITUD", rs.getString("FLAG_FOCOSOLICITUD"));
//               	dataIndividual.put("ID_PROYECTO", rs.getString("ID_PROYECTO"));
//               	dataIndividual.put("ID_TAREA", rs.getString("ID_TAREA"));
//               	dataIndividual.put("NOMBRE_TAREA", rs.getString("NOMBRE_TAREA"));
//               	dataIndividual.put("FECHA_LIMITE", rs.getString("FECHA_LIMITE"));
//               	dataIndividual.put("FECHA_LIMITE_FMT", rs.getString("FECHA_LIMITE_FMT"));
//               	dataIndividual.put("FECHA_CREACION", rs.getString("FECHA_CREACION"));
//               	dataIndividual.put("FECHA_CREACION_FMT", rs.getString("FECHA_CREACION_FMT"));
//               	dataIndividual.put("FECHA_CIERRE_FMT", rs.getString("FECHA_CIERRE_FMT"));
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
