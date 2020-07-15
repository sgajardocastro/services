package cl.gesvita.ws.obtenerconsultainformecajaresxmesv2;
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
import java.text.ParseException;

import java.util.Date;
import java.util.Calendar;
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
 * Servlet implementation class ObtenerConsultaInformeCajaResxMesV2
 */
@WebServlet("/WSObtenerConsultaInformeCajaResxMesV2/ObtenerConsultaInformeCajaResxMesV2")
public class ObtenerConsultaInformeCajaResxMesV2 extends HttpServlet {

    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(ObtenerConsultaInformeCajaResxMesV2.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ObtenerConsultaInformeCajaResxMesV2() {
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

            StringBuilder StringSql = new StringBuilder();
            // Campos de la consulta con sus tipos
            SimpleDateFormat formato_ddmmyyyy = new SimpleDateFormat("dd/MM/yyyy");
            String[] camposytipos = {"ID_EMPRESA","I","CODI_GRUPO_PROYECTO","S"
            ,"ID_PROYECTO_TEMPLATE","I","FECH_INGRESO_MES","D"
            ,"ALOHA","I","VENTA","I","ALOHA_VENTA","I","MONTO_RECAUDACION","I"
            ,"MONTO_FALTANTE","I","FALTANTE_DOCUMENTADO","I"  };
            
            // Construir el SQL
            StringSql.append("SELECT * FROM VW_CONSULTA_INFORME_CAJA_RESXMES  avtar ");
            String sqlAnd = " WHERE ";
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
            StringSql.append(sqlAnd + " (");
            StringSql.append("exists(");
            StringSql.append("select 1 from VW_SEGURIDAD_USERSGRUPO ugrup");
            StringSql.append(" where avtar.CODI_GRUPO_PROYECTO = ugrup.CODI_GRUPO and ugrup.id_usuario = ?)");
            StringSql.append(" or exists (select 1 from vw_seguridad_usuario usuario");
            StringSql.append(" where usuario.id_usuario = ? and usuario.ID_ROL = 10)");
            StringSql.append(" or exists (select 1 from vw_seguridad_usuario usuario, vw_seguridad_grupo grupo, tb_grupo_tipo grtipo");
            StringSql.append(" where usuario.id_usuario = ?");
            StringSql.append(" and usuario.ID_ROL in (11,2) and avtar.CODI_GRUPO_PROYECTO = grupo.CODI_GRUPO");
            StringSql.append(" and grupo.id_tipo_grupo = grtipo.id_tipo_grupo");
            StringSql.append(" and 	grtipo.NOMBRE_TIPO = 'MESADETRABAJO'");
            StringSql.append(" and grupo.id_area_grupo = usuario.ID_AREA ) )");
			StringSql.append(" and avtar.FECH_INGRESO_MES >= ? ");
            StringSql.append(" and avtar.FECH_INGRESO_MES < ? ");

          // BYSECURITY logger.info("Query NUEVA: " + StringSql.toString());

            stmt = connection.prepareStatement( StringSql.toString() );
            int j= 1;
            for (int i = 0; i < camposytipos.length; i+=2) {
                if (m.containsKey(camposytipos[i].toLowerCase())){
                    switch (camposytipos[i+1].charAt(0)) {
                        case 'I':
                            int intVal = Integer.parseInt(m.get(camposytipos[i].toLowerCase()));
                          // BYSECURITY logger.info("val["+ j +"]= " + intVal);
                            stmt.setInt(j, intVal);
                            break;
                        case 'F':
                            float floatVal = Float.parseFloat(m.get(camposytipos[i].toLowerCase()));
                          // BYSECURITY logger.info("val["+ j +"]= " + floatVal);
                            stmt.setFloat(j, floatVal);
                            break;
                        case 'S':
                        case 'D':
                            String val = m.get(camposytipos[i].toLowerCase());
                          // BYSECURITY logger.info("val["+ j +"]= " + val);
                            stmt.setString(j, val );
                            break;
                    }
                    j++;
                }
            }
            int intVal = Integer.parseInt(m.get("id_usuario"));
            stmt.setInt(j, intVal);
            stmt.setInt(j + 1, intVal);
            stmt.setInt(j + 2, intVal);

            Date f_desde = formato_ddmmyyyy.parse(m.get("FECH_INGRESO_MES-MIN"));
            Date f_hasta = formato_ddmmyyyy.parse(m.get("FECH_INGRESO_MES-MAX"));
            java.sql.Date f_desdesql = new java.sql.Date(f_desde.getTime());
            
            Calendar cal = Calendar.getInstance();
            cal.setTime(f_hasta);
            cal.add(Calendar.DAY_OF_YEAR, 1);

            stmt.setDate(j+3,new java.sql.Date(f_desde.getTime()));
            stmt.setDate(j+4,new java.sql.Date(cal.getTime().getTime()));

            rs = stmt.executeQuery();
            dataGrupal = new HashMap<String, Object>();
            while (rs.next()) {
                totalRegistro++;
                dataIndividual = new HashMap<String, Object>();
                j= 1;
                for (int i = 0; i < camposytipos.length; i += 2) {
                    char k= camposytipos[i + 1].charAt(0);
                    if (k!='-'){
                        dataIndividual.put(camposytipos[i], rs.getString(camposytipos[i]));
                    }
                }
                dataGrupal.put(String.valueOf(totalRegistro), dataIndividual);
            }

            String json = gson.toJson(dataGrupal);
            response.setHeader("access-control-allow-origin", "*");
            out.print(json);

        } catch (ParseException | SQLException e) {
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
