package cl.gesvita.ws.obtenerinformacionproyectoficha;

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
 * Servlet implementation class ObtenerInformacionProyectoFicha
 */
@WebServlet("/WSObtenerInformacionProyectoFicha/ObtenerInformacionProyectoFicha")
public class ObtenerInformacionProyectoFicha extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(ObtenerInformacionProyectoFicha.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ObtenerInformacionProyectoFicha() {
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
            String[] camposytipos = { "ID_PROYECTO","I","ID_PROYECTO_PADRE","I","NOMBRE","S","OBJETIVO","S","FECHA_PLAN_INI","S","FECHA_PLAN_FIN","S","FECHA_ORDER","D","FECHA_REAL_INI","S","FECHA_REAL_FIN","S","NOMBRE_USUARIO","S","NOMBRE_JEFEPROYECTO","S","NOMBRE_SPONSOR","S","NOMBRE_AREA","S","ID_TIPO","I","DESCRIPCION_TIPO","S","ID_MAGNITUD","I","NOMBRE_MAGNITUD","S","ID_PRIORIDAD","I","NOMBRE_PRIORIDAD","S","ID_ESTADO","I","NOMBRE_ESTADO","S","NOMBRE_EMPRESA","S","NOMBRE_PRESUPUESTO","S","OBSERVACION","S","DIAS_CUTOVER","S","DIAS_CUTOVER_NUMERO","I","ID_USUARIO_JEFE_PROYECTO","I","NOMBRE_PROGRAMA","S","ID_EMPRESA","I","ID_PAIS","I","FECHA_REAL_INI_DATE","D","FECHA_REAL_FIN_DATE","D","FECHA_PLAN_INI_DATE","D","FECHA_PLAN_FIN_DATE","D","FECHA_MODIFICACION","D","NOMBRE_TIPO","S" };
            
            // Construir el SQL
            StringSql.append("SELECT * FROM VW_PROYECTO_SETUP_FICHA");
            String sqlWhereAnd = " WHERE ";
            for (int i = 0; i < camposytipos.length;i+=2) {
                if (m.containsKey(camposytipos[i].toLowerCase())){
                    if (camposytipos[i+1].charAt(0) == 'D') {
                        StringSql.append( sqlWhereAnd + camposytipos[i] + " = to_date( ? , 'dd/mm/yyyy')");
                    } else {
                        StringSql.append( sqlWhereAnd + camposytipos[i] + " = ?");
                    }
                    sqlWhereAnd = " AND ";
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
//            rs = stmt.executeQuery("SELECT * FROM VW_PROYECTO_SETUP_FICHA WHERE "+
//"DECODE('"+m.get("id_proyecto")+"', 'null', '*', ID_PROYECTO) = DECODE('"+m.get("id_proyecto")+"', 'null', '*', "+m.get("id_proyecto")+") AND DECODE('"+m.get("id_proyecto_padre")+"', 'null', '*', ID_PROYECTO_PADRE) = DECODE('"+m.get("id_proyecto_padre")+"', 'null', '*', "+m.get("id_proyecto_padre")+") AND DECODE('"+m.get("nombre")+"', 'null', '*', NOMBRE) = DECODE('"+m.get("nombre")+"', 'null', '*', '"+m.get("nombre")+"') AND DECODE('"+m.get("objetivo")+"', 'null', '*', OBJETIVO) = DECODE('"+m.get("objetivo")+"', 'null', '*', '"+m.get("objetivo")+"') AND DECODE('"+m.get("fecha_plan_ini")+"', 'null', '*', FECHA_PLAN_INI) = DECODE('"+m.get("fecha_plan_ini")+"', 'null', '*', '"+m.get("fecha_plan_ini")+"') AND DECODE('"+m.get("fecha_plan_fin")+"', 'null', '*', FECHA_PLAN_FIN) = DECODE('"+m.get("fecha_plan_fin")+"', 'null', '*', '"+m.get("fecha_plan_fin")+"') AND DECODE('"+m.get("fecha_order")+"', 'null', '*', FECHA_ORDER) = DECODE('"+m.get("fecha_order")+"', 'null', '*', to_date('"+m.get("fecha_order")+"', 'dd/mm/yyyy')) AND DECODE('"+m.get("fecha_real_ini")+"', 'null', '*', FECHA_REAL_INI) = DECODE('"+m.get("fecha_real_ini")+"', 'null', '*', '"+m.get("fecha_real_ini")+"') AND DECODE('"+m.get("fecha_real_fin")+"', 'null', '*', FECHA_REAL_FIN) = DECODE('"+m.get("fecha_real_fin")+"', 'null', '*', '"+m.get("fecha_real_fin")+"') AND DECODE('"+m.get("nombre_usuario")+"', 'null', '*', NOMBRE_USUARIO) = DECODE('"+m.get("nombre_usuario")+"', 'null', '*', '"+m.get("nombre_usuario")+"') AND DECODE('"+m.get("nombre_jefeproyecto")+"', 'null', '*', NOMBRE_JEFEPROYECTO) = DECODE('"+m.get("nombre_jefeproyecto")+"', 'null', '*', '"+m.get("nombre_jefeproyecto")+"') AND DECODE('"+m.get("nombre_sponsor")+"', 'null', '*', NOMBRE_SPONSOR) = DECODE('"+m.get("nombre_sponsor")+"', 'null', '*', '"+m.get("nombre_sponsor")+"') AND DECODE('"+m.get("nombre_area")+"', 'null', '*', NOMBRE_AREA) = DECODE('"+m.get("nombre_area")+"', 'null', '*', '"+m.get("nombre_area")+"') AND DECODE('"+m.get("id_tipo")+"', 'null', '*', ID_TIPO) = DECODE('"+m.get("id_tipo")+"', 'null', '*', "+m.get("id_tipo")+") AND DECODE('"+m.get("descripcion_tipo")+"', 'null', '*', DESCRIPCION_TIPO) = DECODE('"+m.get("descripcion_tipo")+"', 'null', '*', '"+m.get("descripcion_tipo")+"') AND DECODE('"+m.get("id_magnitud")+"', 'null', '*', ID_MAGNITUD) = DECODE('"+m.get("id_magnitud")+"', 'null', '*', "+m.get("id_magnitud")+") AND DECODE('"+m.get("nombre_magnitud")+"', 'null', '*', NOMBRE_MAGNITUD) = DECODE('"+m.get("nombre_magnitud")+"', 'null', '*', '"+m.get("nombre_magnitud")+"') AND DECODE('"+m.get("id_prioridad")+"', 'null', '*', ID_PRIORIDAD) = DECODE('"+m.get("id_prioridad")+"', 'null', '*', "+m.get("id_prioridad")+") AND DECODE('"+m.get("nombre_prioridad")+"', 'null', '*', NOMBRE_PRIORIDAD) = DECODE('"+m.get("nombre_prioridad")+"', 'null', '*', '"+m.get("nombre_prioridad")+"') AND DECODE('"+m.get("id_estado")+"', 'null', '*', ID_ESTADO) = DECODE('"+m.get("id_estado")+"', 'null', '*', "+m.get("id_estado")+") AND DECODE('"+m.get("nombre_estado")+"', 'null', '*', NOMBRE_ESTADO) = DECODE('"+m.get("nombre_estado")+"', 'null', '*', '"+m.get("nombre_estado")+"') AND DECODE('"+m.get("nombre_empresa")+"', 'null', '*', NOMBRE_EMPRESA) = DECODE('"+m.get("nombre_empresa")+"', 'null', '*', '"+m.get("nombre_empresa")+"') AND DECODE('"+m.get("nombre_presupuesto")+"', 'null', '*', NOMBRE_PRESUPUESTO) = DECODE('"+m.get("nombre_presupuesto")+"', 'null', '*', '"+m.get("nombre_presupuesto")+"') AND DECODE('"+m.get("observacion")+"', 'null', '*', OBSERVACION) = DECODE('"+m.get("observacion")+"', 'null', '*', '"+m.get("observacion")+"') AND DECODE('"+m.get("dias_cutover")+"', 'null', '*', DIAS_CUTOVER) = DECODE('"+m.get("dias_cutover")+"', 'null', '*', '"+m.get("dias_cutover")+"') AND DECODE('"+m.get("dias_cutover_numero")+"', 'null', '*', DIAS_CUTOVER_NUMERO) = DECODE('"+m.get("dias_cutover_numero")+"', 'null', '*', "+m.get("dias_cutover_numero")+") AND DECODE('"+m.get("id_usuario_jefe_proyecto")+"', 'null', '*', ID_USUARIO_JEFE_PROYECTO) = DECODE('"+m.get("id_usuario_jefe_proyecto")+"', 'null', '*', "+m.get("id_usuario_jefe_proyecto")+") AND DECODE('"+m.get("nombre_programa")+"', 'null', '*', NOMBRE_PROGRAMA) = DECODE('"+m.get("nombre_programa")+"', 'null', '*', '"+m.get("nombre_programa")+"') AND DECODE('"+m.get("id_empresa")+"', 'null', '*', ID_EMPRESA) = DECODE('"+m.get("id_empresa")+"', 'null', '*', "+m.get("id_empresa")+") AND DECODE('"+m.get("id_pais")+"', 'null', '*', ID_PAIS) = DECODE('"+m.get("id_pais")+"', 'null', '*', "+m.get("id_pais")+") AND DECODE('"+m.get("fecha_real_ini_date")+"', 'null', '*', FECHA_REAL_INI_DATE) = DECODE('"+m.get("fecha_real_ini_date")+"', 'null', '*', to_date('"+m.get("fecha_real_ini_date")+"', 'dd/mm/yyyy')) AND DECODE('"+m.get("fecha_real_fin_date")+"', 'null', '*', FECHA_REAL_FIN_DATE) = DECODE('"+m.get("fecha_real_fin_date")+"', 'null', '*', to_date('"+m.get("fecha_real_fin_date")+"', 'dd/mm/yyyy')) AND DECODE('"+m.get("fecha_plan_ini_date")+"', 'null', '*', FECHA_PLAN_INI_DATE) = DECODE('"+m.get("fecha_plan_ini_date")+"', 'null', '*', to_date('"+m.get("fecha_plan_ini_date")+"', 'dd/mm/yyyy')) AND DECODE('"+m.get("fecha_plan_fin_date")+"', 'null', '*', FECHA_PLAN_FIN_DATE) = DECODE('"+m.get("fecha_plan_fin_date")+"', 'null', '*', to_date('"+m.get("fecha_plan_fin_date")+"', 'dd/mm/yyyy')) AND DECODE('"+m.get("fecha_modificacion")+"', 'null', '*', FECHA_MODIFICACION) = DECODE('"+m.get("fecha_modificacion")+"', 'null', '*', to_date('"+m.get("fecha_modificacion")+"', 'dd/mm/yyyy')) AND DECODE('"+m.get("nombre_tipo")+"', 'null', '*', NOMBRE_TIPO) = DECODE('"+m.get("nombre_tipo")+"', 'null', '*', '"+m.get("nombre_tipo")+"')");

            dataGrupal = new HashMap<String, Object>();
            while (rs.next())
            {
                totalRegistro++;
                dataIndividual = new HashMap<String, Object>();
               dataIndividual.put("ID_PROYECTO", rs.getString("ID_PROYECTO"));
               dataIndividual.put("ID_PROYECTO_PADRE", rs.getString("ID_PROYECTO_PADRE"));
               dataIndividual.put("NOMBRE", rs.getString("NOMBRE"));
               dataIndividual.put("OBJETIVO", rs.getString("OBJETIVO"));
               dataIndividual.put("FECHA_PLAN_INI", rs.getString("FECHA_PLAN_INI"));
               dataIndividual.put("FECHA_PLAN_FIN", rs.getString("FECHA_PLAN_FIN"));
               dataIndividual.put("FECHA_ORDER", rs.getString("FECHA_ORDER"));
               dataIndividual.put("FECHA_REAL_INI", rs.getString("FECHA_REAL_INI"));
               dataIndividual.put("FECHA_REAL_FIN", rs.getString("FECHA_REAL_FIN"));
               dataIndividual.put("NOMBRE_USUARIO", rs.getString("NOMBRE_USUARIO"));
               dataIndividual.put("NOMBRE_JEFEPROYECTO", rs.getString("NOMBRE_JEFEPROYECTO"));
               dataIndividual.put("NOMBRE_SPONSOR", rs.getString("NOMBRE_SPONSOR"));
               dataIndividual.put("NOMBRE_AREA", rs.getString("NOMBRE_AREA"));
               dataIndividual.put("ID_TIPO", rs.getString("ID_TIPO"));
               dataIndividual.put("DESCRIPCION_TIPO", rs.getString("DESCRIPCION_TIPO"));
               dataIndividual.put("ID_MAGNITUD", rs.getString("ID_MAGNITUD"));
               dataIndividual.put("NOMBRE_MAGNITUD", rs.getString("NOMBRE_MAGNITUD"));
               dataIndividual.put("ID_PRIORIDAD", rs.getString("ID_PRIORIDAD"));
               dataIndividual.put("NOMBRE_PRIORIDAD", rs.getString("NOMBRE_PRIORIDAD"));
               dataIndividual.put("ID_ESTADO", rs.getString("ID_ESTADO"));
               dataIndividual.put("NOMBRE_ESTADO", rs.getString("NOMBRE_ESTADO"));
               dataIndividual.put("NOMBRE_EMPRESA", rs.getString("NOMBRE_EMPRESA"));
               dataIndividual.put("NOMBRE_PRESUPUESTO", rs.getString("NOMBRE_PRESUPUESTO"));
               dataIndividual.put("OBSERVACION", rs.getString("OBSERVACION"));
               dataIndividual.put("DIAS_CUTOVER", rs.getString("DIAS_CUTOVER"));
               dataIndividual.put("DIAS_CUTOVER_NUMERO", rs.getString("DIAS_CUTOVER_NUMERO"));
               dataIndividual.put("ID_USUARIO_JEFE_PROYECTO", rs.getString("ID_USUARIO_JEFE_PROYECTO"));
               dataIndividual.put("NOMBRE_PROGRAMA", rs.getString("NOMBRE_PROGRAMA"));
               dataIndividual.put("ID_EMPRESA", rs.getString("ID_EMPRESA"));
               dataIndividual.put("ID_PAIS", rs.getString("ID_PAIS"));
               dataIndividual.put("FECHA_REAL_INI_DATE", rs.getString("FECHA_REAL_INI_DATE"));
               dataIndividual.put("FECHA_REAL_FIN_DATE", rs.getString("FECHA_REAL_FIN_DATE"));
               dataIndividual.put("FECHA_PLAN_INI_DATE", rs.getString("FECHA_PLAN_INI_DATE"));
               dataIndividual.put("FECHA_PLAN_FIN_DATE", rs.getString("FECHA_PLAN_FIN_DATE"));
               dataIndividual.put("FECHA_MODIFICACION", rs.getString("FECHA_MODIFICACION"));
               dataIndividual.put("NOMBRE_TIPO", rs.getString("NOMBRE_TIPO"));
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
        }
    }

}
