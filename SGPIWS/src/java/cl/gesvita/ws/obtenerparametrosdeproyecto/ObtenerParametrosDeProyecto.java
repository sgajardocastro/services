package cl.gesvita.ws.obtenerparametrosdeproyecto;

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
 * Servlet implementation class ObtenerParametrosDeProyecto
 */
@WebServlet("/WSObtenerParametrosDeProyecto/ObtenerParametrosDeProyecto")
public class ObtenerParametrosDeProyecto extends HttpServlet {

    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(ObtenerParametrosDeProyecto.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ObtenerParametrosDeProyecto() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     * response)
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
                "org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"
                + "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");

        DataSource datasource = new DataSource();
        datasource.setPoolProperties(p);

        Map<String, Object> dataIndividual = null;
        Map<String, Object> dataGrupal = null;
        Map<String, Object> dataGrupal2 = null;
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Gson gson = new Gson();
        int totalRegistro = 0;

        Map<String, String> m = new HashMap<String, String>();
        try {
            StringBuilder sb = new StringBuilder();

            String s;
            while ((s = request.getReader().readLine()) != null) {
                sb.append(s);
            }
            //obtener json a array
            requestS = sb.toString();

            if (requestS.length() > 0) {
                requestS = requestS.substring(0, sb.length() - 1);
                requestS = requestS.substring(1);
            }
            String[] splitStr = null;

            int iTot = 0;
            requestS = requestS.replace("\"", "");
            requestS = requestS.replace("\'", "");
            if (requestS.length() > 0) {
                if (requestS.contains(",")) {
                    splitStr = requestS.split(",");
                    for (String val : splitStr) {
                        String[] srSpl = val.split(":");
                        m.put(srSpl[0].trim().toString(), srSpl[1].trim().toString());
                    }
                } else {
                    String[] srSpl = requestS.split(":");
                    m.put(srSpl[0].trim().toString(), srSpl[1].trim().toString());
                }
            }
          // BYSECURITY logger.info("JSON cargado");
        } catch (NullPointerException e1) {
            logger.error("Ha ocurrido un error de tipo NullPointer. " + e1.getMessage());
        } catch (IllegalArgumentException e2) {
            logger.error("Ha ocurrido un error de tipo IllegalArgument. " + e2.getMessage());
        } catch (IndexOutOfBoundsException e3) {
            logger.error("Ha ocurrido un error de tipo IndexOutOfBounds. " + e3.getMessage());
        } catch (Exception e4) {
            logger.error("Ha ocurrido un error. " + e4.getMessage());
        }

        try {
            connection = datasource.getConnection();
//            stmt = connection.createStatement();
//            rs = stmt.executeQuery("SELECT * FROM VW_PROYECTO_JEFESPROYECTOS WHERE "+
//"DECODE('"+m.get("id_usuario")+"', 'null', '*', ID_USUARIO) = DECODE('"+m.get("id_usuario")+"', 'null', '*', "+m.get("id_usuario")+") AND DECODE('"+m.get("nombre_jefe")+"', 'null', '*', NOMBRE_JEFE) = DECODE('"+m.get("nombre_jefe")+"', 'null', '*', '"+m.get("nombre_jefe")+"') AND DECODE('"+m.get("id_empresa")+"', 'null', '*', ID_EMPRESA) = DECODE('"+m.get("id_empresa")+"', 'null', '*', "+m.get("id_empresa")+") AND DECODE('"+m.get("nombre_empresa")+"', 'null', '*', NOMBRE_EMPRESA) = DECODE('"+m.get("nombre_empresa")+"', 'null', '*', '"+m.get("nombre_empresa")+"') AND DECODE('"+m.get("id_area")+"', 'null', '*', ID_AREA) = DECODE('"+m.get("id_area")+"', 'null', '*', "+m.get("id_area")+") AND DECODE('"+m.get("nombre_area")+"', 'null', '*', NOMBRE_AREA) = DECODE('"+m.get("nombre_area")+"', 'null', '*', '"+m.get("nombre_area")+"')");

            dataGrupal = new HashMap<String, Object>();
            dataGrupal2 = new HashMap<String, Object>();
            while (rs.next()) {
                totalRegistro++;
                dataIndividual = new HashMap<String, Object>();
                dataIndividual.put("ID_USUARIO", rs.getString("ID_USUARIO"));
                dataIndividual.put("NOMBRE_JEFE", rs.getString("NOMBRE_JEFE"));
                dataIndividual.put("ID_EMPRESA", rs.getString("ID_EMPRESA"));
                dataIndividual.put("NOMBRE_EMPRESA", rs.getString("NOMBRE_EMPRESA"));
                dataIndividual.put("ID_AREA", rs.getString("ID_AREA"));
                dataIndividual.put("NOMBRE_AREA", rs.getString("NOMBRE_AREA"));
                dataGrupal.put(String.valueOf(totalRegistro), dataIndividual);
                dataGrupal2.put("PROYECTO_JEFESPROYECTOS", dataGrupal);
            }
            StringBuilder StringSql = new StringBuilder();
            // Campos de la consulta con sus tipos
            SimpleDateFormat formato_ddmmyyyy = new SimpleDateFormat("dd/MM/yyyy");
            String[] camposytipos = {"ID_PROGRAMA", "I", "NOMBRE_PROGRAMA", "S", "DESCRIPCION", "S"};

            // Construir el SQL
            StringSql.append("SELECT * FROM VW_PROYECTO_PROGRAMA");
            String sqlWhereAnd = " WHERE ";
            for (int i = 0; i < camposytipos.length; i += 2) {
                if (m.containsKey(camposytipos[i].toLowerCase())) {
                    if (camposytipos[i + 1].charAt(0) == 'D') {
                        StringSql.append(sqlWhereAnd + camposytipos[i] + " = to_date( ? , 'dd/mm/yyyy')");
                    } else {
                        StringSql.append(sqlWhereAnd + camposytipos[i] + " = ?");
                    }
                    sqlWhereAnd = " AND ";
                }
            }
            // BYSECURITY logger.debug("Query NUEVA: " + StringSql.toString());

            stmt = connection.prepareStatement(StringSql.toString());
            for (int i = 0, j = 1; i < camposytipos.length; i += 2) {
                if (m.containsKey(camposytipos[i].toLowerCase())) {
                    switch (camposytipos[i + 1].charAt(0)) {
                        case 'I':
                            int intVal = Integer.parseInt(m.get(camposytipos[i].toLowerCase()));
                            // BYSECURITY logger.debug("val[" + j + "]= " + intVal);
                            stmt.setInt(j, intVal);
                            break;
                        case 'F':
                            float floatVal = Float.parseFloat(m.get(camposytipos[i].toLowerCase()));
                            // BYSECURITY logger.debug("val[" + j + "]= " + floatVal);
                            stmt.setFloat(j, floatVal);
                            break;
                        case 'S':
                        case 'D':
                            String val = m.get(camposytipos[i].toLowerCase());
                            // BYSECURITY logger.debug("val[" + j + "]= " + val);
                            stmt.setString(j, val);
                            break;
                    }
                    j++;
                }
            }

            rs = stmt.executeQuery();

//            rs = stmt.executeQuery("SELECT * FROM VW_PROYECTO_PROGRAMA WHERE "+
//"DECODE('"+m.get("id_programa")+"', 'null', '*', ID_PROGRAMA) = DECODE('"+m.get("id_programa")+"', 'null', '*', "+m.get("id_programa")+") AND DECODE('"+m.get("nombre_programa")+"', 'null', '*', NOMBRE_PROGRAMA) = DECODE('"+m.get("nombre_programa")+"', 'null', '*', '"+m.get("nombre_programa")+"') AND DECODE('"+m.get("descripcion")+"', 'null', '*', DESCRIPCION) = DECODE('"+m.get("descripcion")+"', 'null', '*', '"+m.get("descripcion")+"')");
            totalRegistro = 0;
            dataGrupal = new HashMap<String, Object>();
            while (rs.next()) {
                totalRegistro++;
                dataIndividual = new HashMap<String, Object>();
                dataIndividual.put("ID_PROGRAMA", rs.getString("ID_PROGRAMA"));
                dataIndividual.put("NOMBRE_PROGRAMA", rs.getString("NOMBRE_PROGRAMA"));
                dataIndividual.put("DESCRIPCION", rs.getString("DESCRIPCION"));
                dataGrupal.put(String.valueOf(totalRegistro), dataIndividual);
                dataGrupal2.put("PROYECTO_PROGRAMA", dataGrupal);
            }

            StringSql.setLength(0);
            // stmt.close();
            camposytipos = new String[]{"ID_EMPRESA", "I", "NOMBRE_EMPRESA", "S", "ID_PAIS", "I", "NOMBRE_PAIS", "S", "RUT_EMPRESA", "S", "RAZON_SOCIAL", "S", "GIRO_EMPRESA", "S", "DIRECCION_EMPRESA", "S", "REPRESENTANTE_LEGAL", "S", "FONO_CONTACTO", "S", "NOMBRE_CONTACTO", "S", "ROL_CONTACTO", "S", "EMAIL_EMPRESA", "S", "LATITUD_EMPRESA", "S", "LONGITUD_EMPRESA", "S", "ESTADO_EMPRESA", "S", "ID_EMPRESA_PADRE", "I", "NOMBRE_EMPRESA_PADRE", "S", "ZONA_HORARIA", "S", "ID_TIPO_CAMBIO", "I", "ID_REPRESENTANTE_LEGAL", "I", "NOMBRE_USUARIO_REPRESENTANTE", "S", "ID_USUARIO_CREACION", "I", "NOMBRE_USUARIO_CREACION", "S", "FECHA_CREACION", "D", "FECHA_CREACION_FMT", "S", "FECHA_MODIFICACION", "D", "FECHA_MODIFICACION_FMT", "S", "COMUNA_EMPRESA", "S", "DIRECCION_ADMINISTRATIVA", "S", "COMUNA_ADMINISTRATIVA", "S", "OBSERVACION_CONTACTO", "S"};
            // Construir el SQL
            StringSql.append("SELECT * FROM VW_PROYECTO_EMPRESA");
            sqlWhereAnd = " WHERE ";
            for (int i = 0; i < camposytipos.length; i += 2) {
                if (m.containsKey(camposytipos[i].toLowerCase())) {
                    if (camposytipos[i + 1].charAt(0) == 'D') {
                        StringSql.append(sqlWhereAnd + camposytipos[i] + " = to_date( ? , 'dd/mm/yyyy')");
                    } else if (camposytipos[i + 1].charAt(0) == 'H') {
                        StringSql.append(sqlWhereAnd + camposytipos[i] + " = to_date( ? , 'dd/mm/yyyy hh24miss')");
                    } else {
                        StringSql.append(sqlWhereAnd + camposytipos[i] + " = ?");
                    }
                    sqlWhereAnd = " AND ";
                }
            }
            // BYSECURITY logger.debug("Query NUEVA: " + StringSql.toString());

            stmt = connection.prepareStatement(StringSql.toString());
            for (int i = 0, j = 1; i < camposytipos.length; i += 2) {
                if (m.containsKey(camposytipos[i].toLowerCase())) {
                    switch (camposytipos[i + 1].charAt(0)) {
                        case 'I':
                            int intVal = Integer.parseInt(m.get(camposytipos[i].toLowerCase()));
                            // BYSECURITY logger.debug("val[" + j + "]= " + intVal);
                            stmt.setInt(j, intVal);
                            break;
                        case 'F':
                            float floatVal = Float.parseFloat(m.get(camposytipos[i].toLowerCase()));
                            // BYSECURITY logger.debug("val[" + j + "]= " + floatVal);
                            stmt.setFloat(j, floatVal);
                            break;
                        case 'S':
                        case 'D':
                            String val = m.get(camposytipos[i].toLowerCase());
                            // BYSECURITY logger.debug("val[" + j + "]= " + val);
                            stmt.setString(j, val);
                            break;
                    }
                    j++;
                }
            }

            rs = stmt.executeQuery();

//            rs = stmt.executeQuery("SELECT * FROM VW_PROYECTO_EMPRESA WHERE "+
//"DECODE('"+m.get("id_empresa")+"', 'null', '*', ID_EMPRESA) = DECODE('"+m.get("id_empresa")+"', 'null', '*', "+m.get("id_empresa")+") AND DECODE('"+m.get("nombre_empresa")+"', 'null', '*', NOMBRE_EMPRESA) = DECODE('"+m.get("nombre_empresa")+"', 'null', '*', '"+m.get("nombre_empresa")+"') AND DECODE('"+m.get("nombre_pais")+"', 'null', '*', NOMBRE_PAIS) = DECODE('"+m.get("nombre_pais")+"', 'null', '*', '"+m.get("nombre_pais")+"') AND DECODE('"+m.get("rut_empresa")+"', 'null', '*', RUT_EMPRESA) = DECODE('"+m.get("rut_empresa")+"', 'null', '*', '"+m.get("rut_empresa")+"') AND DECODE('"+m.get("razon_social")+"', 'null', '*', RAZON_SOCIAL) = DECODE('"+m.get("razon_social")+"', 'null', '*', '"+m.get("razon_social")+"') AND DECODE('"+m.get("giro_empresa")+"', 'null', '*', GIRO_EMPRESA) = DECODE('"+m.get("giro_empresa")+"', 'null', '*', '"+m.get("giro_empresa")+"') AND DECODE('"+m.get("direccion_empresa")+"', 'null', '*', DIRECCION_EMPRESA) = DECODE('"+m.get("direccion_empresa")+"', 'null', '*', '"+m.get("direccion_empresa")+"') AND DECODE('"+m.get("fono_contacto")+"', 'null', '*', FONO_CONTACTO) = DECODE('"+m.get("fono_contacto")+"', 'null', '*', '"+m.get("fono_contacto")+"') AND DECODE('"+m.get("email_empresa")+"', 'null', '*', EMAIL_EMPRESA) = DECODE('"+m.get("email_empresa")+"', 'null', '*', '"+m.get("email_empresa")+"') AND DECODE('"+m.get("latitud_empresa")+"', 'null', '*', LATITUD_EMPRESA) = DECODE('"+m.get("latitud_empresa")+"', 'null', '*', '"+m.get("latitud_empresa")+"') AND DECODE('"+m.get("longitud_empresa")+"', 'null', '*', LONGITUD_EMPRESA) = DECODE('"+m.get("longitud_empresa")+"', 'null', '*', '"+m.get("longitud_empresa")+"')");
            totalRegistro = 0;
            dataGrupal = new HashMap<String, Object>();
            while (rs.next()) {
                totalRegistro++;
                dataIndividual = new HashMap<String, Object>();
                dataIndividual.put("ID_EMPRESA", rs.getString("ID_EMPRESA"));
                dataIndividual.put("NOMBRE_EMPRESA", rs.getString("NOMBRE_EMPRESA"));
                dataIndividual.put("NOMBRE_PAIS", rs.getString("NOMBRE_PAIS"));
                dataIndividual.put("RUT_EMPRESA", rs.getString("RUT_EMPRESA"));
                dataIndividual.put("RAZON_SOCIAL", rs.getString("RAZON_SOCIAL"));
                dataIndividual.put("GIRO_EMPRESA", rs.getString("GIRO_EMPRESA"));
                dataIndividual.put("DIRECCION_EMPRESA", rs.getString("DIRECCION_EMPRESA"));
                dataIndividual.put("FONO_CONTACTO", rs.getString("FONO_CONTACTO"));
                dataIndividual.put("EMAIL_EMPRESA", rs.getString("EMAIL_EMPRESA"));
                dataIndividual.put("LATITUD_EMPRESA", rs.getString("LATITUD_EMPRESA"));
                dataIndividual.put("LONGITUD_EMPRESA", rs.getString("LONGITUD_EMPRESA"));
                dataGrupal.put(String.valueOf(totalRegistro), dataIndividual);
                dataGrupal2.put("PROYECTO_EMPRESA", dataGrupal);
            }

            StringSql.setLength(0);
            stmt.close();
            camposytipos = new String[] { "ID_TIPO","I","DESCRIPCION_TIPO","S" };
            // Construir el SQL
            StringSql.append("SELECT * FROM VW_PROYECTO_TIPOS");
            sqlWhereAnd = " WHERE ";
            for (int i = 0; i < camposytipos.length;i+=2) {
                if (m.containsKey(camposytipos[i].toLowerCase())){
                    if (camposytipos[i+1].charAt(0) == 'D') {
                        StringSql.append( sqlWhereAnd + camposytipos[i] + " = to_date( ? , 'dd/mm/yyyy')");
                    } else if (camposytipos[i+1].charAt(0) == 'H') {
                        StringSql.append( sqlWhereAnd + camposytipos[i] + " = to_date( ? , 'dd/mm/yyyy hh24miss')");
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

//            rs = stmt.executeQuery("SELECT * FROM VW_PROYECTO_TIPOS WHERE "
//                    + "DECODE('" + m.get("id_tipo") + "', 'null', '*', ID_TIPO) = DECODE('" + m.get("id_tipo") + "', 'null', '*', " + m.get("id_tipo") + ") AND DECODE('" + m.get("descripcion_tipo") + "', 'null', '*', DESCRIPCION_TIPO) = DECODE('" + m.get("descripcion_tipo") + "', 'null', '*', '" + m.get("descripcion_tipo") + "')");

            totalRegistro = 0;
            dataGrupal = new HashMap<String, Object>();
            while (rs.next()) {
                totalRegistro++;
                dataIndividual = new HashMap<String, Object>();
                dataIndividual.put("ID_TIPO", rs.getString("ID_TIPO"));
                dataIndividual.put("DESCRIPCION_TIPO", rs.getString("DESCRIPCION_TIPO"));
                dataGrupal.put(String.valueOf(totalRegistro), dataIndividual);
                dataGrupal2.put("PROYECTO_TIPOS", dataGrupal);
            }
            StringSql.setLength(0);
            stmt.close();
            camposytipos = new String[] { "ID_MAGNITUD","I","NOMBRE_MAGNITUD","S" };
            // Construir el SQL
            StringSql.append("SELECT * FROM VW_PROYECTO_MAGNITUD");
            sqlWhereAnd = " WHERE ";
            for (int i = 0; i < camposytipos.length;i+=2) {
                if (m.containsKey(camposytipos[i].toLowerCase())){
                    if (camposytipos[i+1].charAt(0) == 'D') {
                        StringSql.append( sqlWhereAnd + camposytipos[i] + " = to_date( ? , 'dd/mm/yyyy')");
                    } else if (camposytipos[i+1].charAt(0) == 'H') {
                        StringSql.append( sqlWhereAnd + camposytipos[i] + " = to_date( ? , 'dd/mm/yyyy hh24miss')");
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

//            rs = stmt.executeQuery("SELECT * FROM VW_PROYECTO_MAGNITUD WHERE "
//                    + "DECODE('" + m.get("id_magnitud") + "', 'null', '*', ID_MAGNITUD) = DECODE('" + m.get("id_magnitud") + "', 'null', '*', " + m.get("id_magnitud") + ") AND DECODE('" + m.get("nombre_magnitud") + "', 'null', '*', NOMBRE_MAGNITUD) = DECODE('" + m.get("nombre_magnitud") + "', 'null', '*', '" + m.get("nombre_magnitud") + "')");

            totalRegistro = 0;
            dataGrupal = new HashMap<String, Object>();
            while (rs.next()) {
                totalRegistro++;
                dataIndividual = new HashMap<String, Object>();
                dataIndividual.put("ID_MAGNITUD", rs.getString("ID_MAGNITUD"));
                dataIndividual.put("NOMBRE_MAGNITUD", rs.getString("NOMBRE_MAGNITUD"));
                dataGrupal.put(String.valueOf(totalRegistro), dataIndividual);
                dataGrupal2.put("PROYECTO_MAGNITUD", dataGrupal);
            }
		    StringSql.setLength(0);
			stmt.close();
		    camposytipos = new String[] { "ID_PRIORIDAD","I","NOMBRE_PRIORIDAD","S" };
            // Construir el SQL
            StringSql.append("SELECT * FROM VW_PROYECTO_PRIORIDAD");
            sqlWhereAnd = " WHERE ";
            for (int i = 0; i < camposytipos.length;i+=2) {
                if (m.containsKey(camposytipos[i].toLowerCase())){
                    if (camposytipos[i+1].charAt(0) == 'D') {
                        StringSql.append( sqlWhereAnd + camposytipos[i] + " = to_date( ? , 'dd/mm/yyyy')");
                    } else if (camposytipos[i+1].charAt(0) == 'H') {
                        StringSql.append( sqlWhereAnd + camposytipos[i] + " = to_date( ? , 'dd/mm/yyyy hh24miss')");
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

//            rs = stmt.executeQuery("SELECT * FROM VW_PROYECTO_PRIORIDAD WHERE "
//                    + "DECODE('" + m.get("id_prioridad") + "', 'null', '*', ID_PRIORIDAD) = DECODE('" + m.get("id_prioridad") + "', 'null', '*', " + m.get("id_prioridad") + ") AND DECODE('" + m.get("nombre_prioridad") + "', 'null', '*', NOMBRE_PRIORIDAD) = DECODE('" + m.get("nombre_prioridad") + "', 'null', '*', '" + m.get("nombre_prioridad") + "')");

            totalRegistro = 0;
            dataGrupal = new HashMap<String, Object>();
            while (rs.next()) {
                totalRegistro++;
                dataIndividual = new HashMap<String, Object>();
                dataIndividual.put("ID_PRIORIDAD", rs.getString("ID_PRIORIDAD"));
                dataIndividual.put("NOMBRE_PRIORIDAD", rs.getString("NOMBRE_PRIORIDAD"));
                dataGrupal.put(String.valueOf(totalRegistro), dataIndividual);
                dataGrupal2.put("PROYECTO_PRIORIDAD", dataGrupal);
            }
		    StringSql.setLength(0);
			stmt.close();
		    camposytipos = new String[] { "ID_AREA","I","NOMBRE_AREA","S","FECHA_CREACION","D","FECHA_MODIFICACION","D","ID_JEFE_AREA","I","ID_AREA_PADRE","I","ID_POSICION_RELEVANCIA","I","ID_EMPRESA","I","NOMBRE_EMPRESA","S" };
            // Construir el SQL
            StringSql.append("SELECT * FROM VW_PROYECTO_AREA");
            sqlWhereAnd = " WHERE ";
            for (int i = 0; i < camposytipos.length;i+=2) {
                if (m.containsKey(camposytipos[i].toLowerCase())){
                    if (camposytipos[i+1].charAt(0) == 'D') {
                        StringSql.append( sqlWhereAnd + camposytipos[i] + " = to_date( ? , 'dd/mm/yyyy')");
                    } else if (camposytipos[i+1].charAt(0) == 'H') {
                        StringSql.append( sqlWhereAnd + camposytipos[i] + " = to_date( ? , 'dd/mm/yyyy hh24miss')");
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
//            rs = stmt.executeQuery("SELECT * FROM VW_PROYECTO_AREA WHERE "
//                    + "DECODE('" + m.get("id_area") + "', 'null', '*', ID_AREA) = DECODE('" + m.get("id_area") + "', 'null', '*', " + m.get("id_area") + ") AND DECODE('" + m.get("nombre_area") + "', 'null', '*', NOMBRE_AREA) = DECODE('" + m.get("nombre_area") + "', 'null', '*', '" + m.get("nombre_area") + "') AND DECODE('" + m.get("nombre_empresa") + "', 'null', '*', NOMBRE_EMPRESA) = DECODE('" + m.get("nombre_empresa") + "', 'null', '*', '" + m.get("nombre_empresa") + "') AND DECODE('" + m.get("id_empresa") + "', 'null', '*', ID_EMPRESA) = DECODE('" + m.get("id_empresa") + "', 'null', '*', " + m.get("id_empresa") + ")");

            totalRegistro = 0;
            dataGrupal = new HashMap<String, Object>();
            while (rs.next()) {
                totalRegistro++;
                dataIndividual = new HashMap<String, Object>();
                dataIndividual.put("ID_AREA", rs.getString("ID_AREA"));
                dataIndividual.put("NOMBRE_AREA", rs.getString("NOMBRE_AREA"));
                dataIndividual.put("NOMBRE_EMPRESA", rs.getString("NOMBRE_EMPRESA"));
                dataIndividual.put("ID_EMPRESA", rs.getString("ID_EMPRESA"));
                dataGrupal.put(String.valueOf(totalRegistro), dataIndividual);
                dataGrupal2.put("PROYECTO_AREA", dataGrupal);
            }

		    StringSql.setLength(0);
			stmt.close();
		    camposytipos = new String[] { "ID_USUARIO","I","NOMBRE","S","ID_EMPRESA","I","NOMBRE_EMPRESA","S","ID_AREA","I","NOMBRE_AREA","S" };
            // Construir el SQL
            StringSql.append("SELECT * FROM VW_PROYECTO_SPONSOR");
            sqlWhereAnd = " WHERE ";
            for (int i = 0; i < camposytipos.length;i+=2) {
                if (m.containsKey(camposytipos[i].toLowerCase())){
                    if (camposytipos[i+1].charAt(0) == 'D') {
                        StringSql.append( sqlWhereAnd + camposytipos[i] + " = to_date( ? , 'dd/mm/yyyy')");
                    } else if (camposytipos[i+1].charAt(0) == 'H') {
                        StringSql.append( sqlWhereAnd + camposytipos[i] + " = to_date( ? , 'dd/mm/yyyy hh24miss')");
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

//            rs = stmt.executeQuery("SELECT * FROM VW_PROYECTO_SPONSOR WHERE "
//                    + "DECODE('" + m.get("id_usuario") + "', 'null', '*', ID_USUARIO) = DECODE('" + m.get("id_usuario") + "', 'null', '*', " + m.get("id_usuario") + ") AND DECODE('" + m.get("nombre") + "', 'null', '*', NOMBRE) = DECODE('" + m.get("nombre") + "', 'null', '*', '" + m.get("nombre") + "') AND DECODE('" + m.get("id_empresa") + "', 'null', '*', ID_EMPRESA) = DECODE('" + m.get("id_empresa") + "', 'null', '*', " + m.get("id_empresa") + ") AND DECODE('" + m.get("nombre_empresa") + "', 'null', '*', NOMBRE_EMPRESA) = DECODE('" + m.get("nombre_empresa") + "', 'null', '*', '" + m.get("nombre_empresa") + "') AND DECODE('" + m.get("id_area") + "', 'null', '*', ID_AREA) = DECODE('" + m.get("id_area") + "', 'null', '*', " + m.get("id_area") + ") AND DECODE('" + m.get("nombre_area") + "', 'null', '*', NOMBRE_AREA) = DECODE('" + m.get("nombre_area") + "', 'null', '*', '" + m.get("nombre_area") + "')");

            totalRegistro = 0;
            dataGrupal = new HashMap<String, Object>();
            while (rs.next()) {
                totalRegistro++;
                dataIndividual = new HashMap<String, Object>();
                dataIndividual.put("ID_USUARIO", rs.getString("ID_USUARIO"));
                dataIndividual.put("NOMBRE", rs.getString("NOMBRE"));
                dataIndividual.put("ID_EMPRESA", rs.getString("ID_EMPRESA"));
                dataIndividual.put("NOMBRE_EMPRESA", rs.getString("NOMBRE_EMPRESA"));
                dataIndividual.put("ID_AREA", rs.getString("ID_AREA"));
                dataIndividual.put("NOMBRE_AREA", rs.getString("NOMBRE_AREA"));
                dataGrupal.put(String.valueOf(totalRegistro), dataIndividual);
                dataGrupal2.put("PROYECTO_SPONSOR", dataGrupal);
            }
            StringSql.setLength(0);
            stmt.close();
            camposytipos = new String[] { "ID_PRESUPUESTO","I","ID_CCOSTO","I","NOMBRE_CCOSTO","S","VALOR_CENTRO_COSTO","F","ID_MONEDA","I" };
            // Construir el SQL
            StringSql.append("SELECT * FROM VW_PROYECTO_CCOSTOS");
            sqlWhereAnd = " WHERE ";
            for (int i = 0; i < camposytipos.length;i+=2) {
                if (m.containsKey(camposytipos[i].toLowerCase())){
                    if (camposytipos[i+1].charAt(0) == 'D') {
                        StringSql.append( sqlWhereAnd + camposytipos[i] + " = to_date( ? , 'dd/mm/yyyy')");
                    } else if (camposytipos[i+1].charAt(0) == 'H') {
                        StringSql.append( sqlWhereAnd + camposytipos[i] + " = to_date( ? , 'dd/mm/yyyy hh24miss')");
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

//            rs = stmt.executeQuery("SELECT * FROM VW_PROYECTO_CCOSTOS WHERE "
//                    + "DECODE('" + m.get("id_ccosto") + "', 'null', '*', ID_CCOSTO) = DECODE('" + m.get("id_ccosto") + "', 'null', '*', " + m.get("id_ccosto") + ") AND DECODE('" + m.get("nombre_ccosto") + "', 'null', '*', NOMBRE_CCOSTO) = DECODE('" + m.get("nombre_ccosto") + "', 'null', '*', '" + m.get("nombre_ccosto") + "')");

            totalRegistro = 0;
            dataGrupal = new HashMap<String, Object>();
            while (rs.next()) {
                totalRegistro++;
                dataIndividual = new HashMap<String, Object>();
                dataIndividual.put("ID_CCOSTO", rs.getString("ID_CCOSTO"));
                dataIndividual.put("NOMBRE_CCOSTO", rs.getString("NOMBRE_CCOSTO"));
                dataGrupal.put(String.valueOf(totalRegistro), dataIndividual);
                dataGrupal2.put("PROYECTO_CCOSTOS", dataGrupal);
            }

            StringSql.setLength(0);
            stmt.close();
            camposytipos = new String[] { "ID_ROL","I","NOMBRE_ROL","S","FECHA_CREACION","D","FECHA_MODIFICACION","D","ID_USUARIO_CREACION","I","CODI_GRUPO_BASE","S","ESTADO_ROL","S","FECHA_CREACION_FMT","S","FECHA_MODIFICACION_FMT","S" };
            // Construir el SQL
            StringSql.append("SELECT * FROM VW_SEGURIDAD_ROL");
            sqlWhereAnd = " WHERE ";
            for (int i = 0; i < camposytipos.length;i+=2) {
                if (m.containsKey(camposytipos[i].toLowerCase())){
                    if (camposytipos[i+1].charAt(0) == 'D') {
                        StringSql.append( sqlWhereAnd + camposytipos[i] + " = to_date( ? , 'dd/mm/yyyy')");
                    } else if (camposytipos[i+1].charAt(0) == 'H') {
                        StringSql.append( sqlWhereAnd + camposytipos[i] + " = to_date( ? , 'dd/mm/yyyy hh24miss')");
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

//            rs = stmt.executeQuery("SELECT * FROM VW_SEGURIDAD_ROL WHERE "
//                    + "DECODE('" + m.get("id_rol") + "', 'null', '*', ID_ROL) = DECODE('" + m.get("id_rol") + "', 'null', '*', " + m.get("id_rol") + ") AND DECODE('" + m.get("nombre_rol") + "', 'null', '*', NOMBRE_ROL) = DECODE('" + m.get("nombre_rol") + "', 'null', '*', '" + m.get("nombre_rol") + "')");

            totalRegistro = 0;
            dataGrupal = new HashMap<String, Object>();
            while (rs.next()) {
                totalRegistro++;
                dataIndividual = new HashMap<String, Object>();
                dataIndividual.put("ID_ROL", rs.getString("ID_ROL"));
                dataIndividual.put("NOMBRE_ROL", rs.getString("NOMBRE_ROL"));
                dataGrupal.put(String.valueOf(totalRegistro), dataIndividual);
                dataGrupal2.put("PROYECTO_ROL", dataGrupal);
            }

            String json = gson.toJson(dataGrupal2);
            response.setHeader("access-control-allow-origin", "*");
            out.print(json);

        } catch (SQLException e) {
            //System.out.println("Connection Failed! Check output console");
            logger.error("Error durante la consulta a la base de datos. " + e.getMessage());
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
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                    connection = null;
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
