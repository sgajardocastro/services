/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.gesvita.ws.obtener.lib;

import cl.gesvita.util.ClobUtil;
import cl.gesvita.ws.obtener.exception.GesvitaException;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.MalformedJsonException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

/**
 *
 * @author Felipe
 */
public class ObtenerLib {

    private final static String[][] TABLE_SYMBOLS = new String[][]{
        new String[]{"lt", "", "<", ""},
        new String[]{"gt", "", ">", ""},
        new String[]{"le", "", "<=", ""},
        new String[]{"ge", "", ">=", ""},
        new String[]{"ne", " not ", "=", ""},
        new String[]{"sw", " UPPER (", ") like UPPER(", ") || '%'"},
        new String[]{"ct", " UPPER (", ") like '%' || UPPER(", ") || '%'"},
        new String[]{"nsw", " not UPPER (", ") like UPPER(", ") || '%'"},
        new String[]{"nct", " not UPPER (", ") like '%' || UPPER(", ") || '%'"},
        new String[]{"null", "", " is ", " null "}
    };
    
    
    private static final SimpleDateFormat FORMATO_DDMMYYYY = new SimpleDateFormat("dd/MM/yyyy");
    private static final SimpleDateFormat FORMATO_DDMMYYYYHHMMSS = new SimpleDateFormat("dd/MM/yy Hmmss");

    public static String getDefaultObtenerResoultSet(Logger logger, Connection connection, String[] camposytipos, String viewname, Map<String, String> m) throws SQLException {
        ResultSet rs = null;
        PreparedStatement stmt = null;

        /*
        '+' => Don't filter it
        '-' => Don't filter and don't output it
        '/' => Don't output it
         */
        try {
            Gson gson = new Gson();
            int totalRegistro = 0;
            Map<String, Object> dataGrupal;
            Map<String, Object> dataIndividual;
            StringBuilder StringSql = new StringBuilder();
            // Campos de la consulta con sus tipos
            // Construir el SQL
            StringSql.append("SELECT * FROM ");
            StringSql.append(viewname);
            String sqlWhereAnd = " WHERE ";
            for (int i = 0; i < camposytipos.length; i += 2) {
                char k = camposytipos[i + 1].charAt(camposytipos[i + 1].length() - 1);
                char k0 = camposytipos[i + 1].charAt(0);
                if (m.containsKey(camposytipos[i].toLowerCase()) && k0 != '-' && k0 != '+') {
                    if (k == 'D') {
                        StringSql.append(sqlWhereAnd).append(camposytipos[i])
                                .append(" = to_date( ? , 'dd/mm/yyyy')");
                    } else if (k == 'H') {
                        StringSql.append(sqlWhereAnd).append(camposytipos[i])
                                .append(" = to_date( ? , 'dd/mm/yyyy hh24miss')");
                    } else if (k != 'C') {
                        StringSql.append(sqlWhereAnd).append(camposytipos[i])
                                .append(" = ?");
                    }
                    if (k != 'C') {
                        sqlWhereAnd = " AND ";
                    }
                }
            }
            // BYSECURITY logger.info("Query NUEVA: " + StringSql.toString());

            stmt = connection.prepareStatement(StringSql.toString());
            for (int i = 0, j = 1; i < camposytipos.length; i += 2) {
                char k = camposytipos[i + 1].charAt(camposytipos[i + 1].length() - 1);
                char k0 = camposytipos[i + 1].charAt(0);
                if (m.containsKey(camposytipos[i].toLowerCase()) && k0 != '-' && k0 != '+' && k != 'C') {
                    switch (k) {
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

            dataGrupal = new HashMap<>();
            while (rs.next()) {
                totalRegistro++;
                dataIndividual = new HashMap<>();
                for (int i = 0, j = 1; i < camposytipos.length; i += 2) {
                    char k = camposytipos[i + 1].charAt(camposytipos[i + 1].length() - 1);
                    char k0 = camposytipos[i + 1].charAt(0);
                    if (k0 != '-' && k0 != '/') {
                        if (k == 'D') {
                            dataIndividual.put(camposytipos[i], rs.getString(camposytipos[i]));
                        } else if (k != 'C') {
                            dataIndividual.put(camposytipos[i], rs.getString(camposytipos[i]));
                        } else {
                            dataIndividual.put(camposytipos[i],
                                    ClobUtil.clobToString(rs.getClob(camposytipos[i])));
                        }
                    }
                }
                dataGrupal.put(String.valueOf(totalRegistro), dataIndividual);
            }
            return gson.toJson(dataGrupal);

        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    logger.error(e.getMessage(), e.fillInStackTrace());
                }
            }

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    logger.error(e.getMessage(), e.fillInStackTrace());
                }
            }
        }
    }

    public static String getDefaultObtenerResoultSetExt(Logger logger, Connection connection, String[] camposytipos, String viewname, JsonObject input) throws SQLException, GesvitaException {
        return getDefaultObtenerResoultSetExt(logger, connection, camposytipos, viewname, input , "" ,  " * " , "" , null); 
    }
    
    
    public static String getDefaultObtenerResoultSetExt(Logger logger, Connection connection, String[] camposytipos, String viewname, JsonObject input,
                         String extratabla, String campos, String continuacion, Map<String, String> mapa)
                         throws SQLException, GesvitaException {
        ResultSet rs = null;
        PreparedStatement stmt = null;
        Map<String, String[]> mapaSymbols = new HashMap<>();
        for (String x[] : TABLE_SYMBOLS) {
            mapaSymbols.put(x[0].toLowerCase(), x);
        }
        try {
            Gson gson = new Gson();
            int totalRegistro = 0;
            Map<String, Object> dataGrupal;
            Map<String, Object> dataIndividual;
            StringBuilder StringSql = new StringBuilder();
            // Campos de la consulta con sus tipos
            // Construir el SQL
            StringSql.append("SELECT ");
            if (mapa != null) {
                StringSql.append(ObtenerLib.replaceTokens(mapa, campos));
            } else {
                StringSql.append(campos);
            }
            StringSql.append(" FROM ");
            StringSql.append(viewname);
            StringSql.append(" PPLVIEW");
            
            // Comienzo código nuevo
            if (mapa != null && extratabla != null && !extratabla.equals("")) {
                StringSql.append(ObtenerLib.replaceTokens(mapa, extratabla));
            }
            // Fin código nuevo
            
            String sqlWhereAnd = " WHERE ";
            for (int i = 0; i < camposytipos.length; i += 2) {
                char k = camposytipos[i + 1].charAt(0);

                JsonElement value = input.get(camposytipos[i].toLowerCase());

                if (value != null && k != '-' && k != '+') {

                    if (value instanceof JsonArray) {
                        JsonArray value2 = (JsonArray) value;
                        Iterator it = value2.iterator();
                        StringSql.append(sqlWhereAnd).append(camposytipos[i])
                                .append(" in (");
                        // BYSECURITY logger.info("clausula (in) para : " + camposytipos[i]);
                        String sqlNadaOComa = "";
                        while (it.hasNext()) {
                            it.next();
                            if (k == 'D') {
                                StringSql.append(sqlNadaOComa).append(" to_date( ? , 'dd/mm/yyyy')");
                            } else if (k == 'H') {
                                StringSql.append(sqlNadaOComa).append(" to_date( ? , 'dd/mm/yyyy hh24miss')");
                            } else if (k != 'C') {
                                StringSql.append(sqlNadaOComa).append("?");
                            }
                            sqlNadaOComa = ",";
                        }
                        StringSql.append(")");
                        sqlWhereAnd = " AND ";
                    } else if (value instanceof JsonObject) {
                        JsonObject obj = (JsonObject) value;
                        Iterator<String> it = obj.keySet().iterator();
                        while (it.hasNext()) {
                            String key = it.next().toLowerCase();
                            // BYSECURITY logger.info("key : " + key);
                            if (!mapaSymbols.containsKey(key)) {
                                continue;
                            }
                            String fields[] = mapaSymbols.get(key);
                            // BYSECURITY logger.info("values : '" + fields[1] + "'/'" + fields[2] + "'/" + fields[3] + "'");
                            // CAMBIO 1:
                             if (key.equals("null")) {
                                StringSql.append(sqlWhereAnd).append(fields[1]).append(camposytipos[i])
                                        .append(fields[2]).append(fields[3]);
                             } else if (k == 'D') {
                            // if (k == 'D') {
                                StringSql.append(sqlWhereAnd).append(fields[1]).append(camposytipos[i])
                                        .append(fields[2]).append(" to_date( ? , 'dd/mm/yyyy')").append(fields[3]);
                            } else if (k == 'H') {
                                StringSql.append(sqlWhereAnd).append(fields[1]).append(camposytipos[i])
                                        .append(fields[2]).append(" to_date( ? , 'dd/mm/yyyy hh24miss')").append(fields[3]);
                            } else if (k != 'C') {
                                StringSql.append(sqlWhereAnd).append(fields[1]).append(camposytipos[i])
                                        .append(fields[2]).append("?").append(fields[3]);
                            }
                            sqlWhereAnd = " AND ";
                        }
                    } else {
                        if (k == 'D') {
                            StringSql.append(sqlWhereAnd).append(camposytipos[i])
                                    .append(" = to_date( ? , 'dd/mm/yyyy')");
                        } else if (k == 'H') {
                            StringSql.append(sqlWhereAnd).append(camposytipos[i])
                                    .append(" = to_date( ? , 'dd/mm/yyyy hh24miss')");
                        } else if (k != 'C') {
                            StringSql.append(sqlWhereAnd).append(camposytipos[i]).append(" = ?");
                        }
                        if (k != 'C') {
                            sqlWhereAnd = " AND ";
                        }
                    }
                }
            }
            if (mapa != null && !continuacion.equals("")) {
                mapa.put("WHEREAND",sqlWhereAnd);
                StringSql.append(ObtenerLib.replaceTokens(mapa, continuacion));
            } else {
                StringSql.append(continuacion);
            }
            // Order BY
            JsonElement orderby = input.get("ORDERBY");
            if (orderby != null && orderby instanceof JsonArray ) {
                JsonArray orderby2 = (JsonArray) orderby;
                Iterator it = orderby2.iterator();
                boolean hayorden = false;
                StringBuilder stringOrder = new StringBuilder();
                
                Map mapacampos;
                mapacampos = new HashMap<>();
                for (int i = 0;i<camposytipos.length;i+=2) {
                    mapacampos.put(camposytipos[i],i);
                }
                
                stringOrder.append(" ORDER BY");
                String sqlNadaOComa = " ";
                String tipoorden;
                while (it.hasNext()) {
                    String field = cleanInputFieldString(("" + it.next()).toUpperCase());
                    // BYSECURITY logger.info("FIELD ORDER BY: " + field);
                    tipoorden = "";
                    if (field.length() > 0 && field.charAt(0) == '-') {
                        field = field.substring(1);
                        tipoorden = " DESC";
                    }
                    if (mapacampos.containsKey(field)){
                        stringOrder.append(sqlNadaOComa);
                        stringOrder.append(field);
                        stringOrder.append(tipoorden);
                        sqlNadaOComa = ",";
                        hayorden = true;
                    }
                }
                if (hayorden) {
                    StringSql.append(stringOrder);
                }
            }

            // BYSECURITY 
            logger.info("Query NUEVA1: " + StringSql.toString());

            stmt = connection.prepareStatement(StringSql.toString());
            // BYSECURITY logger.info("query_preparada");
            for (int i = 0, j = 1; i < camposytipos.length; i += 2) {
                char k = camposytipos[i + 1].charAt(camposytipos[i + 1].length() - 1);
                char k0 = camposytipos[i + 1].charAt(0);
                String campo = camposytipos[i].toLowerCase();
                JsonElement value = input.get(campo);
                if (value != null && k0 != '-' && k0 != '+' && k != 'C') {
                    // BYSECURITY logger.info("CAMPO[" + campo + "]: " + value);
                    if (value instanceof JsonArray) {
                        // BYSECURITY logger.info("JsonArray");
                        JsonArray value2 = (JsonArray) value;
                        Iterator<JsonElement> it = value2.iterator();
                        while (it.hasNext()) {
                            String data = "" + it.next();
                            // BYSECURITY logger.info("Fill JsonArray " + j + "," + k + "," + data );
                            fillPreparedParam(logger, j, k, stmt, data);
                            j++;
                        }
                    } else if (value instanceof JsonObject) {
                        // BYSECURITY logger.info("JsonObject");
                        JsonObject value2 = (JsonObject) value;
                        // BYSECURITY logger.info("JsonObject.value : " + value2.toString() );
                        Iterator<String> it = value2.keySet().iterator();
                        while (it.hasNext()) {
                            String key = it.next().toLowerCase();
                            // BYSECURITY logger.info(" itera : " + key );
                            // CAMBIO #2 para incorporar is NULL
                            if (!mapaSymbols.containsKey(key) || key.equals("null") ) {
                                continue;
                            }
                            String data = "" + value2.get(key);
                            // BYSECURITY logger.info("Fill " + j + "," + k + "," + data );
                            fillPreparedParam(logger, j, k, stmt, "" + value2.get(key));
                            j++;
                        }
                    } else {
                        // BYSECURITY logger.info("NO JsonObject,  NO JsonObject");
                        String data = "" + input.get(camposytipos[i].toLowerCase()).toString();
                        // BYSECURITY logger.info("Fill " + j + "," + k + "," + data );
                        fillPreparedParam(logger, j, k, stmt, data);
                        j++;
                    }
                }
            }
            rs = stmt.executeQuery();

            dataGrupal = new HashMap<>();
            while (rs.next()) {
                // BYSECURITY logger.info("Registro leido de BD");
                totalRegistro++;
                dataIndividual = new HashMap<>();
                for (int i = 0, j = 1; i < camposytipos.length; i += 2) {
                    char k = camposytipos[i + 1].charAt(camposytipos[i + 1].length() - 1);
                    char k0 = camposytipos[i + 1].charAt(0);
                    if (k0 != '-' && k0 != '/') {
                        // BYSECURITY logger.info("Leer campo : '" + camposytipos[i] + "', tipo: " + k);
                        if (k == 'S') {
                            String t = rs.getString(camposytipos[i]);
                            if (t==null) {
                                dataIndividual.put(camposytipos[i], "");
                            } else {
                                dataIndividual.put(camposytipos[i], t);
                            }
                        } else if (k != 'C') {
                            dataIndividual.put(camposytipos[i], rs.getString(camposytipos[i]));
                        } else {
                            Clob t = rs.getClob(camposytipos[i]);
                            if (t==null) {
                                dataIndividual.put(camposytipos[i], "");
                            } else {
                                dataIndividual.put(camposytipos[i], ClobUtil.clobToString(t));
                            }
                        }
                    }
                }
                dataGrupal.put(String.valueOf(totalRegistro), dataIndividual);
            }
            String salida = gson.toJson(dataGrupal);
            // BYSECURITY2 logger.info(salida);
            return salida;

        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    logger.error(e.getMessage(), e.fillInStackTrace());
                }
            }

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    logger.error(e.getMessage(), e.fillInStackTrace());
                }
            }
        }
    }

    private static void fillPreparedParam(Logger logger, int j, char k, PreparedStatement stmt, String field) throws SQLException, GesvitaException {
        Date valDate;
        String val;
        Clob cl;
        switch (k) {
            case 'I':
                int intVal = cleanInputFieldInt(field);
                // BYSECURITY1 logger.info("val(I)[" + j + "]= " + intVal);
                
                stmt.setInt(j, intVal);
                break;
            case 'F':
                float floatVal = cleanInputFieldFloat(field);
                // BYSECURITY1 logger.info("val(F)[" + j + "]= " + floatVal);
                stmt.setFloat(j, floatVal);
                break;
            case 'C':
                val = cleanInputFieldString(field);
                // BYSECURITY1 logger.info("val(C)[" + j + "]= " + val);
                stmt.setString(j, val);
                break;
            case 'S':
                val = cleanInputFieldString(field);
                // BYSECURITY1 logger.info("val(S)[" + j + "]= " + val);
                stmt.setString(j, val);
                break;
            case 'H':
                val = cleanInputFieldString(field);
                try {
                    valDate = FORMATO_DDMMYYYYHHMMSS.parse(val);
                } catch (ParseException ex) {
                    throw new GesvitaException("Se esperaba fecha formato dd/MM/yyyy hh24miss");
                }
                // BYSECURITY1 logger.info("val(H)[" + j + "]= " + val);
                stmt.setString(j, val);
                break;
            case 'D':
                val = cleanInputFieldString(field);
                try {
                    valDate = FORMATO_DDMMYYYY.parse(val);
                } catch (ParseException ex) {
                    throw new GesvitaException("Se esperaba fecha formato dd/MM/yyyy");
                }
                // BYSECURITY1 logger.info("val(D)[" + j + "]= " + val);
                stmt.setString(j, val);
                break;
        }
    }

    public static void setLogParam(Class clase) throws GesvitaException {
        try {
            Properties propsLog4 = new Properties();
            InputStream inputStreamLog4 = clase.getResourceAsStream("log4j.properties");
            propsLog4.load(inputStreamLog4);
            PropertyConfigurator.configure(propsLog4);
        } catch (IOException ex) {
            throw new GesvitaException("IOException in setLogParam()");
        }
    }

    public static DataSource getDataSourcePool(Logger logger) throws GesvitaException {
        try {
            InitialContext ctx;
            ctx = new InitialContext();
            // Context env = (Context) ctx.lookup("java:comp/env");
            DataSource dsource = (DataSource) ctx.lookup("java:comp/env/jdbc/SGPIDB");
            return dsource;
        } catch (NamingException e) {
            logger.error("NamingException", e.fillInStackTrace());
            throw new GesvitaException("NamingException al obtener Pool a BD");
        }
    }

    public static DataSource getDataSource(Logger logger) throws GesvitaException {
        String archivoPro;
        Properties prop = new Properties();
        try {
            archivoPro = System.getProperty("catalina.base") + "/webapps/propiedades/db.properties";
            prop.load(new FileInputStream(archivoPro));
//            String pooled = prop.getProperty("pooled");
//            if (pooled != null & pooled.equalsIgnoreCase("true"))
//                return getDataSourcePool(logger);
        } catch (FileNotFoundException e) {
            logger.error("Error FileNotFoundException", e.fillInStackTrace());
            throw new GesvitaException("FileNotFoundException in getDataSource()");
        } catch (IOException ex) {
            logger.error("Error IOException", ex.fillInStackTrace());
            throw new GesvitaException("IOException in getDataSource()");
        }
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e1) {
            logger.error("Error ", e1.fillInStackTrace());
        }
        PoolProperties p = new PoolProperties();
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
        return datasource;
    }

    public static float cleanInputFieldFloat(String value) throws GesvitaException {
        try {
            return Float.parseFloat(cleanInputFieldString(value));
        } catch (NumberFormatException e) {
            throw new GesvitaException("Expected a Float for field, but get a String: (" + value + ")");
        }
    }

    public static int cleanInputFieldInt(String value) throws GesvitaException {
        try {
            return Integer.parseInt(cleanInputFieldString(value));
        } catch (NumberFormatException e) {
            throw new GesvitaException("Expected a int for field, but get a String: (" + value + ")");
        }
    }

    public static String cleanInputFieldString(String value) {
        String result = value;
        if (result.startsWith("\"") && result.endsWith("\"")) {
            result = result.substring(1, result.length() - 1);
        }
        if (result.startsWith("'") && result.endsWith("'")) {
            result = result.substring(1, result.length() - 1);
        }
        return result;
    }

    public static JsonObject readInput(Logger logger, HttpServletRequest request) throws GesvitaException {
        try {
            StringBuilder sb = new StringBuilder();
            String s;
            while ((s = request.getReader().readLine()) != null) {
                sb.append(s);
            }
            JsonParser parser = new JsonParser();
            JsonElement resp = parser.parse(sb.toString());
            if (resp instanceof JsonObject) {
                return (JsonObject) resp;
            } else if (resp instanceof JsonNull) {
                return new JsonObject();
            } else {
                return new JsonObject();
            }
        } catch (MalformedJsonException ex) {
            logger.error("Ha ocurrido un error de tipo MalformedJsonException. ", ex.fillInStackTrace());
            // out.print("{\"resultadoEjecucion\":\"NOK\",\"mensaje\":\"Ocurrio un error\",\"mensajeTec\":\"" + ex.getClass().toString() + ": " + ex.getMessage() + "\"}");
            throw new GesvitaException("MalformedJsonException in readInput()");
        } catch (NullPointerException e1) {
            logger.error("Ha ocurrido un error de tipo NullPointer. ", e1.fillInStackTrace());
            throw new GesvitaException("NullPointerException in readInput()");
        } catch (IllegalArgumentException e2) {
            logger.error("Ha ocurrido un error de tipo IllegalArgument. ", e2.fillInStackTrace());
            throw new GesvitaException("IllegalArgumentException in readInput()");
        } catch (IndexOutOfBoundsException e3) {
            logger.error("Ha ocurrido un error de tipo IndexOutOfBounds. ", e3.fillInStackTrace());
            // out.print("{\"resultadoEjecucion\":\"NOK\",\"mensaje\":\"Ocurrio un error\",\"mensajeTec\":\"" + e3.getClass().toString() + ": " + e3.getMessage() + "\"}");
            throw new GesvitaException("IndexOutOfBoundsException in readInput()");
        } catch (IOException ex) {
            logger.error("Ha ocurrido un error de tipo IOException", ex.fillInStackTrace());
            throw new GesvitaException("IOException in readInput()");
//        } catch (Exception ex) {
//            logger.error("Ha ocurrido un error" ,ex.fillInStackTrace());
//            throw new GesvitaException("Exception in readInput()" );
        }
    }

    public static Map<String, List<String>> ExtractParams(Logger logger, JsonObject jsonObj) {
        Map<String, List<String>> salida = new HashMap<>();
        Iterator<String> it = jsonObj.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            int p = key.indexOf('-');
            List<String> lista;

            if (p != -1) {
                if (!salida.containsKey(key)) {
                    lista = new ArrayList<>();
                    salida.put(key, lista);
                } else {
                    lista = salida.get(key);
                }
                lista.add("");
                salida.replace(key, lista);
            } else {
                if (!salida.containsKey(key.substring(0, p))) {
                    lista = new ArrayList<>();
                    salida.put(key, lista);
                } else {
                    lista = salida.get(key);
                }
                lista.add(key.substring(p + 1));
                salida.replace(key, lista);
            }
        }
        return salida;
    }

    public static void addToken(Map<String, String> tokensMap, char tipo, JsonObject input, String name) throws GesvitaException {
        String name_lower= name.toLowerCase();
        if (!input.has(name_lower)){
            return;
        }
        JsonElement obj = input.get(name_lower);
        if (obj instanceof JsonArray) {
            if (tipo == 'I' || tipo == 'D')
                throw new GesvitaException("Campo '" + name + "' no se esperaba como JsonArray");
        } else if (obj instanceof JsonObject) {
            if (tipo == 'I' || tipo == 'D')
                throw new GesvitaException("Campo '" + name + "' no se esperaba como JsonObject");
        }
        addToken(tokensMap, tipo, name, obj.toString());
    }
    
    public static void addToken(Map<String, String> tokensMap, char tipo, String name, String value) throws GesvitaException {
        if ( (value.startsWith("'") && value.endsWith("'"))
                || (value.startsWith("\"") && value.endsWith("\"")) ){
           value = value.substring(1, value.length()-1);
        }
        if ( (name.startsWith("'") && name.endsWith("'"))
                || (name.startsWith("\"") && name.endsWith("\"")) ){
           name = name.substring(1, name.length()-1);
        }
        String regex_int = "^([\\d]+)$";
        String regex_date = "^(\\d{1,2})(\\/|-)(\\d{1,2})\\2(\\d{4})$";
        String regex_varchar = "^([\\w ]+)$";
        String regex_sql = "^([^;]+)$";
        Pattern pattern;
        switch(tipo) {
            case 'I' : pattern = Pattern.compile(regex_int);
                  break;
            case 'D' : pattern = Pattern.compile(regex_date);
                  break;
            case 'V' : pattern = Pattern.compile(regex_varchar);
                  break;
            case 'S' : pattern = Pattern.compile(regex_sql);
                  break;
            default: pattern = Pattern.compile(regex_sql);
        }
        Matcher m = pattern.matcher(value);
        if (!m.find()) {
            throw new GesvitaException("Campo '" + name +"' era esperado de tipo : " + tipo);
        } else {
            tokensMap.put(name,value);
        }
    }
    
    public static String replaceTokens(Map<String, String> tokensMap, String toInspect) {
        String regex = "\\{\\{([\\w\\.]+)\\}\\}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(toInspect);
        String result = toInspect;
        while (matcher.find()) {
            String token = matcher.group();     // Ex: ${fizz}
            String tokenKey = matcher.group(1); // Ex: fizz
            String replacementValue;
            if (tokensMap.containsKey(tokenKey)) {
                replacementValue = tokensMap.get(tokenKey);
            } else {
                continue;
            }
            result = result.replaceFirst(Pattern.quote(token), replacementValue);
        }
        return result;
    }

    public static JsonObject readInputPart(Logger logger, Part part) throws GesvitaException {
        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(part.getInputStream()));
            boolean flag = false;
            for (String s; (s = reader.readLine()) != null; ) {
                sb.append(s);
            }
            JsonParser parser = new JsonParser();
            JsonElement resp = parser.parse(sb.toString());
            if (resp instanceof JsonObject) {
                return (JsonObject) resp;
            } else if (resp instanceof JsonNull) {
                return new JsonObject();
            } else {
                return new JsonObject();
            }
        } catch (MalformedJsonException ex) {
            logger.error("Ha ocurrido un error de tipo MalformedJsonException. ", ex.fillInStackTrace());
            // out.print("{\"resultadoEjecucion\":\"NOK\",\"mensaje\":\"Ocurrio un error\",\"mensajeTec\":\"" + ex.getClass().toString() + ": " + ex.getMessage() + "\"}");
            throw new GesvitaException("MalformedJsonException in readInputPart()");
        } catch (NullPointerException e1) {
            logger.error("Ha ocurrido un error de tipo NullPointer. ", e1.fillInStackTrace());
            throw new GesvitaException("NullPointerException in readInputPart()");
        } catch (IllegalArgumentException e2) {
            logger.error("Ha ocurrido un error de tipo IllegalArgument. ", e2.fillInStackTrace());
            throw new GesvitaException("IllegalArgumentException in readInputPart()");
        } catch (IndexOutOfBoundsException e3) {
            logger.error("Ha ocurrido un error de tipo IndexOutOfBounds. ", e3.fillInStackTrace());
            // out.print("{\"resultadoEjecucion\":\"NOK\",\"mensaje\":\"Ocurrio un error\",\"mensajeTec\":\"" + e3.getClass().toString() + ": " + e3.getMessage() + "\"}");
            throw new GesvitaException("IndexOutOfBoundsException in readInputPart()");
        } catch (IOException ex) {
            logger.error("Ha ocurrido un error de tipo IOException", ex.fillInStackTrace());
            throw new GesvitaException("IOException in readInputPart()");
//        } catch (Exception ex) {
//            logger.error("Ha ocurrido un error" ,ex.fillInStackTrace());
//            throw new GesvitaException("Exception in readInput()" );
        }
    }

}
