package cl.gesvita.ws.obtenersucursalporgrupouser;

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
 * Servlet implementation class ObtenerSucursalPorGrupoUser
 */
@WebServlet("/WSObtenerSucursalPorGrupoUser/ObtenerSucursalPorGrupoUser")
public class ObtenerSucursalPorGrupoUser extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(ObtenerSucursalPorGrupoUser.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ObtenerSucursalPorGrupoUser() {
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
            String[] camposytipos = {"ID_SUCURSAL","I","NOMBRE_SUCURSAL","S","TIPO_SUCURSAL","S","FONO_CONTACTO","S",
                "NOMBRE_CONTACTO","S","DIRECCION_SUCURSAL","S","COMUNA_SUCURSAL","S","LATITUD_SUCURSAL","S",
                "LONGITUD_SUCURSAL","S","NOMBRE_IMAGEN_SUCURSAL","S","ID_EMPRESA","I","DATA_EXTENDIDA","S",
                "ID_GRUPO","I","CODI_GRUPO","S","APELLIDO_PATERNO","S","APELLIDO_MATERNO","S","NOMBRE","S",
                "ID_USUARIO","I","DESCRIPCION_GRUPO","S","CORREO","S","TELEFONO","S","CELULAR","S"};
            String json = ObtenerLib.getDefaultObtenerResoultSet(logger, connection, camposytipos, "VW_PROYECTO_SUCURSALXGRUPOUSER" , m);
//            stmt = connection.createStatement();
//            rs = stmt.executeQuery("SELECT * FROM VW_PROYECTO_SUCURSALXGRUPOUSER WHERE "+
// "DECODE('"+m.get("id_sucursal")+"', 'null', '*', ID_SUCURSAL) = DECODE('"+m.get("id_sucursal")+"', 'null', '*', "+m.get("id_sucursal")+") AND DECODE('"+m.get("nombre_sucursal")+"', 'null', '*', NOMBRE_SUCURSAL) = DECODE('"+m.get("nombre_sucursal")+"', 'null', '*', '"+m.get("nombre_sucursal")+"') AND DECODE('"+m.get("tipo_sucursal")+"', 'null', '*', TIPO_SUCURSAL) = DECODE('"+m.get("tipo_sucursal")+"', 'null', '*', '"+m.get("tipo_sucursal")+"') AND DECODE('"+m.get("fono_contacto")+"', 'null', '*', FONO_CONTACTO) = DECODE('"+m.get("fono_contacto")+"', 'null', '*', '"+m.get("fono_contacto")+"') AND DECODE('"+m.get("nombre_contacto")+"', 'null', '*', NOMBRE_CONTACTO) = DECODE('"+m.get("nombre_contacto")+"', 'null', '*', '"+m.get("nombre_contacto")+"') AND DECODE('"+m.get("direccion_sucursal")+"', 'null', '*', DIRECCION_SUCURSAL) = DECODE('"+m.get("direccion_sucursal")+"', 'null', '*', '"+m.get("direccion_sucursal")+"') AND DECODE('"+m.get("comuna_sucursal")+"', 'null', '*', COMUNA_SUCURSAL) = DECODE('"+m.get("comuna_sucursal")+"', 'null', '*', '"+m.get("comuna_sucursal")+"') AND DECODE('"+m.get("latitud_sucursal")+"', 'null', '*', LATITUD_SUCURSAL) = DECODE('"+m.get("latitud_sucursal")+"', 'null', '*', '"+m.get("latitud_sucursal")+"') AND DECODE('"+m.get("longitud_sucursal")+"', 'null', '*', LONGITUD_SUCURSAL) = DECODE('"+m.get("longitud_sucursal")+"', 'null', '*', '"+m.get("longitud_sucursal")+"') AND DECODE('"+m.get("nombre_imagen_sucursal")+"', 'null', '*', NOMBRE_IMAGEN_SUCURSAL) = DECODE('"+m.get("nombre_imagen_sucursal")+"', 'null', '*', '"+m.get("nombre_imagen_sucursal")+"') AND DECODE('"+m.get("id_empresa")+"', 'null', '*', ID_EMPRESA) = DECODE('"+m.get("id_empresa")+"', 'null', '*', "+m.get("id_empresa")+") AND DECODE('"+m.get("data_extendida")+"', 'null', '*', DATA_EXTENDIDA) = DECODE('"+m.get("data_extendida")+"', 'null', '*', '"+m.get("data_extendida")+"') AND DECODE('"+m.get("id_grupo")+"', 'null', '*', ID_GRUPO) = DECODE('"+m.get("id_grupo")+"', 'null', '*', "+m.get("id_grupo")+") AND DECODE('"+m.get("codi_grupo")+"', 'null', '*', CODI_GRUPO) = DECODE('"+m.get("codi_grupo")+"', 'null', '*', '"+m.get("codi_grupo")+"') AND DECODE('"+m.get("apellido_paterno")+"', 'null', '*', APELLIDO_PATERNO) = DECODE('"+m.get("apellido_paterno")+"', 'null', '*', '"+m.get("apellido_paterno")+"') AND DECODE('"+m.get("apellido_materno")+"', 'null', '*', APELLIDO_MATERNO) = DECODE('"+m.get("apellido_materno")+"', 'null', '*', '"+m.get("apellido_materno")+"') AND DECODE('"+m.get("nombre")+"', 'null', '*', NOMBRE) = DECODE('"+m.get("nombre")+"', 'null', '*', '"+m.get("nombre")+"') AND DECODE('"+m.get("id_usuario")+"', 'null', '*', ID_USUARIO) = DECODE('"+m.get("id_usuario")+"', 'null', '*', "+m.get("id_usuario")+") AND DECODE('"+m.get("descripcion_grupo")+"', 'null', '*', DESCRIPCION_GRUPO) = DECODE('"+m.get("descripcion_grupo")+"', 'null', '*', '"+m.get("descripcion_grupo")+"') AND DECODE('"+m.get("correo")+"', 'null', '*', CORREO) = DECODE('"+m.get("correo")+"', 'null', '*', '"+m.get("correo")+"') AND DECODE('"+m.get("telefono")+"', 'null', '*', TELEFONO) = DECODE('"+m.get("telefono")+"', 'null', '*', '"+m.get("telefono")+"') AND DECODE('"+m.get("celular")+"', 'null', '*', CELULAR) = DECODE('"+m.get("celular")+"', 'null', '*', '"+m.get("celular")+"')");

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
