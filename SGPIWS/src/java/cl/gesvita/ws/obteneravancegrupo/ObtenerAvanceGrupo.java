package cl.gesvita.ws.obteneravancegrupo;

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
 * Servlet implementation class ObtenerAvanceGrupo
 */
@WebServlet("/WSObtenerAvanceGrupo/ObtenerAvanceGrupo")
public class ObtenerAvanceGrupo extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(ObtenerAvanceGrupo.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ObtenerAvanceGrupo() {
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
        PreparedStatement stmt = null;
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
            StringBuilder StringSql = new StringBuilder();
            // Campos de la consulta con sus tipos
            SimpleDateFormat formato_ddmmyyyy = new SimpleDateFormat("dd/MM/yyyy");
            String[] camposytipos = {"ID_PROYECTO","I","NOMBRE","S","ID_PROYECTO_PADRE","I",
                "ID_ESTADO_PROYECTO","I","NOMBRE_ESTADO_PROYECTO","S","FECHA_REAL_INI","S",
                "FECHA_REAL_FIN","S","DATA_GRUPOS","S","CANTIDADPADRE","I"};
            
            // Construir el SQL
            StringSql.append("SELECT * FROM VW_PROYECTO_AVANCEGRUPO WHERE ");
            String sqlAnd = "";
            for (int i = 0; i < camposytipos.length;i+=2) {
                if (m.containsKey(camposytipos[i].toLowerCase())){
                    if (camposytipos[i+1].charAt(0) == 'D') {
                        StringSql.append( sqlAnd + camposytipos[i] + " = to_date( ? , 'dd/mm/yyyy')");
                    } else {
                        StringSql.append( sqlAnd + camposytipos[i] + " = ?");
                    }
                    sqlAnd = " AND ";
                }
            }
            // BYSECURITY logger.debug("Query NUEVA: " + StringSql.toString());

            stmt = connection.prepareStatement( StringSql.toString() );
            for (int i = 0, j = 1; i < camposytipos.length; i+=2) {
                if (m.containsKey(camposytipos[i].toLowerCase())){
                    switch (camposytipos[i+1].charAt(0)) {
                        case 'I':
                            int intVal = Integer.parseInt(m.get(camposytipos[i].toLowerCase()));
                            // BYSECURITY logger.debug("val["+ j +"]= " + intVal);
                            stmt.setInt(j, intVal);
                            break;
                        case 'F':
                            float floatVal = Float.parseFloat(m.get(camposytipos[i].toLowerCase()));
                            // BYSECURITY logger.debug("val["+ j +"]= " + floatVal);
                            stmt.setFloat(j, floatVal);
                            break;
                        case 'S':
                        case 'D':
                            String val = m.get(camposytipos[i].toLowerCase());
                            // BYSECURITY logger.debug("val["+ j +"]= " + val);
                            stmt.setString(j, val );
                            break;
                    }
                    j++;
                }
            }
            
            rs = stmt.executeQuery();
//            stmt = connection.createStatement();
//            rs = stmt.executeQuery("SELECT * FROM VW_PROYECTO_AVANCEGRUPO WHERE "+
//"DECODE('"+m.get("id_proyecto")+"', 'null', '*', ID_PROYECTO) = DECODE('"+m.get("id_proyecto")+"', 'null', '*', "+m.get("id_proyecto")+") AND DECODE('"+m.get("nombre")+"', 'null', '*', NOMBRE) = DECODE('"+m.get("nombre")+"', 'null', '*', '"+m.get("nombre")+"') AND DECODE('"+m.get("id_proyecto_padre")+"', 'null', '*', ID_PROYECTO_PADRE) = DECODE('"+m.get("id_proyecto_padre")+"', 'null', '*', "+m.get("id_proyecto_padre")+") AND DECODE('"+m.get("id_estado_proyecto")+"', 'null', '*', ID_ESTADO_PROYECTO) = DECODE('"+m.get("id_estado_proyecto")+"', 'null', '*', "+m.get("id_estado_proyecto")+") AND DECODE('"+m.get("nombre_estado_proyecto")+"', 'null', '*', NOMBRE_ESTADO_PROYECTO) = DECODE('"+m.get("nombre_estado_proyecto")+"', 'null', '*', '"+m.get("nombre_estado_proyecto")+"') AND DECODE('"+m.get("fecha_real_ini")+"', 'null', '*', FECHA_REAL_INI) = DECODE('"+m.get("fecha_real_ini")+"', 'null', '*', '"+m.get("fecha_real_ini")+"') AND DECODE('"+m.get("fecha_real_fin")+"', 'null', '*', FECHA_REAL_FIN) = DECODE('"+m.get("fecha_real_fin")+"', 'null', '*', '"+m.get("fecha_real_fin")+"') AND DECODE('"+m.get("data_grupos")+"', 'null', '*', DATA_GRUPOS) = DECODE('"+m.get("data_grupos")+"', 'null', '*', '"+m.get("data_grupos")+"') AND DECODE('"+m.get("cantidadpadre")+"', 'null', '*', CANTIDADPADRE) = DECODE('"+m.get("cantidadpadre")+"', 'null', '*', "+m.get("cantidadpadre")+")");

            dataGrupal = new HashMap<String, Object>();
            while (rs.next())
            {
                totalRegistro++;
                dataIndividual = new HashMap<String, Object>();
               dataIndividual.put("ID_PROYECTO", rs.getString("ID_PROYECTO"));
               dataIndividual.put("NOMBRE", rs.getString("NOMBRE"));
               dataIndividual.put("ID_PROYECTO_PADRE", rs.getString("ID_PROYECTO_PADRE"));
               dataIndividual.put("ID_ESTADO_PROYECTO", rs.getString("ID_ESTADO_PROYECTO"));
               dataIndividual.put("NOMBRE_ESTADO_PROYECTO", rs.getString("NOMBRE_ESTADO_PROYECTO"));
               dataIndividual.put("FECHA_REAL_INI", rs.getString("FECHA_REAL_INI"));
               dataIndividual.put("FECHA_REAL_FIN", rs.getString("FECHA_REAL_FIN"));
               dataIndividual.put("DATA_GRUPOS", rs.getString("DATA_GRUPOS"));
               dataIndividual.put("CANTIDADPADRE", rs.getString("CANTIDADPADRE"));
             dataGrupal.put(String.valueOf(totalRegistro), dataIndividual);
            }
            String json = gson.toJson(dataGrupal);
            response.setHeader("access-control-allow-origin", "*");
            out.print(json);

        } catch (SQLException e) {
            //System.out.println("Connection Failed! Check output console");
            logger.error("Error durante la consulta a la base de datos. "+e.getMessage());
            //e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                    rs = null;
                } catch (SQLException e) {
                    logger.error(e.getMessage(), e.fillInStackTrace());
                }
            }

            if (stmt != null) {
                try {
                    stmt.close();
                    stmt = null;
                } catch (SQLException e) {
                    logger.error(e.getMessage(), e.fillInStackTrace());
                }
            }

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
//            if (datasource != null)
//            {
//                try {
//                        datasource.close();
//                    }
//                    catch (Exception e)
//                    {
//                        e.printStackTrace();
//                    }
//            }
//            if (rs != null)
//            {
//                try {
//                    rs.close();
//                }
//                catch (SQLException e)
//                {
//                    e.printStackTrace();
//                }
//            }
//
//            if (stmt != null){
//                try {
//                    stmt.close();
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                }
//            }
        }
    }

}
