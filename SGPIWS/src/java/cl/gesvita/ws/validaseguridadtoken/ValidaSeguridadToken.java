package cl.gesvita.ws.validaseguridadtoken;

import cl.gesvita.ws.obtener.exception.GesvitaException;
import cl.gesvita.ws.obtener.lib.ObtenerLib;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.Properties;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.log4j.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.naming.*;
import javax.naming.directory.*;
/**
 * Servlet implementation class ValidaSeguridadToken
 */
@WebServlet("/WSSValidaSeguridadToken/ValidaSeguridadToken")
public class ValidaSeguridadToken extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(ValidaSeguridadToken.class);


    /**
     * @see HttpServlet#HttpServlet()
     */
    public ValidaSeguridadToken() {
        super();
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        DataSource datasource = null;
        Connection connection = null;
        JsonObject jsonObj;
        response.setHeader("access-control-allow-origin", "*");
        String etapa ="";
        JsonObject jsonLdap;
        
        try {
            // Inicializar Log4J
            etapa = "Inicializar logs";
            ObtenerLib.setLogParam(this.getClass());
            // Obtener Data Source
            etapa = "Obtener DS";
            datasource = ObtenerLib.getDataSource(logger);
            // Leer la entrada
            etapa = "parseo entrada";
            jsonObj = ObtenerLib.readInput(logger, request);
            // Leer el archivo de parámetros
            etapa = "Validar usuario";
            jsonLdap = leerParametrosLDAP(jsonObj);
        } catch (GesvitaException ex) {
            logger.error(ex.getMessage());
            // logger.error(ex.fillInStackTrace());
            out.print("{\"resultadoEjecucion\":\"NOK\",\"mensaje\":\"Ocurrio un error\",\"mensajeTec\":\"Error en "+ etapa + "\"}");
            if (datasource != null)
                datasource.close();
            return;
        }
        // Realizar la consulta a BD
        try {
            // Preparar la consulta
            etapa = "Obtener conexion";
            connection = datasource.getConnection();
            
            etapa = "Prepara consulta a BD";
            String[] camposytipos;
            
            String[] camposytipossinpwd = {"ID_USUARIO","+I","CODI_USUARIO","+S","NOMBRE_USUARIO","+S","RUT_USUARIO","+S",
                    "TELEFONO_USUARIO","+S","CELULAR_USUARIO","+S","CORREO_USUARIO","+S","ESTADO_USUARIO","+S","USUARIO","S",
                "NOMBRE","+S","APELLIDO_MATERNO","+S","APELLIDO_PATERNO","+S","RUT","+S","TELEFONO","+S","CELULAR","+S","CORREO",
                "+S","ID_ROL","+I","NOMBRE_ROL","+S","CODI_GRUPO_BASE_ROL","+S","ID_PERFIL","+I","CODI_GRUPO_BASE_PERFIL","+S",
                "NOMBRE_PERFIL","+S","ESTADO","+S","ID_EMPRESA","+I","NOMBRE_EMPRESA","+S","ID_AREA","+I","NOMBRE_AREA","+S",
                "PASSWORD_USUARIO","/S","PASSWORD","/S","OBSERVACION_USUARIO","+S"};
            String[] camposytiposfull = {"ID_USUARIO","+I","CODI_USUARIO","+S","NOMBRE_USUARIO","+S","RUT_USUARIO","+S",
                "TELEFONO_USUARIO","+S","CELULAR_USUARIO","+S","CORREO_USUARIO","+S","ESTADO_USUARIO","+S","USUARIO","S",
                "NOMBRE","+S","APELLIDO_MATERNO","+S","APELLIDO_PATERNO","+S","RUT","+S","TELEFONO","+S","CELULAR","+S","CORREO",
                "+S","ID_ROL","+I","NOMBRE_ROL","+S","CODI_GRUPO_BASE_ROL","+S","ID_PERFIL","+I","CODI_GRUPO_BASE_PERFIL","+S",
                "NOMBRE_PERFIL","+S","ESTADO","+S","ID_EMPRESA","+I","NOMBRE_EMPRESA","+S","ID_AREA","+I","NOMBRE_AREA","+S",
                "PASSWORD_USUARIO","S","PASSWORD","S","OBSERVACION_USUARIO","+S"};

            String metodo = limpieza(leerStringJsonFieldOVacio(jsonLdap,"METHOD"));
            String login = limpieza(leerStringJsonFieldOVacio(jsonLdap,"usuario"));
            String clave = limpieza(leerStringJsonFieldOVacio(jsonLdap,"password"));
            String jsonquerystr= "{\"usuario\":\"" + login + "\",\"password\":\"" + clave + "\"}";
            logger.info("METHOD '" + metodo + "'");
            
            if ("LDAP".equalsIgnoreCase(metodo)) {
                camposytipos = camposytipossinpwd;
                jsonquerystr= "{\"usuario\":\"" + login + "\"}";
            } else if ("BD".equalsIgnoreCase(metodo)) {
                camposytipos = camposytiposfull;
                jsonquerystr= "{\"usuario\":\"" + login + "\",\"password\":\"" + clave + "\"}";
            } else {  // TEST
                logger.info("Opción Test '" + clave + "'");
                if (!"123".equals(clave)) {
                    String salidaNOK = "{\"resultadoEjecucion\":\"NOK\",\"mensaje\":\"Ocurrio un error\",\"mensajeTec\":\"Usuario no existe o clave invalida\"}";
                    out.print(salidaNOK);
                    return;
                }
                logger.info("Clave OK");
                camposytipos = camposytipossinpwd;
                jsonquerystr= "{\"usuario\":\"" + login + "\"}";
                logger.info("Consulta : " + jsonquerystr);
            }
            JsonParser parser = new JsonParser();
            JsonObject jsonquery = (JsonObject) parser.parse(jsonquerystr);
            // Llamada a la BD
            etapa = "llamada a BD";
            String json = ObtenerLib.getDefaultObtenerResoultSetExt(logger, connection,
                    camposytipos, "VW_SEGURIDAD_USUARIO" , jsonquery);
            // Analisis respuesta a BD
            etapa = "Analizar respuesta de consulta de usuario a BD";
            JsonObject jsonout = (JsonObject) parser.parse(json);
            // Revisar que venga un primer registro y que su id != 0
            boolean hay_match_en_bd_del_usuario = false;
            JsonObject jsonout2=null;
            if (jsonout.has("1") && (jsonout.get("1") instanceof JsonObject)) {
                jsonout2 = (JsonObject) jsonout.get("1");
                if (jsonout2.has("ID_USUARIO") ) {
                    String Str_id_usuario = limpieza(leerStringJsonFieldOVacio(jsonout2,"ID_USUARIO"));
                    if (!("".equals(Str_id_usuario) || "0".equals(Str_id_usuario))) {
                        hay_match_en_bd_del_usuario = true;
                    }
                }
            }
            etapa = "Obtener Token";
            if (hay_match_en_bd_del_usuario) {
                CallableStatement stmt = connection.prepareCall("{call PKG_MONITOR_UTILITIES.GENERATESESSIONTOKEN(?,?,?,?)}");
                stmt.setFloat(1, Float.parseFloat(readFieldString(jsonout2,"ID_USUARIO")));
                stmt.registerOutParameter(2, Types.VARCHAR);
                stmt.registerOutParameter(3, Types.NUMERIC);
                stmt.registerOutParameter(4, Types.VARCHAR);
                stmt.execute();
                
                int out_codigo = stmt.getInt(3);
                if (out_codigo == 0) {
                    jsonout.addProperty("TOKEN", stmt.getString(2));
                } else {
                    jsonout.addProperty("resultadoEjecucion", "NOK");
                    logger.error("Error durante la consulta a la base de datos. " + stmt.getString(4));
                    jsonout.addProperty("mensaje", "No se pudo generar la sesión");
                    out.print(jsonout.toString());
                    return;
                }
            }            
            // Imprimir la salida
            etapa = "agregar ok a respuesta";
            
            if ("LDAP".equalsIgnoreCase(metodo)) {
                if (hay_match_en_bd_del_usuario) {
                    jsonout.addProperty("resultadoEjecucion", "OK");
                } else {
                    jsonout.addProperty("resultadoEjecucion", "WARN");
                    jsonout.addProperty("mensaje", "Usuario no ingresado en sistema");
                }
            } else {
                if (hay_match_en_bd_del_usuario) {
                    jsonout.addProperty("resultadoEjecucion", "OK");
                } else {
                    jsonout.addProperty("resultadoEjecucion", "NOK");
                    jsonout.addProperty("mensaje", "Usuario o contraseña no valido");
                }
            }
            out.print(jsonout.toString());
        } catch (SQLException e) {
            logger.error("Error durante la consulta a la base de datos. ",e.fillInStackTrace());
            out.print("{\"resultadoEjecucion\":\"NOK\",\"mensaje\":\"Ocurrio un error\",\"mensajeTec\":\"SQLException\"}");
        } catch (GesvitaException ex) {
            logger.error("Error de en etapa ("+ etapa + ") : " + ex.getMessage());
            String salidaNOK = "{\"resultadoEjecucion\":\"NOK\",\"mensaje\":\"Ocurrio un error\",\"mensajeTec\":\"Error en consulta a BD\"}";
            out.print(salidaNOK);
        } finally {
            try {
                if (connection!= null && !connection.isClosed()){
                    connection.close();
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

    private JsonObject leerParametrosLDAP(JsonObject jsonObj) throws GesvitaException{
        String archivoPro;
        
        String formatoemail = "^[A-Za-z0-9\\._%\\-]+@[A-Za-z0-9\\._%\\-]+\\.[A-Za-z]{2,4}$";
        String formatopwd = "^[^\'\"]{8,}$";
        String formatologinbd = "^[A-Za-z0-9_\\-\\.@]{6,}$";
        String formatopwdbd = "^[^\'\"]{3,}$";

        // Parametros de consulta
        String username = "";
        String userPwd = "";
        
        Properties propFile = new Properties();
        try {
            archivoPro = System.getProperty("catalina.base") + "/webapps/propiedades/login.properties";
            
            // Si el archivo no existe ??? falta código
            
            propFile.load(new FileInputStream(archivoPro));
        } catch (FileNotFoundException e) {
            logger.error("Error FileNotFoundException", e.fillInStackTrace());
            throw new GesvitaException("FileNotFoundException in leerParametrosLDAP()");
        } catch (IOException ex) {
            logger.error("Error IOException", ex.fillInStackTrace());
            throw new GesvitaException("IOException in leerParametrosLDAP()");
        }
        
        JsonObject outjson = new JsonObject();
        
        String method = propFile.getProperty("METHOD");   // LDAP , BD
        if ("LDAP".equalsIgnoreCase(method)) {
            username = leerStringJsonFieldOblig(jsonObj,"usuario",formatoemail);
            userPwd = leerStringJsonFieldOblig(jsonObj,"password",formatopwd);

            String base = propFile.getProperty("LDAP.SECURITY_GROUP");   // "OU=Users,OU=Central,OU=Sodimac,OU=CHILE,DC=Falabella,DC=com";
            String identifyingAttribute = propFile.getProperty("LDAP.SECURITY_PROPID");  // "userPrincipalName";
            String keystorePath = propFile.getProperty("LDAP.SECURITY_KEYFILE");  // "C:/Users/Felipe/Documents/NetBeansProjects/CAFalabella.cer";
            String serverURL = propFile.getProperty("LDAP.URL_PROVIDER");  // "ldap://appdc.falabella.cl:3268";
            String adminUser = propFile.getProperty("LDAP.SECURITY_USER");  // "CN=SGPILdap,OU=SGPI Sodimac,OU=APP Corporativas,DC=Falabella,DC=com";
            String adminPwd = propFile.getProperty("LDAP.SECURITY_PWD");  // "xxxxxxxxx";
            
            // Varibles de Salida
            String distinguishedName = "";
            String name_propio = "";
            String apellido = "";
            boolean authenticated = false;
            
            try {
                // (1) Connect to LDAP server
                System.setProperty("javax.net.ssl.keyStore", keystorePath);
                System.setProperty("javax.net.ssl.keyStorePassword", "changeit");
                Properties props = new Properties();
                props.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
                props.put(Context.PROVIDER_URL, serverURL);
                props.put(Context.SECURITY_PRINCIPAL, adminUser);
                props.put(Context.SECURITY_CREDENTIALS, adminPwd);
                // (2) Authenticate with a service user of whom we know the DN and credentials
                InitialDirContext context = new InitialDirContext(props);
                // (3) Search for the user you want to authenticate, search him with some attribute (for example sAMAccountName)
                String[] attributeFilter = new String[] { identifyingAttribute , "sn" , "givenName"};
                SearchControls sc = new SearchControls();
                sc.setReturningAttributes(attributeFilter);
                sc.setSearchScope(SearchControls.SUBTREE_SCOPE);

                // use a search filter to find only the user we want to authenticate
                String searchFilter = "(" + identifyingAttribute + "=" + username + ")";
                NamingEnumeration<SearchResult> results = context.search(base, searchFilter, sc);

                if (results.hasMore()) {
                    // (4) Get the DN of the user we found
                    SearchResult result = results.next();
                    distinguishedName = result.getNameInNamespace();
                    name_propio = getStringAttributeLdapUser(result,"givenName");
                    apellido = getStringAttributeLdapUser(result,"sn");
                }
                if ( ! "".equals(distinguishedName)) {
                    // (5) Open another connection to the LDAP server with the found DN and the password
                    Properties propsUsr = new Properties();
                    propsUsr.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
                    propsUsr.put(Context.PROVIDER_URL, serverURL);
                    propsUsr.put(Context.SECURITY_PRINCIPAL, distinguishedName);
                    propsUsr.put(Context.SECURITY_CREDENTIALS, userPwd);
                    // (6) If the user is found and authentication works, you are fine
                    try {
                        InitialDirContext contextUsr = new InitialDirContext(propsUsr);
                        authenticated = true;  // AUTENTICATION OK
                    } catch (NamingException ex ){
                        authenticated = false; // AUTENTICATION FAILED
                    }
                } else {
                    authenticated = false; // USER NOT FOUND
                }
            } catch (NamingException ex) {
                System.err.println(ex.fillInStackTrace());
                logger.error("Problemas en la conexión a LDAP o con el usuario administrador");
            }
            outjson.addProperty("METHOD", "LDAP");
            if (authenticated) {
                outjson.addProperty("AUTENTICATHED", "OK");
                outjson.addProperty("NOMBRES", name_propio);
                outjson.addProperty("APELLIDOS", apellido);
            } else {
                outjson.addProperty("AUTENTICATHED", "NOK");
            }
            return outjson;
        } else if ("TEST".equalsIgnoreCase(method)) {
            username = leerStringJsonFieldOblig(jsonObj,"usuario",formatologinbd);
            userPwd = leerStringJsonFieldOblig(jsonObj,"password",formatopwdbd);
            outjson.addProperty("METHOD", "TEST");
            outjson.addProperty("usuario", username);
            outjson.addProperty("password", userPwd);
//        } else if ("BD".equalsIgnoreCase(method)) {
//            username = leerStringJsonFieldOblig(jsonObj,"usuario",formatologinbd);
//            userPwd = leerStringJsonFieldOblig(jsonObj,"password",formatopwdbd);
//            outjson.addProperty("METHOD", "BD");
//            outjson.addProperty("usuario", username);
//            outjson.addProperty("password", userPwd);
        } else {
            username = leerStringJsonFieldOblig(jsonObj,"usuario",formatologinbd);
            userPwd = leerStringJsonFieldOblig(jsonObj,"password",formatopwdbd);
            outjson.addProperty("METHOD", "BD");
            outjson.addProperty("usuario", username);
            outjson.addProperty("password", userPwd);
        }
        return outjson;
    }


    private String leerStringJsonFieldOVacio(JsonObject input, String field) {
        JsonElement valueElem = input.get(field);
        if ( valueElem != null ) {
            if (valueElem instanceof JsonArray) {
                JsonArray value2 = (JsonArray) valueElem;
                return value2.toString();
            } else if (valueElem instanceof JsonObject) {
                JsonObject value2 = (JsonObject) valueElem;
                return value2.toString();
            } else {
                return limpieza(valueElem.toString());
            }
        } else {
            return "";
        }
    }
    
    private String leerStringJsonFieldOblig(JsonObject input, String field, String formato)
            throws GesvitaException {
        JsonElement valueElem = input.get(field);
        if ( valueElem != null ) {
            if (valueElem instanceof JsonArray) {
                JsonArray value2 = (JsonArray) valueElem;
                return value2.toString();
            } else if (valueElem instanceof JsonObject) {
                JsonObject value2 = (JsonObject) valueElem;
                return value2.toString();
            } else {
                String data = limpieza(valueElem.toString());
                
                if (!cumpleFormato(data,formato)) {
                    logger.info("Data que no cumple :'" + data + "' , formato : '"+ formato + "'");
                    throw new GesvitaException("Campo: '" + field + "' no cumple formato");
                }
                return data;
            }
        } else {
            throw new GesvitaException("Campo: '" + field + "' no encontrado");
        }
    }
    
    private boolean cumpleFormato(String val, String regex_formato) {
        Pattern pattern_field = Pattern.compile(regex_formato);
        Matcher m = pattern_field.matcher(val);
        return m.matches();
    }
    
    public String getStringAttributeLdapUser(SearchResult result,String field) {
        String ret = "";
        if (result.getAttributes().size() >0) {
            try {
                ret = (String) result.getAttributes().get(field).get(0);
            } catch (NamingException ex) { }
        }
            
        return ret;
    }
    
    private String limpieza(String in) {
        String val = "" + in;
        while (true) {
            if ((val.startsWith("\\") && val.endsWith("\\"))
                || (val.startsWith("\"") && val.endsWith("\""))
                || (val.startsWith("'") && val.endsWith("'"))) {
                val = val.substring(1, val.length() - 1);
            } else if ((val.startsWith("\\\"") && val.endsWith("\\\""))){
                val = val.substring(2, val.length() - 2);
            } else break;
        }
        return val;

    }

    private String readFieldString(JsonObject jsonObj, String field) throws GesvitaException {
        String fieldlow = field.toLowerCase();
        if (!jsonObj.has(fieldlow))
            throw new GesvitaException("Expected field: '" + field + "' is not present in the input");
        JsonElement value = jsonObj.get(fieldlow);
        String salida = value.toString();
        if (!(value instanceof JsonArray) && !(value instanceof JsonObject)) {
            if (salida.startsWith("\"") && salida.endsWith("\""))
                salida = salida.substring(1, salida.length()-1);
            else if (salida.startsWith("'") && salida.endsWith("'"))
                salida = salida.substring(1, salida.length()-1);
        }
        return salida;
    }

}
