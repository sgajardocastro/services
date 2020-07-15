package cl.gesvita.ws.subirpresupuestocc;

import cl.gesvita.util.ClobUtil;
import cl.gesvita.ws.subircapex.bean.EntradaCapex;
import cl.gesvita.ws.obtener.exception.GesvitaException;
import cl.gesvita.ws.obtener.lib.ObtenerLib;
// import cl.gesvita.ws.subircapex.bean.TasksAndWarnings;
import cl.gesvita.ws.subircapex.bean.Warning;
import cl.gesvita.ws.subirpresupuestocc.bean.Columna;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.log4j.Logger;

/**
 * Servlet implementation class SubirPresupuestoCC
 */
@WebServlet("/WSSubirPresupuestoCC/SubirPresupuestoCC")
public class SubirPresupuestoCC extends HttpServlet {

    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(SubirPresupuestoCC.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public SubirPresupuestoCC() {
        super();
    }

    /**
     * @param request
     * @param response
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     * response)
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


//        String CAMPOSDBPPLES[] = {"id","partida","presupuesto","gasto real","saldo","porcentaje"};
//        String CABECERASPPLES[] = {"CC","Año","Item","Número OC","Estado Aprobación","Monto CLP","Fecha Creeación",
//                                "Comprador", "Proveedor", "Cuenta", "Descripción"};
//        String CAMPOSDBFILA[] = {"id","partida","presupuesto","gasto real","saldo","porcentaje"};
//        
        Columna CAMPOSCOMUNES[] = {
            new Columna("In_CORRELATIVO_CCOSTOCMOV","correlativo","I",null,true),
            new Columna("In_FECHA_CARGA","fecha","D",null,true),
            new Columna("In_ID_PROYECTO","proyecto","I",null,true),
            new Columna("In_NOMBRE_ARCHIVO","archivo","S",null,true),
            new Columna("In_ID_MONEDA","moneda","I","1",false),
            new Columna("In_ID_PRODUCTO_COMPRA","idproducto","S","",false),
            new Columna("In_CANTIDAD_PRODUCTO_COOMPRA","cantidad","I","1",false),
            new Columna("In_ID_PROVEEDOR","idproveedor","I","",false),
            new Columna("In_ID_PROYECTO_ERP","id_proyecto_erp","I","",false),
            new Columna("In_NOMBRE_PROYECTO_ERP","nombre_proyecto_erp","S","",false),
            new Columna("In_modalidad_carga","modalidad_carga","S","APPEND",false),
            new Columna("In_modalidad_carga_last","modalidad_carga","S","APPEND-END",false),
        };

        Columna CABECERASFILA[] = {
            new Columna("In_CENTRO_COSTO","CC","S",null,true),
            new Columna("In_AGNO_CENTRO_COSTO","Año","I",null,false),
            new Columna("In_ITEM","Item","I",null,true),
            new Columna("In_NUMERO_OC","Número OC","I",null,false),
            new Columna("In_ESTADO_APROBACION","Estado Aprobación","S",null,false),
            new Columna("In_MONTO_CCOSTOCMOV","Monto CLP","I",null,true),
            new Columna("In_FECHA_CREACION_CCOSTOCMOV","Fecha Creación","D",null,false),
            new Columna("In_NOMBRE_COMPRADOR","Comprador","S",null,false),
            new Columna("In_NOMBRE_PROVEEDOR","Proveedor","S",null,false),
            new Columna("In_CODIGO_CUENTA","Cuenta","S",null,false),
            new Columna("In_DESCRIPCION_CCOSTOCMOV","Descripción","S",null,true)};


//{"CC":"0P011","Año":"2018","Item":"1","Número OC":"609977",
//"Estado Aprobación":"Aprobado","Monto CLP":"810107",
//"Fecha Creación":"8/17/18","Comprador":"SOTO ALVAREZ, FRESIA DEL ROSARIO",
//"Proveedor":"DIMENSION DATA CHILE S.A.",
//"Cuenta":"201-130809801-0P011-0000-000-0000",
//"Descripción":"Switch Cisco HC Rancagua Proy Retiro En Tienda  CC 0P011 X US$1.216,14.-"}

        
        int i, len;
        Map<String,Columna> mapkeys = new HashMap<>();
        for (i =0, len = CABECERASFILA.length; i< len ; i++) {
            mapkeys.put(CABECERASFILA[i].getCampodb(), CABECERASFILA[i]);
        }
        for (i =0, len = CAMPOSCOMUNES.length ; i< len ; i++) {
            mapkeys.put(CAMPOSCOMUNES[i].getCampodb(), CAMPOSCOMUNES[i]);
        }

        Pattern pattern_num;
        Pattern pattern_entero;
        String regex_num = "^[\\d]+\\.[\\d]+$";
        pattern_num = Pattern.compile(regex_num);
        String regex_entero = "^[\\d\\,]+$";
        pattern_entero = Pattern.compile(regex_entero);

        Connection conn = null;

        Map<String, Object> dataRetornar = new HashMap<>();
        Gson gson = new Gson();

        response.setContentType("application/json");

        String etapa = "Obtener Conexión";
        PrintWriter out = response.getWriter();
        DataSource datasource;
        JsonObject jsonObj;

        try {
            // Inicializar Log4J
            ObtenerLib.setLogParam(this.getClass());
            // Obtener Data Source
            datasource = ObtenerLib.getDataSource(logger);
            // BYSECURITY logger.info("Datasource obtenido");
        } catch (GesvitaException ex) {
            logger.error(ex.fillInStackTrace());
            dataRetornar.put("out_codigo", 9999);
            dataRetornar.put("out_mensaje", "Error en conexión a BD");
            sendData(response, out, gson, dataRetornar);
            return;
        }
        try {
            etapa = "Leer entrada request";
            // Leer la entrada excel
            jsonObj = ObtenerLib.readInput(logger, request);
            // BYSECURITY logger.info("entrada leida");
            
            etapa = "Leer parametros gral.";
            // Leer parametros generales
            JsonObject paramcomunJson = LeeParamComun(jsonObj);
            // Extraer JsonObject correspondiente a excel
            etapa = "Extraer JSON de excel";
            JsonElement arrayJsonArch  = jsonObj.get("in_content");
            JsonArray jsonObj_arch;

            if (arrayJsonArch != null && arrayJsonArch instanceof JsonArray) {
                jsonObj_arch = (JsonArray)arrayJsonArch;
            } else {
                throw new GesvitaException("No se seleccionó archivo excel o el formato no corresponde");
            }
            etapa = "Decodificar JSON de excel";
            JsonArray jsonObjExcel = decodificarColumnasExcel(logger,jsonObj_arch,
                    CABECERASFILA, pattern_num , pattern_entero);
            
            etapa = "generar conexion a BD";
            conn = datasource.getConnection();
            
            etapa = "generar ID de BD";
            paramcomunJson = GenerarFechaCarga(logger, conn, paramcomunJson);
            
            etapa = "Carga en BD";
            dataRetornar = guardarEnBDExcel(logger,conn , mapkeys, paramcomunJson,jsonObjExcel);
            sendData(response, out, gson, dataRetornar);
        } catch (SQLException e) {
            logger.error(etapa);
            logger.error(e.fillInStackTrace());
            dataRetornar.put("resultadoEjecucion","NOK");
            dataRetornar.put("out_codigo", 9998);
            dataRetornar.put("out_mensaje", "SQLException");
            sendData(response, out, gson, dataRetornar);
        } catch (GesvitaException ex) {
            logger.error(etapa);
            logger.error(ex.getMessage());
            dataRetornar.put("resultadoEjecucion","NOK");
            dataRetornar.put("out_codigo", 9999);
            dataRetornar.put("out_mensaje", ex.getMessage());
            sendData(response, out, gson, dataRetornar);
        } catch (Exception e) {
            logger.error(etapa);
            logger.error(e.fillInStackTrace());
            dataRetornar.put("resultadoEjecucion","NOK");
            dataRetornar.put("out_codigo", 9997);
            dataRetornar.put("out_mensaje", "Exception");
            sendData(response, out, gson, dataRetornar);
        } finally {
            if (datasource != null) {
                try {
                    datasource.close();
                } catch (Exception e) {
                    logger.error(e.fillInStackTrace());
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    logger.error(e.fillInStackTrace());
                }
            }
        }
    }

    private void sendData(HttpServletResponse response, PrintWriter out, Gson gson, Map<String, Object> dataRetornar) {
        String json = gson.toJson(dataRetornar);
        response.setHeader("access-control-allow-origin", "*");
        out.print(json);
    }


//
//    private Map<Double,JsonObject> generarIndiceID(Logger logger, JsonElement jsonObj) {
//        Map<Double,JsonObject> map = new HashMap();
//        if (jsonObj instanceof JsonArray) {
//            JsonArray arr;
//            arr = (JsonArray) jsonObj;
//            for (JsonElement fila:arr) {
//                if (fila instanceof JsonObject) {
//                    JsonObject filaObj = (JsonObject) fila;
//                    Double idfila = leeDoubleFromJsonObject("id",filaObj);
//
//                    if (idfila != 0D && !map.containsKey(idfila)) {
//                        map.put(idfila,filaObj);
//                    }
//                }
//            }
//        }
//        return map;
//    }

    // Aquellos que no se puede leer el campo quedan como 0.
    private Double leeDoubleFromJsonObject(String id, JsonObject jsonObj) {
        if (!jsonObj.has(id))
            return 0D;
        String data = jsonObj.get(id).toString();
        try {
            return Double.parseDouble(data);
        } catch (NumberFormatException ex) {
            return 0D;
            
        }
    }

//    private int actualizarValores(Logger logger, JsonElement jsonObjDB, Map<Double,JsonObject> mapExcel,
//            String CABECERAS[], String CABECERASDB[],
//            Pattern pattern_num, Pattern pattern_entero ) {
//        if (jsonObjDB instanceof JsonArray) {
//            JsonArray arr;
//            arr = (JsonArray) jsonObjDB;
//            Set<Double> cjto_keys = mapExcel.keySet();
//            
//            logger.info("Actualizar valores");
//            for (JsonElement fila:arr) {
//                // logger.info(fila.toString());
//                if (fila instanceof JsonObject) {
//                    JsonObject filaObj = (JsonObject) fila;
//                    Double idfila = leeDoubleFromJsonObject("id",filaObj);
//
//                    if (idfila != 0D && mapExcel.containsKey(idfila)) {
//                        JsonObject filaExcel = mapExcel.get(idfila);
//                        ActualizaCamposFila(filaObj , filaExcel);
//                        cjto_keys.remove(idfila);
//                    }
//                }
//            }
//            // logger.info("Agregar filas no encontradas");
//            // logger.info("Keys: " + cjto_keys.toString());
//            for ( Double k : cjto_keys) {
//                // logger.info("K = " + k);
//                if (k!= 0D) {
//                    // logger.info("lee " + k);
//                    JsonElement elem = mapExcel.get(k);
//                    // logger.info(elem.toString());
//                    if ((elem == null) || !(elem instanceof JsonObject)){
//                        continue;
//                    }
//                    // logger.info("Clonar !!!");
//                    JsonObject o = cloneFilaMasAsterisco(CABECERASDB,(JsonObject) elem , pattern_num, pattern_entero);
//                    if (o != null){
//                        // logger.info("añadir : " + o.toString());
//                        arr.add(o);
//                    }
//                    // logger.info("Nulo");
//                }
//            }
//        }
//        return 0;
//        
//    }

//    private void ActualizaCamposFila(JsonObject jsonObjDB, JsonObject filaExcel) {
//        
//        String CABECERASDB[] = {"presupuesto","gasto_real","saldo"};
//        for (String field : CABECERASDB){
//            ActualizaCampoIntegerFila(jsonObjDB,filaExcel,field);
//        }
//    }
    
//    private void ActualizaCampoIntegerFila(JsonObject jsonObjDB, JsonObject filaExcel,String field) {
//        if (filaExcel.has(field)) {
//            Long valorL = leeLongFromJsonObject(field,filaExcel);
//            if (jsonObjDB.has(field)) {
//                jsonObjDB.remove(field);
//            }
//            jsonObjDB.addProperty(field, valorL);
//        }
//    }

//    private Long leeLongFromJsonObject(String field, JsonObject jsonObj) {
//        String data = jsonObj.get(field).getAsString();
//        try {
//            return Long.parseLong(data);
//        } catch (NumberFormatException ex) {
//            return 0L;
//        }
//    }
    
    // Leer el excel y transformarlo en un JSON entendible
    private JsonArray decodificarColumnasExcel(Logger logger, JsonArray arr,Columna CABECERASFILA[],
            Pattern pattern_num, Pattern pattern_entero ) throws GesvitaException {
        int i,len;
        JsonArray salida = new JsonArray();
        Pattern pattern_percent, current_patern;
        String regex_percent = "^[\\d]+\\.[\\d]{2}\\%$";
        pattern_percent = Pattern.compile(regex_percent);
        String regex_date = "^([0][1-9]|[12][0-9]|3[01])(\\/|-)([0][1-9]|[1][0-2])\\2(\\d{2,4})$";
        Pattern pattern_date = Pattern.compile(regex_date);
        int k=1;
        for (JsonElement fila:arr) {
            
            if (fila instanceof JsonObject) {
                JsonObject filaObj = (JsonObject) fila;
                Set<String> keys = filaObj.keySet();
                
                JsonObject filaOut = new JsonObject();
                
                filaOut.addProperty("In_CORRELATIVO_CCOSTOCMOV", "" + k);
                k++;
                
                for (i= 0, len = CABECERASFILA.length; i< len ;i++) {
                    String valor;
                    String campodb = CABECERASFILA[i].getCampodb();
                    if ( filaObj.has(CABECERASFILA[i].getNombre()) ) {
                        valor = filaObj.get(CABECERASFILA[i].getNombre()).getAsString();
                        
                        if (valor.startsWith("\"") && valor.endsWith("\""))
                            valor = valor.substring(1,valor.length()-1);
                        String tipo = CABECERASFILA[i].getTipo().trim();
                        
                        switch (tipo) {
                            case "I":
                                current_patern = pattern_entero;
                                break;
                            case "F":
                                current_patern = pattern_num;
                                break;
                            case "D":    
                                current_patern = pattern_date;
                                break;
                            default:
                                current_patern = null;
                                break;
                        }
                        if (current_patern != null) {
                            if (!current_patern.matcher(valor).matches()) {
                                valor = CABECERASFILA[i].getDefaultValue();
                            }
                        }
                    } else {
                        valor = CABECERASFILA[i].getDefaultValue();
                    }
                    filaOut.addProperty(campodb, valor);
                }
                salida.add(filaOut);
                
                

            } else {
                throw new GesvitaException("No se seleccionó archivo excel o el formato no corresponde (2)");
            }
        }
        return salida;
    }
    
    private String recuperaValor (JsonObject filaObj, Columna campo, Pattern patern) {
        String valor = null;
        String id = campo.getNombre();
        String tipo = campo.getTipo();
        String omision = campo.getDefaultValue();
//        if (filaObj.has(id)) {
//            valor = filaObj.get(id);
//            if (tipo.equals("I") ) {
//                if ()
//                
//            }
//            
//            
//        } else {
//            valor = omision;
//        }
        
        return null;
    
    
    }
    
//    private JsonObject processFila(String keycabeceras[], String keycabecerasdb[], JsonObject fila, Pattern pattern_num, Pattern pattern_entero){
//        JsonObject salida = new JsonObject();
//        
//        salida.addProperty(keycabecerasdb[0], leeNumeroFilaCapex(fila, keycabeceras[0], pattern_num));
//        salida.addProperty(keycabecerasdb[1], leeCadenaFilaCapex(fila, keycabeceras[1]));
//
//        salida.addProperty(keycabecerasdb[2], leeEnteroFilaCapex(fila, keycabeceras[2], pattern_entero));
//        salida.addProperty(keycabecerasdb[3], leeEnteroFilaCapex(fila, keycabeceras[3], pattern_entero));
//        salida.addProperty(keycabecerasdb[4], leeEnteroFilaCapex(fila, keycabeceras[4], pattern_entero));
//        // salida.addProperty(keycabeceras[5],   leePorcentajeFilaCapex(fila, keycabeceras[5]));
//        return salida;
//    }
    
//    private JsonObject cloneFilaMasAsterisco(String keycabeceras[], JsonObject fila, Pattern pattern_num, Pattern pattern_entero){
//        JsonObject salida = new JsonObject();
//        double valD;
//        String valS;
//        valD = leeNumeroFilaCapex(fila, keycabeceras[0], pattern_num);
//        // logger.info("D");
//        if (valD != 0D)
//            salida.addProperty(keycabeceras[0], valD );
//        // logger.info("S");
//        valS = leeCadenaFilaCapex(fila, keycabeceras[1]);
//        // logger.info("S1");
//        if (valS != null)
//            salida.addProperty(keycabeceras[1], "* " + valS);
//        else
//            return null;
//        // logger.info("L");
//        salida.addProperty(keycabeceras[2], leeEnteroFilaCapex(fila, keycabeceras[2], pattern_entero));
//        salida.addProperty(keycabeceras[3], leeEnteroFilaCapex(fila, keycabeceras[3], pattern_entero));
//        salida.addProperty(keycabeceras[4], leeEnteroFilaCapex(fila, keycabeceras[4], pattern_entero));
//        return salida;
//    }
    
//    private double leeNumeroFilaCapex(JsonObject fila, String field, Pattern pattern_num) {
//        if (fila.has(field)) {
//            String val = fila.get(field).getAsString().trim();
//            Matcher m = pattern_num.matcher(val);
//            if (m.find()) {
//                return Double.parseDouble(val);
//            } else {
//                return 0D;
//            }
//        } else {
//            return 0D;
//        }
//    }

//    private String leeCadenaFilaCapex(JsonObject fila, String field) {
//        if (fila.has(field)) {
//            String val = fila.get(field).toString();
//            if (val == null)
//                return null;
//            val = val.trim();
//            if (val.equalsIgnoreCase("Null"))
//                return null;
//            for (int i=0;i<5;i++) {
//                if ((val.startsWith("\\") && val.endsWith("\\"))
//                    || (val.startsWith("\"") && val.endsWith("\""))
//                    || (val.startsWith("'") && val.endsWith("'"))) {
//                    val = val.substring(1, val.length() - 1);
//                } else if ((val.startsWith("\\\"") && val.endsWith("\\\""))){
//                    val = val.substring(2, val.length() - 2);
//                } else break;
//            }
//            return val;
//        } else {
//            return null;
//        }
//    }

//    private long leeEnteroFilaCapex(JsonObject fila, String field, Pattern pattern_entero) {
//        if (fila.has(field)) {
//            String val = fila.get(field).getAsString().trim();
//            Matcher m = pattern_entero.matcher(val);
//            if (m.find()) {
//                return Long.parseLong(val.replaceAll(",", ""));
//            } else {
//                return 0L;
//            }
//        } else {
//            return 0L;
//        }
//    }

//    private JsonElement leerJsonDeDB(Logger logger, Connection conn, JsonObject jsonObj) throws GesvitaException , SQLException {
//        String[] camposytipos = {"ID_PROYECTO","I","ID_TAREA_HIJO","I","DATA_EXTENDIDA_HIJO","C"};
//        JsonObject json_consulta = new JsonObject();
//        if (jsonObj.has("in_id_proyecto") && jsonObj.has("in_id_tarea") && jsonObj.has("in_id_usuario") ) {
//            json_consulta.addProperty("id_proyecto", limpiaNum(jsonObj.get("in_id_proyecto").toString()));
//            json_consulta.addProperty("id_tarea_hijo", limpiaNum(jsonObj.get("in_id_tarea").toString()));
//        } else {
//            throw new GesvitaException("Los campos in_id_proyecto, in_id_usuario e in_id_tarea son obligatorios");
//        }
//        // BYSECURITY logger.info ("Entrada Consulta" + json_consulta.toString());
//        // VW_PROYECTO_SUBTAREAS
//        
//        
//        String json;
//        // Alternativa 1: Consulta a BD sin uso de biblioteca
//         json = consultaDB(logger,conn,json_consulta); 
//        // Alternativa 2: Consulta a BD con uso de biblioteca
////        json = ObtenerLib.getDefaultObtenerResoultSetExt(logger, conn,
////                camposytipos, "VW_PROYECTO_SUBTAREAS" , json_consulta ,
////                " ID_PROYECTO, ID_TAREA_HIJO, DATA_EXTENDIDA_HIJO " , "" , null);
//        
//        // json = "[{\"id\":\"1.0\",\"partida\":\"Derechos municipales y permisos\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.0\",\"partida\":\"Proyectos de diseño\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.01\",\"partida\":\"Arquitectura\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.02\",\"partida\":\"Cálculo\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.03\",\"partida\":\"Revisor independiente\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.04\",\"partida\":\"Topografia\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.05\",\"partida\":\"Mecánica de suelos\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.06\",\"partida\":\"Instalaciones eléctricas\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.07\",\"partida\":\"Iluminación\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.08\",\"partida\":\"Sistema de seguridad contra incendios\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.09\",\"partida\":\"Instalaciones de clima\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.1\",\"partida\":\"Pavimentos\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.11\",\"partida\":\"Instalaciones sanitarias\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.12\",\"partida\":\"Impacto vial\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.13\",\"partida\":\"Cargas combustibles\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.14\",\"partida\":\"prevención (CCTV y sistema anti hurto) (Incluido en capex de prevención)\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.15\",\"partida\":\"Copia planos\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.0\",\"partida\":\"Construcción\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.1\",\"partida\":\"Excavaciones masivas y rellenos compactados\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.15\",\"partida\":\"Obra Gruesa\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.151\",\"partida\":\"Trabajos preliminares\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.1511\",\"partida\":\"Demoliciones, desmontajes, retiro escombros\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0.1512\",\"partida\":\"Replanteo topográfico\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.152\",\"partida\":\"Excavaciones menores (fundaciones, estanques, zanjas, rampas)\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.153\",\"partida\":\"Obras de Hormigón\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.154\",\"partida\":\"Estructura metálica\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.155\",\"partida\":\"Cubiertas y revestimientos metálicos\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.156\",\"partida\":\"Obras exteriores en terreno propio\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.157\",\"partida\":\"Instalaciones (Pasadas, obras civiles y soportes de equipos por Constructora)\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.2\",\"partida\":\"Instalaciones sanitarias (incluye gas)\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.25\",\"partida\":\"Terminaciones\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0.0\",\"partida\":\"Adicionales / Imprevistos\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.3\",\"partida\":\"Inspección técnica de Obra\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.35\",\"partida\":\"Obras de impacto vial\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.4\",\"partida\":\"Equipos de elevación\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.4\",\"partida\":\"Instalaciones eléctricas\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.41\",\"partida\":\"Contrato obras eléctricas\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.42\",\"partida\":\"Subestaciones\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.43\",\"partida\":\"Grupo generador\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.44\",\"partida\":\"Inspección técnica eléctrico\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.45\",\"partida\":\"Luminaria\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.5\",\"partida\":\"Instalaciones de Clima\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.51\",\"partida\":\"Contrato obras climatización\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.52\",\"partida\":\"Equipos Rooftops/Chillers\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.53\",\"partida\":\"Equipos Mini split\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.54\",\"partida\":\"Control centralizado\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.55\",\"partida\":\"Instalaciones sistema contra incendios\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"Compras de G. de Proyectos en construcción (No incluye Suministros de equipos de instalaciones de ingeniería)\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"Letreros institucionales en fachada tienda\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"TOTEM\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"Correo neumático bidireccional\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"Docklever\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"Puertas de Boveda\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"Tolva pasa valores\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"Arriendo de contenedores\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"Decapado y abrillantado de piso\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4,0\",\"partida\":\"Habilitación y Equipamiento\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4,1\",\"partida\":\"Gerencia de Proyectos\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1001\",\"partida\":\"Rack de tienda\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1002\",\"partida\":\"Estantería Cantilever\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1003\",\"partida\":\"Estantería Hi-Cube\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1004\",\"partida\":\"Estantería Out-rriger\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1005\",\"partida\":\"Estentería de Góndola\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1006\",\"partida\":\"Paneles perforados\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1007\",\"partida\":\"Entrepaños, planchas trupan\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"Mallas\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1008\",\"partida\":\"Accesorios\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1009\",\"partida\":\"Check Outs\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.101\",\"partida\":\"Portillones y guías\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"Equipamiento mascotas vivas\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1013\",\"partida\":\"Instalaciones electricas exhibiciones\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1015\",\"partida\":\"Caseta caja patio constructor\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1016\",\"partida\":\"Mueble caja patio constructor\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1017\",\"partida\":\"Muebles especiales patio constructor\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1018\",\"partida\":\"Perfilera\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"Mobiliario tienda (oficinas, arriendo herramientas)\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"Equipamiento de cosina\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1021\",\"partida\":\"Caja fuerte tesorería\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1022\",\"partida\":\"Armario de seguridad especial\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1023\",\"partida\":\"Elevador de plataforma\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1024\",\"partida\":\"Grúas electricas\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1025\",\"partida\":\"Máquina dimensionadora de alfombra\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1026\",\"partida\":\"Máquina dimensionadora de linoleos\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1027\",\"partida\":\"Máquina de cables eléctricos\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1028\",\"partida\":\"Trasnspaleta electrica\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1029\",\"partida\":\"Tronzadora de perfiles de acero\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1031\",\"partida\":\"Máquinas dimensionadoras de madera\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1032\",\"partida\":\"Máquina compactadora de cartones\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1033\",\"partida\":\"Máquina de tintometría\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1037\",\"partida\":\"Luminaria Segunda etapa (en Racks)\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.104\",\"partida\":\"Inspección técnica etapa habilitación\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2\",\"partida\":\"Gerencia de administración y adquisiciones\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"Mobiliario de casino\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2001\",\"partida\":\"Sillas, sillones y mesas\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2002\",\"partida\":\"Música ambiental\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2003\",\"partida\":\"Lockers\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2004\",\"partida\":\"Canastos\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2005\",\"partida\":\"Carro con porta bebe\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2006\",\"partida\":\"Carro electrico para minusvalidos\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2007\",\"partida\":\"Carro niño tipo auto\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2008\",\"partida\":\"Carro cliente auto servicio\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2009\",\"partida\":\"Carro de arrastre nacional\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.201\",\"partida\":\"Carros Boston\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2011\",\"partida\":\"Carros placas + tableros\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2012\",\"partida\":\"Carros SP4\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2013\",\"partida\":\"Escaleras de fibra 8 peldaños\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2014\",\"partida\":\"Escaleras tipo avión\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2015\",\"partida\":\"Sillas de ruedas clientes con canasto\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2016\",\"partida\":\"Traspaletas\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2017\",\"partida\":\"Napoleón de piso\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2018\",\"partida\":\"Máquina reensecadora de cemento\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2019\",\"partida\":\"Turnomatic\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.202\",\"partida\":\"Dispensador de tohallas\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2021\",\"partida\":\"Máquina de café\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2022\",\"partida\":\"Máquina de Fax\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2023\",\"partida\":\"Televisores 21\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2024\",\"partida\":\"DVD\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2025\",\"partida\":\"Data Show con telón\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2026\",\"partida\":\"camara fotográfica\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2027\",\"partida\":\"Máquina cortadora de monedas\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2028\",\"partida\":\"Máquina cortadora de billetes\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2029\",\"partida\":\"Máquina trituradora de papel\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.203\",\"partida\":\"Calculadora para arqueo\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2031\",\"partida\":\"Calculador de bolsillo\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2032\",\"partida\":\"Dispensador de papel higienico\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2033\",\"partida\":\"Máquina enzunchadora\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2034\",\"partida\":\"Fundas extensión uñas grúa horquilla\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.3\",\"partida\":\"Gerencia de servicios\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.31\",\"partida\":\"Máquina Ingleteadora y mesón en patio constructor\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.32\",\"partida\":\"Dotación de equipos sala de dimensionado\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.3201\",\"partida\":\"Mesa corte de vidrio\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.3202\",\"partida\":\"Atril Montado en riel\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.3203\",\"partida\":\"Atril Fijo\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.3204\",\"partida\":\"Carro para atriles\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.3205\",\"partida\":\"Riel\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.3206\",\"partida\":\"Carro de acopio\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.3207\",\"partida\":\"Máq. / Pulidora de vidro. (Metral modelo SL)\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.3208\",\"partida\":\"Elementos para cortar vidrio \",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.3209\",\"partida\":\"Máq. de corte circular\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.321\",\"partida\":\"mueble perforadora de bisagras\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.3211\",\"partida\":\"Atriles de acopio\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.33\",\"partida\":\"Dotación arriendo de herramientas\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.4\",\"partida\":\"Gerencia de Prevención (CCTV y Sistema Anti Hurto)\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.5\",\"partida\":\"Gerencia de sistemas\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.6\",\"partida\":\"Gerencia de Diseño de tienda\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.7\",\"partida\":\"Bonos y Viáticos\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"}]";
//        // json= "[{\"ID_PROYECTO\":17995,\"ID_TAREA_HIJO\":261620,\"DATA_EXTENDIDA_HIJO\":[{\"id\":\"1.0\",\"partida\":\"Derechos municipales y permisos\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.0\",\"partida\":\"Proyectos de diseño\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.01\",\"partida\":\"Arquitectura\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.02\",\"partida\":\"Cálculo\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.03\",\"partida\":\"Revisor independiente\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.04\",\"partida\":\"Topografia\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.05\",\"partida\":\"Mecánica de suelos\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.06\",\"partida\":\"Instalaciones eléctricas\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.07\",\"partida\":\"Iluminación\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.08\",\"partida\":\"Sistema de seguridad contra incendios\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.09\",\"partida\":\"Instalaciones de clima\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.1\",\"partida\":\"Pavimentos\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.11\",\"partida\":\"Instalaciones sanitarias\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.12\",\"partida\":\"Impacto vial\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.13\",\"partida\":\"Cargas combustibles\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.14\",\"partida\":\"prevención (CCTV y sistema anti hurto) (Incluido en capex de prevención)\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.15\",\"partida\":\"Copia planos\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.0\",\"partida\":\"Construcción\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.1\",\"partida\":\"Excavaciones masivas y rellenos compactados\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.15\",\"partida\":\"Obra Gruesa\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.151\",\"partida\":\"Trabajos preliminares\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.1511\",\"partida\":\"Demoliciones, desmontajes, retiro escombros\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0.1512\",\"partida\":\"Replanteo topográfico\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.152\",\"partida\":\"Excavaciones menores (fundaciones, estanques, zanjas, rampas)\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.153\",\"partida\":\"Obras de Hormigón\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.154\",\"partida\":\"Estructura metálica\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.155\",\"partida\":\"Cubiertas y revestimientos metálicos\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.156\",\"partida\":\"Obras exteriores en terreno propio\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.157\",\"partida\":\"Instalaciones (Pasadas, obras civiles y soportes de equipos por Constructora)\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.2\",\"partida\":\"Instalaciones sanitarias (incluye gas)\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.25\",\"partida\":\"Terminaciones\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0.0\",\"partida\":\"Adicionales / Imprevistos\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.3\",\"partida\":\"Inspección técnica de Obra\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.35\",\"partida\":\"Obras de impacto vial\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.4\",\"partida\":\"Equipos de elevación\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.4\",\"partida\":\"Instalaciones eléctricas\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.41\",\"partida\":\"Contrato obras eléctricas\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.42\",\"partida\":\"Subestaciones\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.43\",\"partida\":\"Grupo generador\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.44\",\"partida\":\"Inspección técnica eléctrico\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.45\",\"partida\":\"Luminaria\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.5\",\"partida\":\"Instalaciones de Clima\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.51\",\"partida\":\"Contrato obras climatización\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.52\",\"partida\":\"Equipos Rooftops/Chillers\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.53\",\"partida\":\"Equipos Mini split\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.54\",\"partida\":\"Control centralizado\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.55\",\"partida\":\"Instalaciones sistema contra incendios\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"Compras de G. de Proyectos en construcción (No incluye Suministros de equipos de instalaciones de ingeniería)\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"Letreros institucionales en fachada tienda\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"TOTEM\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"Correo neumático bidireccional\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"Docklever\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"Puertas de Boveda\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"Tolva pasa valores\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"Arriendo de contenedores\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"Decapado y abrillantado de piso\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4,0\",\"partida\":\"Habilitación y Equipamiento\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4,1\",\"partida\":\"Gerencia de Proyectos\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1001\",\"partida\":\"Rack de tienda\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1002\",\"partida\":\"Estantería Cantilever\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1003\",\"partida\":\"Estantería Hi-Cube\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1004\",\"partida\":\"Estantería Out-rriger\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1005\",\"partida\":\"Estentería de Góndola\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1006\",\"partida\":\"Paneles perforados\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1007\",\"partida\":\"Entrepaños, planchas trupan\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"Mallas\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1008\",\"partida\":\"Accesorios\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1009\",\"partida\":\"Check Outs\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.101\",\"partida\":\"Portillones y guías\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"Equipamiento mascotas vivas\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1013\",\"partida\":\"Instalaciones electricas exhibiciones\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1015\",\"partida\":\"Caseta caja patio constructor\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1016\",\"partida\":\"Mueble caja patio constructor\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1017\",\"partida\":\"Muebles especiales patio constructor\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1018\",\"partida\":\"Perfilera\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"Mobiliario tienda (oficinas, arriendo herramientas)\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"Equipamiento de cosina\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1021\",\"partida\":\"Caja fuerte tesorería\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1022\",\"partida\":\"Armario de seguridad especial\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1023\",\"partida\":\"Elevador de plataforma\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1024\",\"partida\":\"Grúas electricas\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1025\",\"partida\":\"Máquina dimensionadora de alfombra\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1026\",\"partida\":\"Máquina dimensionadora de linoleos\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1027\",\"partida\":\"Máquina de cables eléctricos\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1028\",\"partida\":\"Trasnspaleta electrica\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1029\",\"partida\":\"Tronzadora de perfiles de acero\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1031\",\"partida\":\"Máquinas dimensionadoras de madera\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1032\",\"partida\":\"Máquina compactadora de cartones\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1033\",\"partida\":\"Máquina de tintometría\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1037\",\"partida\":\"Luminaria Segunda etapa (en Racks)\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.104\",\"partida\":\"Inspección técnica etapa habilitación\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2\",\"partida\":\"Gerencia de administración y adquisiciones\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"Mobiliario de casino\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2001\",\"partida\":\"Sillas, sillones y mesas\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2002\",\"partida\":\"Música ambiental\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2003\",\"partida\":\"Lockers\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2004\",\"partida\":\"Canastos\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2005\",\"partida\":\"Carro con porta bebe\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2006\",\"partida\":\"Carro electrico para minusvalidos\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2007\",\"partida\":\"Carro niño tipo auto\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2008\",\"partida\":\"Carro cliente auto servicio\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2009\",\"partida\":\"Carro de arrastre nacional\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.201\",\"partida\":\"Carros Boston\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2011\",\"partida\":\"Carros placas + tableros\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2012\",\"partida\":\"Carros SP4\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2013\",\"partida\":\"Escaleras de fibra 8 peldaños\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2014\",\"partida\":\"Escaleras tipo avión\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2015\",\"partida\":\"Sillas de ruedas clientes con canasto\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2016\",\"partida\":\"Traspaletas\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2017\",\"partida\":\"Napoleón de piso\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2018\",\"partida\":\"Máquina reensecadora de cemento\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2019\",\"partida\":\"Turnomatic\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.202\",\"partida\":\"Dispensador de tohallas\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2021\",\"partida\":\"Máquina de café\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2022\",\"partida\":\"Máquina de Fax\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2023\",\"partida\":\"Televisores 21\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2024\",\"partida\":\"DVD\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2025\",\"partida\":\"Data Show con telón\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2026\",\"partida\":\"camara fotográfica\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2027\",\"partida\":\"Máquina cortadora de monedas\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2028\",\"partida\":\"Máquina cortadora de billetes\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2029\",\"partida\":\"Máquina trituradora de papel\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.203\",\"partida\":\"Calculadora para arqueo\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2031\",\"partida\":\"Calculador de bolsillo\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2032\",\"partida\":\"Dispensador de papel higienico\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2033\",\"partida\":\"Máquina enzunchadora\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2034\",\"partida\":\"Fundas extensión uñas grúa horquilla\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.3\",\"partida\":\"Gerencia de servicios\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.31\",\"partida\":\"Máquina Ingleteadora y mesón en patio constructor\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.32\",\"partida\":\"Dotación de equipos sala de dimensionado\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.3201\",\"partida\":\"Mesa corte de vidrio\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.3202\",\"partida\":\"Atril Montado en riel\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.3203\",\"partida\":\"Atril Fijo\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.3204\",\"partida\":\"Carro para atriles\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.3205\",\"partida\":\"Riel\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.3206\",\"partida\":\"Carro de acopio\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.3207\",\"partida\":\"Máq. / Pulidora de vidro. (Metral modelo SL)\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.3208\",\"partida\":\"Elementos para cortar vidrio \",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.3209\",\"partida\":\"Máq. de corte circular\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.321\",\"partida\":\"mueble perforadora de bisagras\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.3211\",\"partida\":\"Atriles de acopio\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.33\",\"partida\":\"Dotación arriendo de herramientas\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.4\",\"partida\":\"Gerencia de Prevención (CCTV y Sistema Anti Hurto)\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.5\",\"partida\":\"Gerencia de sistemas\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.6\",\"partida\":\"Gerencia de Diseño de tienda\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.7\",\"partida\":\"Bonos y Viáticos\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"}]}]";
//
//        // BYSECURITY logger.info("ok consulta");
//        
//        // BYSECURITY logger.info("Salida lectura BD: " + json);
//        
//        JsonParser parser = new JsonParser();
//        JsonArray jo = parser.parse(json).getAsJsonArray();
//        JsonObject jo_0 = jo.get(0).getAsJsonObject();
//        return jo_0.get("DATA_EXTENDIDA_HIJO");
//        // return jo_0.getAsJsonArray("DATA_EXTENDIDA_HIJO");
//    }
    
//    private String limpiaNum(String in) {
//        if (in != null && !in.equals("")) {
//            if ( in.startsWith("\\\"") && in.endsWith("\\\"") ) {
//                return in.substring(2, in.length()-2);
//            } else if ( in.startsWith("'") && in.endsWith("'") ) {
//                return in.substring(1, in.length()-1);
//            } else  if ( in.startsWith("\"") && in.endsWith("\"") ) {
//                return in.substring(1, in.length()-1);
//            }
//        }
//        return in;
//    }

//    private Map guardarEnBd(Connection conn, JsonObject jsonObj, JsonElement jsonObjDB) throws SQLException, GesvitaException {
//        CallableStatement stmt;
//        Clob clob;
//        Map<String, Object> dataRetornar = new HashMap<>();
//        
//        stmt = conn.prepareCall("{call PKG_MONITOR_UPDATE.PROYECTO_TAREA_DATAEXT(?,?,?,?,?)}");
//        clob = conn.createClob();
//        stmt.setFloat(1, Float.parseFloat(readFieldString(jsonObj,"in_id_tarea")));
//        stmt.setFloat(2, Float.parseFloat(readFieldString(jsonObj,"in_id_usuario")));
//        clob.setString(1, jsonObjDB.toString());
//        stmt.setClob(3, clob);
//        stmt.registerOutParameter(4, Types.NUMERIC);
//        stmt.registerOutParameter(5, Types.VARCHAR);
//        stmt.execute();
//        dataRetornar.put("out_codigo", stmt.getInt(4));
//        dataRetornar.put("out_mensaje", stmt.getString(5));
//        dataRetornar.put("resultadoEjecucion", "OK");
//        return dataRetornar;
//        // sendData(response, out, dataRetornar);
//    }
    
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
    
    private long readFieldLong(JsonObject jsonObj, String field) throws GesvitaException {
        try {
            return Long.parseLong(readFieldString(jsonObj, field));
        } catch (NumberFormatException e) {
            throw new GesvitaException("Expected field: '" + field + "' is not a number");
        }
    }

//    private String consultaDB(Logger logger, Connection conn, JsonObject json_consulta) throws  SQLException , GesvitaException{
//        PreparedStatement stmt;
//        String query = "SELECT  ID_PROYECTO, ID_TAREA_HIJO, DATA_EXTENDIDA_HIJO  FROM VW_PROYECTO_SUBTAREAS PPLVIEW WHERE ID_PROYECTO = ? AND ID_TAREA_HIJO = ?";
//        stmt = conn.prepareStatement(query);
//        stmt.setLong(1, readFieldLong(json_consulta, "id_proyecto"));
//        stmt.setLong(2, readFieldLong(json_consulta, "id_tarea_hijo"));
//        ResultSet rs = stmt.executeQuery();
//        if (rs.next()) {
//            long p = rs.getLong("ID_PROYECTO");
//            long t = rs.getLong("ID_TAREA_HIJO");
//            String dat = ClobUtil.clobToString(rs.getClob("DATA_EXTENDIDA_HIJO"));
//            
//            JsonParser parser = new JsonParser();
//            JsonElement json_dat = parser.parse(dat);
//            
//            JsonArray json_out = new JsonArray();
//            JsonObject item0 = new JsonObject();
//            item0.addProperty("ID_PROYECTO", p);
//            item0.addProperty("ID_TAREA_HIJO", t);
//            item0.add("DATA_EXTENDIDA_HIJO", json_dat);
//            json_out.add(item0);
//            
//            String salida = json_out.toString();
//            
//            // BYSECURITY logger.info("Salida DB " + salida);
//            
//            return salida;
//        } else{
//            return "";
//        }
//    }

    private JsonObject LeeParamComun(JsonObject jsonObj) throws GesvitaException {

//            new Columna("In_ID_PROYECTO","proyecto","I",null,true),
//            new Columna("In_NOMBRE_ARCHIVO","archivo","S",null,true),
//        "in_archivo":"0P011 20.11.18.xlsx","in_id_tarea":"266634","in_id_proyecto":18059
        JsonObject paramcomun = new JsonObject();
        paramcomun.addProperty("In_NOMBRE_ARCHIVO", readFieldString(jsonObj,"in_archivo") );
        paramcomun.addProperty("In_ID_PROYECTO",readFieldLong(jsonObj,"in_id_proyecto") );
        return paramcomun;
    }

    private JsonObject GenerarFechaCarga(Logger logger, Connection conn, JsonObject paramcomunJson)
            throws GesvitaException, SQLException {
        // In_FECHA_CARGA
        // new Columna("In_FECHA_CARGA","fecha","D",null,true),
        String query = "SELECT TO_CHAR(CURRENT_DATE, 'dd/mm/yyyy') from dual";
        PreparedStatement ps = conn.prepareStatement(query);
        
        ResultSet rs =  ps.executeQuery(); // ps.executeQuery();
        if (rs.next()) {
            paramcomunJson.addProperty("In_FECHA_CARGA",rs.getString(1));
        } else {
            throw new GesvitaException("No se puede recuperar fecha de BD");
        }
        try {
            if (ps != null)
                ps.close();
            if (rs != null)
                rs.close();
        } catch (Exception e) {}
        return paramcomunJson;
        
    }

    private Map<String, Object> guardarEnBDExcel(Logger logger, Connection conn, Map<String,Columna> mapkeys,
            JsonObject paramcomunJson, JsonArray jsonObjExcel) throws SQLException, GesvitaException {
        Map<String, Object> dataRetornar = new HashMap<>();
        int numline = 1;
        long out_id_presupuesto_ccostoresmov = 0L;
        long out_codigo = 0L;
        String out_mensaje = "";
        long lineExcelFaltantes;
        lineExcelFaltantes = jsonObjExcel.size();
        try {
             
            CallableStatement stmt = conn.prepareCall("{call PKG_MONITOR_INSERT.CARGAR_PRESUPUESTO_CCOSTOMOV("
                    + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");
            
            // BYSECURITY logger.info("Parametros comunes:"+ paramcomunJson.toString());
            // Setear campos comunes
            Carga4JsonDate(  stmt, 1, mapkeys, "In_FECHA_CARGA",paramcomunJson, "dd/MM/yyyy");
            Carga4JsonLong(  stmt, 2, mapkeys, "In_ID_PROYECTO",paramcomunJson);
            Carga4JsonString(stmt, 3, mapkeys, "In_NOMBRE_ARCHIVO",paramcomunJson);
            for (JsonElement line: jsonObjExcel){
                JsonObject lineObj;
//                String valor = extactParamString(JsonElement, "In_NUMERO_OC");
//                
                numline++;
                lineExcelFaltantes--;
                if (line instanceof JsonObject) {
                    lineObj = (JsonObject) line;
                } else {
                    continue;
                }
//                if (numline <3)
//                    logger.info("linea " + numline + ":"+ lineObj.toString());
                Carga4JsonLong(  stmt,  4,mapkeys, "In_CORRELATIVO_CCOSTOCMOV",lineObj);
//                if (numline <3)
//                    logger.info("In_CENTRO_COSTO");
                Carga4JsonString(stmt, 5,mapkeys, "In_CENTRO_COSTO",lineObj);
//                if (numline <3)
//                    logger.info("In_AGNO_CENTRO_COSTO");
                Carga4JsonLong(  stmt, 6,mapkeys, "In_AGNO_CENTRO_COSTO",lineObj);
//                if (numline <3)
//                    logger.info("In_ITEM");
                Carga4JsonLong(  stmt, 7,mapkeys, "In_ITEM",lineObj);
//                if (numline <3)
//                    logger.info("In_NUMERO_OC");
                Carga4JsonLong(  stmt, 8,mapkeys, "In_NUMERO_OC",lineObj);
//                if (numline <3)
//                    logger.info("In_ESTADO_APROBACION");
                Carga4JsonString(stmt, 9,mapkeys, "In_ESTADO_APROBACION",lineObj);
//                if (numline <3)
//                    logger.info("In_MONTO_CCOSTOCMOV");
                Carga4JsonLong(  stmt, 10,mapkeys, "In_MONTO_CCOSTOCMOV",lineObj);
//                if (numline <3)
//                    logger.info("In_ID_MONEDA");
                Carga4JsonLong(  stmt, 11,mapkeys, "In_ID_MONEDA",lineObj);
//                if (numline <3)
//                    logger.info("In_FECHA_CREACION_CCOSTOCMOV");
                Carga4JsonDate(stmt, 12,mapkeys, "In_FECHA_CREACION_CCOSTOCMOV",lineObj,"dd/MM/yy");
//                if (numline <3)
//                    logger.info("In_ID_PRODUCTO_COMPRA");
                Carga4JsonString(stmt, 13,mapkeys, "In_ID_PRODUCTO_COMPRA",lineObj);
//                if (numline <3)
//                    logger.info("In_CANTIDAD_PRODUCTO_COOMPRA");
                Carga4JsonLong(  stmt, 14,mapkeys, "In_CANTIDAD_PRODUCTO_COOMPRA",lineObj);
//                if (numline <3)
//                    logger.info("In_NOMBRE_COMPRADOR");
                Carga4JsonString(stmt, 15,mapkeys, "In_NOMBRE_COMPRADOR",lineObj);
//                if (numline <3)
//                    logger.info("In_ID_PROVEEDOR");
                Carga4JsonString(stmt, 16,mapkeys, "In_ID_PROVEEDOR",lineObj);
//                if (numline <3)
//                    logger.info("In_NOMBRE_PROVEEDOR");
                Carga4JsonString(stmt, 17,mapkeys, "In_NOMBRE_PROVEEDOR",lineObj);
//                if (numline <3)
//                    logger.info("In_CODIGO_CUENTA");
                Carga4JsonString(stmt, 18,mapkeys, "In_CODIGO_CUENTA",lineObj);
//                if (numline <3)
//                    logger.info("In_DESCRIPCION_CCOSTOCMOV");
                Carga4JsonString(stmt, 19,mapkeys, "In_DESCRIPCION_CCOSTOCMOV",lineObj);
//                if (numline <3)
//                    logger.info("In_ID_PROYECTO_ERP");
                Carga4JsonString(stmt, 20,mapkeys, "In_ID_PROYECTO_ERP",lineObj);
//                if (numline <3)
//                    logger.info("In_NOMBRE_PROYECTO_ERP");
                Carga4JsonString(stmt, 21,mapkeys, "In_NOMBRE_PROYECTO_ERP",lineObj);
//                if (numline <3)
//                    logger.info("In_modalidad_carga");
                if (lineExcelFaltantes == 0){
                    Carga4JsonString(stmt, 22,mapkeys, "In_modalidad_carga_last",lineObj);
                } else {
                    Carga4JsonString(stmt, 22,mapkeys, "In_modalidad_carga",lineObj);
                }
                stmt.registerOutParameter(23, Types.NUMERIC);
                stmt.registerOutParameter(24, Types.NUMERIC);
                stmt.registerOutParameter(25, Types.VARCHAR);
//                if (numline <3)
//                    logger.info("Execute()");
                stmt.execute();
                out_id_presupuesto_ccostoresmov = stmt.getLong(23);
                out_codigo = stmt.getLong(24);
                out_mensaje = stmt.getString(25);
//                if (numline <10) {
//                    logger.info("RES.MOV(" + numline +  ")=" + out_id_presupuesto_ccostoresmov + "/OUTCODIGO:" + out_codigo + "/MSG:" + out_mensaje);
//                }
                if (out_codigo != 0 ) {
                    try {
                        if (out_id_presupuesto_ccostoresmov != 0)
                            vueltaAtras(conn,out_id_presupuesto_ccostoresmov);
                    } catch (Exception e) {}
                    dataRetornar.put("out_codigo", out_codigo);
                    dataRetornar.put("out_mensaje", out_mensaje);
                    return dataRetornar;
                }
            }
        } catch (ParseException ex) {
            String msg = "ParseException en linea # " + numline;
            logger.error(msg);
            logger.error(ex.fillInStackTrace());
            if (out_id_presupuesto_ccostoresmov != 0L){
                try {
                    vueltaAtras(conn,out_id_presupuesto_ccostoresmov);
                } catch (Exception e) {}
            }
            throw new GesvitaException(msg);
        } catch (GesvitaException ex) {
            String msg = "Error en linea # " + numline + " : " +ex.getMessage();
            logger.error(msg);
            logger.error(ex.fillInStackTrace());
            if (out_id_presupuesto_ccostoresmov != 0L){
                try {
                    vueltaAtras(conn,out_id_presupuesto_ccostoresmov);
                } catch (Exception e) {}
            }
            throw new GesvitaException(msg);
        }
        dataRetornar.put("out_id_presupuesto_ccostoresmov", out_id_presupuesto_ccostoresmov);
        dataRetornar.put("out_codigo", out_codigo);
        dataRetornar.put("out_mensaje", out_mensaje);
        dataRetornar.put("out_numlines", "" + (numline-1));
        return dataRetornar;
    }
    
    private void vueltaAtras(Connection conn, long id) throws SQLException{
//            logger.info("Vuelta atras id " + id);
            CallableStatement stmt = conn.prepareCall("{call PKG_MONITOR_DELETE.PROYECTO_PRESUPUESTO_CCOSTOMOV_DEL"
                    + "(?,?,?)}");
            stmt.setLong(1, id);
            stmt.registerOutParameter(2, Types.NUMERIC);
            stmt.registerOutParameter(3, Types.VARCHAR);
            stmt.execute();
    }
    
    private void Carga4JsonString(CallableStatement stmt, int i, Map<String,Columna> mapkeys,String field, JsonObject paramcomunJson)
            throws GesvitaException, SQLException {
        String valor;
        Columna f = mapkeys.get(field);
        if (f == null) {
            throw new GesvitaException("Campo: " + field + " desconocido");
        }
        if (f.isObligatorio()) {
            if (! paramcomunJson.has(field))
                throw new GesvitaException("Falta campo obligatorio: " + field);;
            valor =  extactParamString(paramcomunJson, field);
        } else {
            if (! paramcomunJson.has(field)) {
                valor= f.getDefaultValue();
                if (valor.equals("null"))
                    stmt.setNull(i, Types.VARCHAR );
            } else {
                valor=  extactParamString(paramcomunJson, field);
            }
        }
        stmt.setString(i, valor);
    }

    private void Carga4JsonLong(CallableStatement stmt, int i, Map<String,Columna> mapkeys,String field, JsonObject paramcomunJson)
            throws GesvitaException, SQLException {
        String valor;
        long valorL;
        Columna f = mapkeys.get(field);
        if (f == null) {
            throw new GesvitaException("Campo: " + field + " desconocido");
        }
        if (f.isObligatorio()) {
            if (! paramcomunJson.has(field))
                throw new GesvitaException("Falta campo obligatorio: " + field);
            valor = extactParamString(paramcomunJson, field);
            if (valor.equals("null")) {
                throw new GesvitaException("Falta campo obligatorio: " + field);
            } 
            valorL = Long.parseLong(valor);
        } else {
            if (! paramcomunJson.has(field)) {
                valor = f.getDefaultValue();
            } else {
                valor = extactParamString(paramcomunJson, field);
            }
            if (valor.equals("null")) {
                stmt.setNull(i, Types.NUMERIC );
                return;
            }
            valorL = Long.parseLong(valor);
        }
        stmt.setLong(i, valorL);
    }

    private void Carga4JsonDate(CallableStatement stmt, int i, Map<String,Columna> mapkeys, String field, JsonObject paramcomunJson, String formato)
            throws ParseException, GesvitaException, SQLException {
        String valor;
        java.sql.Date valorD;
        SimpleDateFormat sdf = new SimpleDateFormat(formato);
        Columna f = mapkeys.get(field);
        if (f == null) {
            throw new GesvitaException("Campo: " + field + " desconocido");
        }
        if (f.isObligatorio()) {
            if (! paramcomunJson.has(field))
                throw new GesvitaException("Falta campo obligatorio: " + field);
            valorD = new java.sql.Date(sdf.parse(extactParamString(paramcomunJson, field)).getTime());
        } else {
            if (! paramcomunJson.has(field)) {
                valor = f.getDefaultValue();
            } else {
                valor = extactParamString(paramcomunJson, field);
            }
            if (valor.equals("null")) {
                stmt.setNull(i, Types.DATE );
                return;
            }
            valorD =  new java.sql.Date(sdf.parse(valor).getTime());
        }
        stmt.setDate(i, valorD);
    }
    
    private String extactParamString(JsonObject jsonObj, String field) throws GesvitaException {
        JsonElement value = jsonObj.get(field);
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
