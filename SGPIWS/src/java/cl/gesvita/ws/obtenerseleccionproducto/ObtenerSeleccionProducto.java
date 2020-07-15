package cl.gesvita.ws.obtenerseleccionproducto;

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
 * Servlet implementation class ObtenerSeleccionProducto
 */
@WebServlet("/WSObtenerSeleccionProducto/ObtenerSeleccionProducto")
public class ObtenerSeleccionProducto extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(ObtenerSeleccionProducto.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ObtenerSeleccionProducto() {
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
            String[] camposytipos = {"ID_CATALOGO","I","ID_PRODUCTO","I","NOMBRE_PRODUCTO","S","CODIGO_PRODUCTO","S",
                "OBSERVACION_PRODUCTO","S","CANTIDAD_DISPONIBLE","I","ID_PRODUCTO_PACK","I","CATEGORIA_PRODUCTO","S",
                "IMAGEN_PRODUCTO","S","FECHA_CREACION","D","FECHA_VIGENCIAHASTA","D","FECHA_MODIFICACION","D",
                "ID_EMPRESA","I","NOMBRE_CATALOGO","S","NOMBRE_TIPO_CATALOGO","S"};
            String json = ObtenerLib.getDefaultObtenerResoultSet(logger, connection, camposytipos, "VW_SOLICITUD_PRODUCTO_ADD" , m);
//            stmt = connection.createStatement();
//            rs = stmt.executeQuery("SELECT * FROM VW_SOLICITUD_PRODUCTO_ADD WHERE "+
//"DECODE('"+m.get("id_catalogo")+"', 'null', '*', ID_CATALOGO) = DECODE('"+m.get("id_catalogo")+"', 'null', '*', "+m.get("id_catalogo")+") AND DECODE('"+m.get("id_producto")+"', 'null', '*', ID_PRODUCTO) = DECODE('"+m.get("id_producto")+"', 'null', '*', "+m.get("id_producto")+") AND DECODE('"+m.get("nombre_producto")+"', 'null', '*', NOMBRE_PRODUCTO) = DECODE('"+m.get("nombre_producto")+"', 'null', '*', '"+m.get("nombre_producto")+"') AND DECODE('"+m.get("codigo_producto")+"', 'null', '*', CODIGO_PRODUCTO) = DECODE('"+m.get("codigo_producto")+"', 'null', '*', '"+m.get("codigo_producto")+"') AND DECODE('"+m.get("observacion_producto")+"', 'null', '*', OBSERVACION_PRODUCTO) = DECODE('"+m.get("observacion_producto")+"', 'null', '*', '"+m.get("observacion_producto")+"') AND DECODE('"+m.get("cantidad_disponible")+"', 'null', '*', CANTIDAD_DISPONIBLE) = DECODE('"+m.get("cantidad_disponible")+"', 'null', '*', "+m.get("cantidad_disponible")+") AND DECODE('"+m.get("id_producto_pack")+"', 'null', '*', ID_PRODUCTO_PACK) = DECODE('"+m.get("id_producto_pack")+"', 'null', '*', "+m.get("id_producto_pack")+") AND DECODE('"+m.get("categoria_producto")+"', 'null', '*', CATEGORIA_PRODUCTO) = DECODE('"+m.get("categoria_producto")+"', 'null', '*', '"+m.get("categoria_producto")+"') AND DECODE('"+m.get("imagen_producto")+"', 'null', '*', IMAGEN_PRODUCTO) = DECODE('"+m.get("imagen_producto")+"', 'null', '*', '"+m.get("imagen_producto")+"') AND DECODE('"+m.get("fecha_creacion")+"', 'null', '*', FECHA_CREACION) = DECODE('"+m.get("fecha_creacion")+"', 'null', '*', to_date('"+m.get("fecha_creacion")+"', 'dd/mm/yyyy')) AND DECODE('"+m.get("fecha_vigenciahasta")+"', 'null', '*', FECHA_VIGENCIAHASTA) = DECODE('"+m.get("fecha_vigenciahasta")+"', 'null', '*', to_date('"+m.get("fecha_vigenciahasta")+"', 'dd/mm/yyyy')) AND DECODE('"+m.get("fecha_modificacion")+"', 'null', '*', FECHA_MODIFICACION) = DECODE('"+m.get("fecha_modificacion")+"', 'null', '*', to_date('"+m.get("fecha_modificacion")+"', 'dd/mm/yyyy')) AND DECODE('"+m.get("id_empresa")+"', 'null', '*', ID_EMPRESA) = DECODE('"+m.get("id_empresa")+"', 'null', '*', "+m.get("id_empresa")+") AND DECODE('"+m.get("nombre_catalogo")+"', 'null', '*', NOMBRE_CATALOGO) = DECODE('"+m.get("nombre_catalogo")+"', 'null', '*', '"+m.get("nombre_catalogo")+"') AND DECODE('"+m.get("nombre_tipo_catalogo")+"', 'null', '*', NOMBRE_TIPO_CATALOGO) = DECODE('"+m.get("nombre_tipo_catalogo")+"', 'null', '*', '"+m.get("nombre_tipo_catalogo")+"') order by NOMBRE_TIPO_CATALOGO,NOMBRE_CATALOGO, NOMBRE_PRODUCTO,CATEGORIA_PRODUCTO, CODIGO_PRODUCTO");

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
