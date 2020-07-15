package cl.gesvita.ws.obtenerlistadousuarios;

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
 * Servlet implementation class ObtenerListadoUsuarios
 */
@WebServlet("/WSObtenerListadoUsuarios/ObtenerListadoUsuarios")
public class ObtenerListadoUsuarios extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(ObtenerListadoUsuarios.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ObtenerListadoUsuarios() {
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
            String[] camposytipos = { "ID_USUARIO","I","CODI_USUARIO","S","NOMBRE_USUARIO","S","RUT_USUARIO","S","TELEFONO_USUARIO","S","CELULAR_USUARIO","S","CORREO_USUARIO","S","ESTADO_USUARIO","S","USUARIO","S","NOMBRE","S","APELLIDO_MATERNO","S","APELLIDO_PATERNO","S","RUT","S","TELEFONO","S","CELULAR","S","CORREO","S","ID_ROL","I","NOMBRE_ROL","S","CODI_GRUPO_BASE_ROL","S","ID_PERFIL","I","CODI_GRUPO_BASE_PERFIL","S","NOMBRE_PERFIL","S","ESTADO","S","ID_EMPRESA","I","NOMBRE_EMPRESA","S","ID_AREA","I","NOMBRE_AREA","S","PASSWORD_USUARIO","S","PASSWORD","S","OBSERVACION_USUARIO","S" };
            
            // Construir el SQL
            StringSql.append("SELECT * FROM VW_SEGURIDAD_USUARIO");
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
//            rs = stmt.executeQuery("SELECT * FROM VW_SEGURIDAD_USUARIO WHERE "+
//"DECODE('"+m.get("id_usuario")+"', 'null', '*', ID_USUARIO) = DECODE('"+m.get("id_usuario")+"', 'null', '*', "+m.get("id_usuario")+") AND DECODE('"+m.get("codi_usuario")+"', 'null', '*', CODI_USUARIO) = DECODE('"+m.get("codi_usuario")+"', 'null', '*', '"+m.get("codi_usuario")+"') AND DECODE('"+m.get("nombre_usuario")+"', 'null', '*', NOMBRE_USUARIO) = DECODE('"+m.get("nombre_usuario")+"', 'null', '*', '"+m.get("nombre_usuario")+"') AND DECODE('"+m.get("rut_usuario")+"', 'null', '*', RUT_USUARIO) = DECODE('"+m.get("rut_usuario")+"', 'null', '*', '"+m.get("rut_usuario")+"') AND DECODE('"+m.get("telefono_usuario")+"', 'null', '*', TELEFONO_USUARIO) = DECODE('"+m.get("telefono_usuario")+"', 'null', '*', '"+m.get("telefono_usuario")+"') AND DECODE('"+m.get("celular_usuario")+"', 'null', '*', CELULAR_USUARIO) = DECODE('"+m.get("celular_usuario")+"', 'null', '*', '"+m.get("celular_usuario")+"') AND DECODE('"+m.get("correo_usuario")+"', 'null', '*', CORREO_USUARIO) = DECODE('"+m.get("correo_usuario")+"', 'null', '*', '"+m.get("correo_usuario")+"') AND DECODE('"+m.get("estado_usuario")+"', 'null', '*', ESTADO_USUARIO) = DECODE('"+m.get("estado_usuario")+"', 'null', '*', '"+m.get("estado_usuario")+"') AND DECODE('"+m.get("usuario")+"', 'null', '*', USUARIO) = DECODE('"+m.get("usuario")+"', 'null', '*', '"+m.get("usuario")+"') AND DECODE('"+m.get("nombre")+"', 'null', '*', NOMBRE) = DECODE('"+m.get("nombre")+"', 'null', '*', '"+m.get("nombre")+"') AND DECODE('"+m.get("apellido_materno")+"', 'null', '*', APELLIDO_MATERNO) = DECODE('"+m.get("apellido_materno")+"', 'null', '*', '"+m.get("apellido_materno")+"') AND DECODE('"+m.get("apellido_paterno")+"', 'null', '*', APELLIDO_PATERNO) = DECODE('"+m.get("apellido_paterno")+"', 'null', '*', '"+m.get("apellido_paterno")+"') AND DECODE('"+m.get("rut")+"', 'null', '*', RUT) = DECODE('"+m.get("rut")+"', 'null', '*', '"+m.get("rut")+"') AND DECODE('"+m.get("telefono")+"', 'null', '*', TELEFONO) = DECODE('"+m.get("telefono")+"', 'null', '*', '"+m.get("telefono")+"') AND DECODE('"+m.get("celular")+"', 'null', '*', CELULAR) = DECODE('"+m.get("celular")+"', 'null', '*', '"+m.get("celular")+"') AND DECODE('"+m.get("correo")+"', 'null', '*', CORREO) = DECODE('"+m.get("correo")+"', 'null', '*', '"+m.get("correo")+"') AND DECODE('"+m.get("id_rol")+"', 'null', '*', ID_ROL) = DECODE('"+m.get("id_rol")+"', 'null', '*', "+m.get("id_rol")+") AND DECODE('"+m.get("nombre_rol")+"', 'null', '*', NOMBRE_ROL) = DECODE('"+m.get("nombre_rol")+"', 'null', '*', '"+m.get("nombre_rol")+"') AND DECODE('"+m.get("codi_grupo_base_rol")+"', 'null', '*', CODI_GRUPO_BASE_ROL) = DECODE('"+m.get("codi_grupo_base_rol")+"', 'null', '*', '"+m.get("codi_grupo_base_rol")+"') AND DECODE('"+m.get("id_perfil")+"', 'null', '*', ID_PERFIL) = DECODE('"+m.get("id_perfil")+"', 'null', '*', "+m.get("id_perfil")+") AND DECODE('"+m.get("codi_grupo_base_perfil")+"', 'null', '*', CODI_GRUPO_BASE_PERFIL) = DECODE('"+m.get("codi_grupo_base_perfil")+"', 'null', '*', '"+m.get("codi_grupo_base_perfil")+"') AND DECODE('"+m.get("nombre_perfil")+"', 'null', '*', NOMBRE_PERFIL) = DECODE('"+m.get("nombre_perfil")+"', 'null', '*', '"+m.get("nombre_perfil")+"') AND DECODE('"+m.get("estado")+"', 'null', '*', ESTADO) = DECODE('"+m.get("estado")+"', 'null', '*', '"+m.get("estado")+"') AND DECODE('"+m.get("id_empresa")+"', 'null', '*', ID_EMPRESA) = DECODE('"+m.get("id_empresa")+"', 'null', '*', "+m.get("id_empresa")+") AND DECODE('"+m.get("nombre_empresa")+"', 'null', '*', NOMBRE_EMPRESA) = DECODE('"+m.get("nombre_empresa")+"', 'null', '*', '"+m.get("nombre_empresa")+"') AND DECODE('"+m.get("id_area")+"', 'null', '*', ID_AREA) = DECODE('"+m.get("id_area")+"', 'null', '*', "+m.get("id_area")+") AND DECODE('"+m.get("nombre_area")+"', 'null', '*', NOMBRE_AREA) = DECODE('"+m.get("nombre_area")+"', 'null', '*', '"+m.get("nombre_area")+"') AND DECODE('"+m.get("password_usuario")+"', 'null', '*', PASSWORD_USUARIO) = DECODE('"+m.get("password_usuario")+"', 'null', '*', '"+m.get("password_usuario")+"') AND DECODE('"+m.get("password")+"', 'null', '*', PASSWORD) = DECODE('"+m.get("password")+"', 'null', '*', '"+m.get("password")+"') AND DECODE('"+m.get("observacion_usuario")+"', 'null', '*', OBSERVACION_USUARIO) = DECODE('"+m.get("observacion_usuario")+"', 'null', '*', '"+m.get("observacion_usuario")+"')");

            dataGrupal = new HashMap<String, Object>();
            while (rs.next())
            {
                totalRegistro++;
                dataIndividual = new HashMap<String, Object>();
               dataIndividual.put("ID_USUARIO", rs.getString("ID_USUARIO"));
               dataIndividual.put("CODI_USUARIO", rs.getString("CODI_USUARIO"));
               dataIndividual.put("NOMBRE_USUARIO", rs.getString("NOMBRE_USUARIO"));
               dataIndividual.put("RUT_USUARIO", rs.getString("RUT_USUARIO"));
               dataIndividual.put("TELEFONO_USUARIO", rs.getString("TELEFONO_USUARIO"));
               dataIndividual.put("CELULAR_USUARIO", rs.getString("CELULAR_USUARIO"));
               dataIndividual.put("CORREO_USUARIO", rs.getString("CORREO_USUARIO"));
               dataIndividual.put("ESTADO_USUARIO", rs.getString("ESTADO_USUARIO"));
               dataIndividual.put("USUARIO", rs.getString("USUARIO"));
               dataIndividual.put("NOMBRE", rs.getString("NOMBRE"));
               dataIndividual.put("APELLIDO_MATERNO", rs.getString("APELLIDO_MATERNO"));
               dataIndividual.put("APELLIDO_PATERNO", rs.getString("APELLIDO_PATERNO"));
               dataIndividual.put("RUT", rs.getString("RUT"));
               dataIndividual.put("TELEFONO", rs.getString("TELEFONO"));
               dataIndividual.put("CELULAR", rs.getString("CELULAR"));
               dataIndividual.put("CORREO", rs.getString("CORREO"));
               dataIndividual.put("ID_ROL", rs.getString("ID_ROL"));
               dataIndividual.put("NOMBRE_ROL", rs.getString("NOMBRE_ROL"));
               dataIndividual.put("CODI_GRUPO_BASE_ROL", rs.getString("CODI_GRUPO_BASE_ROL"));
               dataIndividual.put("ID_PERFIL", rs.getString("ID_PERFIL"));
               dataIndividual.put("CODI_GRUPO_BASE_PERFIL", rs.getString("CODI_GRUPO_BASE_PERFIL"));
               dataIndividual.put("NOMBRE_PERFIL", rs.getString("NOMBRE_PERFIL"));
               dataIndividual.put("ESTADO", rs.getString("ESTADO"));
               dataIndividual.put("ID_EMPRESA", rs.getString("ID_EMPRESA"));
               dataIndividual.put("NOMBRE_EMPRESA", rs.getString("NOMBRE_EMPRESA"));
               dataIndividual.put("ID_AREA", rs.getString("ID_AREA"));
               dataIndividual.put("NOMBRE_AREA", rs.getString("NOMBRE_AREA"));
               dataIndividual.put("PASSWORD_USUARIO", rs.getString("PASSWORD_USUARIO"));
               dataIndividual.put("PASSWORD", rs.getString("PASSWORD"));
               dataIndividual.put("OBSERVACION_USUARIO", rs.getString("OBSERVACION_USUARIO"));
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
