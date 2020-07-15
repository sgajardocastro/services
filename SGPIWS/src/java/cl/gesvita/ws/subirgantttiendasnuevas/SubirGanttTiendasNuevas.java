package cl.gesvita.ws.subirgantttiendasnuevas;

//import cl.gesvita.ws.subirpresupuestocc.*;
import cl.gesvita.ws.obtener.exception.GesvitaException;
import cl.gesvita.ws.obtener.lib.ObtenerLib;
// import cl.gesvita.ws.subircapex.bean.TasksAndWarnings;
import cl.gesvita.ws.subirgantttiendasnuevas.bean.Entrada;
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
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.log4j.Logger;

/**
 * Servlet implementation class SubirGanttTiendasNuevas
 */
@WebServlet("/WSSubirGanttTiendasNuevas/SubirGanttTiendasNuevas")
public class SubirGanttTiendasNuevas extends HttpServlet {

    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(SubirGanttTiendasNuevas.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public SubirGanttTiendasNuevas() {
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

        // BYSECURITY logger.info("inicio()");
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
        };

        
//        "ITEM":"1","AREA":"CORRAL HERRAMIENTAS ","FECHA DE INICIO ESTIMADA":"12/7/18",
//        "FECHA DE TERMINO ESTIMADA":"12/11/18","FECHA DE INICIO     REAL":"12/7/18",
//        "FECHA DE TERMINO REAL":"12/14/18","DIFERENCIA DIAS":"3","LINEAL":"1","LADO":"2",
//        "EQUIPAMIENTO":"100%","ACCESORIOS EQUIPAMIENTO ":"0%",
//        "ACCESORIOS I&R/MOBILIARIO":"N/A",
//        "CARPINTERÍA":"N/A","GRÁFICA":"0%","CARGA DE PRODUCTOS ":"0%","MICROMERCHANDISING":"0%","COMENTARIOS":"MOVER TABIQUE PARA IMPLEMENTAR LINEAL"
//
//    private String area;
//    private Date fInicioEst;
//    private Date fTerminoEst;
//    private Date fInicioReal;
//    private Date fTerminoReal;
//    private long difDias;
//    private long lineal;
//    private long lado;
//    private Float pEquipamiento;
//    private Float pEquipamientoAccesorios;
//    private Float pEquipamientoMoviliario;
//    private Float pCarpinteria;
//    private Float pGrafica;
//    private Float pCargaProductos;
//    private Float pRomerchandising;
//    private String comentarios;
        // Campos: Identificador, Campo en el excel (sin espacios),
        //           tipo de dato (I,D,S o F), valor por omision, Obligatorio
        Columna CABECERASFILA[] = {
            new Columna("In_ITEM","ITEM","I","0",false),
            new Columna("In_AREA","AREA","S",null,false),
            new Columna("In_FECH_INICIO_EST","FECHA DE INICIO ESTIMADA","D",null,false),
            new Columna("In_FECH_TERMINO_EST","FECHA DE TERMINO ESTIMADA","D",null,false),
            new Columna("In_FECH_INICIO_REAL","FECHA DE INICIO REAL","D",null,false),
            new Columna("In_FECH_TERMINO_REAL","FECHA DE TERMINO REAL","D",null,false),
            new Columna("In_DIFERENCIA_DIAS","DIFERENCIA DIAS","I","0",false),
            new Columna("In_LINEAL","LINEAL","I","0",false),
            new Columna("In_LADO","LADO","I","0",false),
            new Columna("In_EQUIPAMIENTO","EQUIPAMIENTO","P",null,false),
            new Columna("In_ACC_EQUIPAMIENTO","ACCESORIOS EQUIPAMIENTO","P",null,false),
            new Columna("In_ACC_MOVILIARIO","ACCESORIOS I&R/MOBILIARIO","P",null,false),
            new Columna("In_CARPINTERIA","CARPINTERÍA","P",null,false),
            new Columna("In_GRAFICA","GRÁFICA","P",null,false),
            new Columna("In_CARGA_PRODUCTOS","CARGA DE PRODUCTOS","P",null,false),
            new Columna("In_MICROMERCHANDISING","MICROMERCHANDISING","P",null,false),
            new Columna("In_COMENTARIOS","COMENTARIOS","S","",false)
        };
        
        int i, len;
        Map<String,Columna> mapkeys = new HashMap<>();
        for (i =0, len = CABECERASFILA.length; i< len ; i++) {
            mapkeys.put(CABECERASFILA[i].getCampodb(), CABECERASFILA[i]);
        }
        for (i =0, len = CAMPOSCOMUNES.length ; i< len ; i++) {
            mapkeys.put(CAMPOSCOMUNES[i].getCampodb(), CAMPOSCOMUNES[i]);
        }
        
        String stringPatrones[] = { "F","^[\\d]+\\.[\\d]+$"
        ,"I","^[\\d\\,]+$"
        ,"D","^([0]?[1-9]|[1][0-2])(\\/|-)([0]?[1-9]|[12][0-9]|3[01])\\2(\\d{2})$"
        ,"P","^(100|(\\d{1,2})%)$"};
        
        Map<String,Pattern> listaPatrones = generarPatrones(stringPatrones);
        Connection conn = null;

        Map<String, Object> dataRetornar = new HashMap<>();
        Gson gson = new Gson();

        response.setContentType("application/json");

        String etapa = "Obtener Conexión";
        PrintWriter out = response.getWriter();
        DataSource datasource;
        JsonObject jsonObj,jsonObjResp;

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
            // Leer la t_hija excel
            jsonObj = ObtenerLib.readInput(logger, request);
            // BYSECURITY logger.info("t_hija leida");
            
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
                throw new GesvitaException("No se seleccionó archivo excel o el formato no corresponde (1)");
            }
            etapa = "Decodificar JSON de excel";
            Entrada entradas[] = decodificarColumnasExcel(logger,jsonObj_arch,
                mapkeys, listaPatrones);
            if (entradas.length== 0) {
                JsonObject item = new JsonObject();
                item.addProperty("resultadoEjecucion", "NOK");
                item.addProperty("out_codigo", 9997);
                item.addProperty("out_mensaje", "No existen tareas que guardar");
                sendData(response, out, item);
                return;
            }
            etapa = "generar conexion a BD";
            conn = datasource.getConnection();
            
//            etapa = "generar ID de BD";
//            paramcomunJson = GenerarFechaCarga(logger, conn, paramcomunJson);
            
//            if (true) {
//                dataRetornar.put("resultadoEjecucion","NOK");
//                dataRetornar.put("out_codigo", 9998);
//                dataRetornar.put("out_mensaje", "En construción");
//                sendData(response, out, gson, dataRetornar);
//                return;
//            }
            
            etapa = "Carga en BD";
            jsonObjResp = guardarEnBDExcel(logger,conn , mapkeys, paramcomunJson,entradas);
            sendData(response, out, jsonObjResp);
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

    private void sendData(HttpServletResponse response, PrintWriter out, JsonObject jsonObj) {
        response.setHeader("access-control-allow-origin", "*");
        out.print(jsonObj.toString());
    }

    private void sendData(HttpServletResponse response, PrintWriter out, Gson gson, Map<String, Object> dataRetornar) {
        String json = gson.toJson(dataRetornar);
        response.setHeader("access-control-allow-origin", "*");
        out.print(json);
    }

//    // Aquellos que no se puede leer el campo quedan como 0.
//    private Double leeDoubleFromJsonObject(String id, JsonObject jsonObj) {
//        if (!jsonObj.has(id))
//            return 0D;
//        String data = jsonObj.get(id).toString();
//        try {
//            return Double.parseDouble(data);
//        } catch (NumberFormatException ex) {
//            return 0D;
//            
//        }
//    }
    private Map<String, Pattern> generarPatrones(String[] stringPatrones) {
        Map<String, Pattern> salida = new HashMap<>();
        for (int i=0;i<stringPatrones.length ; i+=2){
            Pattern pattern_new;
            pattern_new = Pattern.compile(stringPatrones[i+1]);
            salida.put(stringPatrones[i], pattern_new);
        }
        return salida;
    }


    // Leer el excel y transformarlo en un JSON entendible
    private Entrada[] decodificarColumnasExcel(Logger logger, JsonArray arr, Map<String,Columna> mapkeys, Map<String, Pattern> listaPaterns) throws GesvitaException {
        List<Entrada> lista = new ArrayList();
        Entrada salida[];
        int k=1;
        if (arr.size() == 0 ) {
            throw new GesvitaException("No se seleccionó archivo excel o el formato no corresponde (2)");
        }
        JsonElement fisrtline = arr.get(1);
        if (! (fisrtline instanceof JsonObject)) {
            throw new GesvitaException("No se seleccionó archivo excel o el formato no corresponde (3)");
        }
        JsonObject filaObj = (JsonObject) fisrtline;
        Set<String> keysInFile = filaObj.keySet();
        
        // BYSECURITY logger.info("Array of key: " + keysInFile.toString());
        
        
        Map<String,String> mapKeyinFile;
        mapKeyinFile = new HashMap();
        keysInFile.forEach((ks) -> {
            String k2 = ks.trim();
            k2 = k2.replaceAll("[\t ]+", " ");
            mapKeyinFile.put(k2, ks);
        });
        
        // BYSECURITY logger.info("Mapa : " + mapKeyinFile.toString());
        
        for (JsonElement fila:arr) {
            if (fila instanceof JsonObject) {
                JsonObject filaIn = (JsonObject) fila;
                        // new JsonObject();
                Entrada e = decodificarFilaAEntrada(filaIn,mapkeys,mapKeyinFile,listaPaterns);
                if (e.getItem() == 0L)
                    continue;
                e.setCorrelativo(0F + (0.01F * k));
                lista.add(e);
                k++;
            } else {
                throw new GesvitaException("No se seleccionó archivo excel o el formato no corresponde (3)");
            }
        }
        salida = new Entrada[lista.size()];
        lista.toArray(salida);
        return salida;
    }

    private Entrada decodificarFilaAEntrada(JsonObject filaOut, Map<String,Columna> mapaCabecera, Map<String, String> mapKeyinFile, Map<String, Pattern> listaPaterns) throws GesvitaException {
        Entrada salida = new Entrada();
        
        long nro_item = leeParamNum(filaOut, "In_ITEM" , mapaCabecera, mapKeyinFile, listaPaterns);
        // BYSECURITY logger.info(filaOut.toString());
        salida.setItem(nro_item);
        if (nro_item == 0L)
            return salida;
        salida.setArea(leeParamString(filaOut, "In_AREA" , mapaCabecera, mapKeyinFile, listaPaterns));
        salida.setFInicioEst(leeParamDate(filaOut, "In_FECH_INICIO_EST" , mapaCabecera, mapKeyinFile, listaPaterns));
        salida.setFTerminoEst(leeParamDate(filaOut, "In_FECH_TERMINO_EST" , mapaCabecera, mapKeyinFile, listaPaterns));
        salida.setFInicioReal(leeParamDate(filaOut, "In_FECH_INICIO_REAL" , mapaCabecera, mapKeyinFile, listaPaterns));
        salida.setFTerminoReal(leeParamDate(filaOut, "In_FECH_TERMINO_REAL" , mapaCabecera, mapKeyinFile, listaPaterns));
        salida.setDifDias(leeParamNum(filaOut, "In_DIFERENCIA_DIAS" , mapaCabecera, mapKeyinFile, listaPaterns));
        salida.setLineal(leeParamNum(filaOut, "In_LINEAL" , mapaCabecera, mapKeyinFile, listaPaterns));
        salida.setLado(leeParamNum(filaOut, "In_LADO" , mapaCabecera, mapKeyinFile, listaPaterns));
        salida.setPEquipamiento(leeParamPerc(filaOut, "In_EQUIPAMIENTO" , mapaCabecera, mapKeyinFile, listaPaterns));
        salida.setPEquipamientoAccesorios(leeParamPerc(filaOut, "In_ACC_EQUIPAMIENTO" , mapaCabecera, mapKeyinFile, listaPaterns));
        salida.setPEquipamientoMoviliario(leeParamPerc(filaOut, "In_ACC_MOVILIARIO" , mapaCabecera, mapKeyinFile, listaPaterns));
        salida.setPCarpinteria(leeParamPerc(filaOut, "In_CARPINTERIA" , mapaCabecera, mapKeyinFile, listaPaterns));
        salida.setPGrafica(leeParamPerc(filaOut, "In_GRAFICA" , mapaCabecera, mapKeyinFile, listaPaterns));
        salida.setPCargaProductos(leeParamPerc(filaOut, "In_CARGA_PRODUCTOS" , mapaCabecera, mapKeyinFile, listaPaterns));
        salida.setPMicromerchandising(leeParamPerc(filaOut, "In_MICROMERCHANDISING" , mapaCabecera, mapKeyinFile, listaPaterns));
        salida.setComentarios(leeParamString(filaOut, "In_COMENTARIOS" , mapaCabecera, mapKeyinFile, listaPaterns));
        return salida;
    }

//    private long leeParamNum2(JsonObject filaOut, String in_ITEM,
//            Map<String, Columna> mapaCabecera,
//            Map<String, String> mapKeyinFile,
//            Map<String, Pattern> listaPaterns) throws GesvitaException {
//        String valor = leeParamString2(filaOut,in_ITEM,mapaCabecera,mapKeyinFile,listaPaterns);
//        if (valor == null) {
//            return 0L;
//        } else {
//            return Long.parseLong(valor);
//        }
//    }
//    
//    private String leeParamString2(JsonObject filaOut, String in_String,
//            Map<String,Columna> mapaCabecera, Map<String, String> mapKeyinFile,
//            Map<String, Pattern> listaPaterns) throws GesvitaException {
//        logger.info("JSON" + filaOut.toString());
//        logger.info("String búsqueda : '" + in_String);
//        Columna def_columna = mapaCabecera.get(in_String);
//        // logger.info("Def columna : '" + def_columna.getCampodb() + "/" + def_columna.getNombre());
//        String nameFieldInJson = mapKeyinFile.get(def_columna.getNombre());
//        logger.info("nameFieldInJson : '" + nameFieldInJson);
//        String valor;
//        
//        if (def_columna.isObligatorio()) {
//            if (! filaOut.has(nameFieldInJson))
//                throw new GesvitaException("Falta campo obligatorio: " + nameFieldInJson);
//            valor =  extactParamString(filaOut,nameFieldInJson);
//            boolean entradaOk = ValidarEntradaPatron(valor,def_columna.getTipo(),listaPaterns);
//            if (!entradaOk) {
//                throw new GesvitaException("Campo obligatorio no cumple formato: " + nameFieldInJson);
//            }
//        } else {
//            if (! filaOut.has(nameFieldInJson)) {
//                logger.info("Valor por default");
//                valor = def_columna.getDefaultValue();
//            } else {
//                logger.info("VALOR1 " + filaOut.get(nameFieldInJson).toString() + "/" +
//                        limpiezaString(filaOut.get(nameFieldInJson).toString()) );
//                valor = extactParamString(filaOut,nameFieldInJson);
//                boolean entradaOk = ValidarEntradaPatron(valor,def_columna.getTipo(),listaPaterns);
//                if (!entradaOk) {
//                    logger.info("No corresponde a patrón de tipo " + def_columna.getTipo());
//                    valor = def_columna.getDefaultValue();
//                }
//            }
//        }
//        logger.info("valor2 : '" + valor + "'");
//        if ("null".equals(valor))
//            return null;
//        else
//            return valor;
//    }
    
    private long leeParamNum(JsonObject filaOut, String in_ITEM,
            Map<String, Columna> mapaCabecera,
            Map<String, String> mapKeyinFile,
            Map<String, Pattern> listaPaterns) throws GesvitaException {
        String valor = leeParamString(filaOut,in_ITEM,mapaCabecera,mapKeyinFile,listaPaterns);
        if (valor == null) {
            return 0;
        } else {
            return Long.parseLong(valor);
        }
    }

    private Date leeParamDate(JsonObject filaOut, String in_ITEM,
            Map<String, Columna> mapaCabecera,
            Map<String, String> mapKeyinFile,
            Map<String, Pattern> listaPaterns) throws GesvitaException {
        String valor = leeParamString(filaOut,in_ITEM,mapaCabecera,mapKeyinFile,listaPaterns);
        SimpleDateFormat sdf = new SimpleDateFormat("m/d/y");
        if (valor == null) {
            return null;
        } else {
            try {
                return sdf.parse(valor);
            } catch (ParseException ex) {
                throw new GesvitaException("'" + valor + "' no corresponde a formato de fecha");
            }
        }
    }
    
    private Float leeParamPerc(JsonObject filaOut, String in_ITEM,
            Map<String, Columna> mapaCabecera,
            Map<String, String> mapKeyinFile,
            Map<String, Pattern> listaPaterns) throws GesvitaException {
        String valor = leeParamString(filaOut,in_ITEM,mapaCabecera,mapKeyinFile,listaPaterns);
        
        
        if (valor == null || valor.length() < 2) {
            return 0F;
        } else if ("N/A".equals(valor)) {
            return null;
        } else {
            return Float.parseFloat(valor.substring(0,valor.length()-1));
        }
    }
    
    private String leeParamString(JsonObject filaOut, String in_String,
            Map<String,Columna> mapaCabecera, Map<String, String> mapKeyinFile,
            Map<String, Pattern> listaPaterns) throws GesvitaException {
        Columna def_columna = mapaCabecera.get(in_String);
        String nameFieldInJson = mapKeyinFile.get(def_columna.getNombre());
        String valor;
        
        if (def_columna.isObligatorio()) {
            if (! filaOut.has(nameFieldInJson))
                throw new GesvitaException("Falta campo obligatorio: " + nameFieldInJson);
            valor =  extactParamString(filaOut,nameFieldInJson);
            if (!ValidarEntradaPatron(valor,def_columna.getTipo(),listaPaterns)) {
                throw new GesvitaException("Campo obligatorio no cumple formato: " + nameFieldInJson);
            }
        } else {
            if (! filaOut.has(nameFieldInJson)) {
                valor = def_columna.getDefaultValue();
            } else {
                valor = extactParamString(filaOut,nameFieldInJson);
                if (!ValidarEntradaPatron(valor,def_columna.getTipo(),listaPaterns)) {
                    valor = def_columna.getDefaultValue();
                }
            }
        }
        if ("null".equals(valor))
            return null;
        else
            return valor;
    }
    
    private boolean ValidarEntradaPatron(String entrada, String tipo,Map<String, Pattern> listaPaterns) {
        if (listaPaterns.containsKey(tipo)){
            Matcher m = listaPaterns.get(tipo).matcher(entrada);
            return m.matches();
        }else{
            return true;
        }
    }
    
//    private String recuperaValor (JsonObject filaObj, Columna campo, Pattern patern) {
//        String valor = null;
//        String id = campo.getNombre();
//        String tipo = campo.getTipo();
//        String omision = campo.getDefaultValue();
//        return null;
//    }

    private String readParamString(JsonObject jsonObj, String field) throws GesvitaException {
        if (!jsonObj.has(field))
            throw new GesvitaException("Expected field: '" + field + "' is not present in the input");
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
    
    private long readParamLong(JsonObject jsonObj, String field) throws GesvitaException {
        try {
            return Long.parseLong(readParamString(jsonObj, field));
        } catch (NumberFormatException e) {
            throw new GesvitaException("Expected field: '" + field + "' is not a number");
        }
    }
    
//    private String readFieldString(JsonObject jsonObj, String field) throws GesvitaException {
//        String fieldlow = field.toLowerCase();
//        if (!jsonObj.has(fieldlow))
//            throw new GesvitaException("Expected field: '" + field + "' is not present in the input");
//        JsonElement value = jsonObj.get(fieldlow);
//        String salida = value.toString();
//        if (!(value instanceof JsonArray) && !(value instanceof JsonObject)) {
//            if (salida.startsWith("\"") && salida.endsWith("\""))
//                salida = salida.substring(1, salida.length()-1);
//            else if (salida.startsWith("'") && salida.endsWith("'"))
//                salida = salida.substring(1, salida.length()-1);
//        }
//        return salida;
//    }
    
//    private long readFieldLong(JsonObject jsonObj, String field) throws GesvitaException {
//        try {
//            return Long.parseLong(readFieldString(jsonObj, field));
//        } catch (NumberFormatException e) {
//            throw new GesvitaException("Expected field: '" + field + "' is not a number");
//        }
//    }

    private JsonObject LeeParamComun(JsonObject jsonObj) throws GesvitaException {
        JsonObject paramcomun = new JsonObject();
        paramcomun.addProperty("In_NOMBRE_ARCHIVO", readParamString(jsonObj,"in_archivo") );
        paramcomun.addProperty("In_ID_PROYECTO",readParamLong(jsonObj,"in_id_proyecto") );
        paramcomun.addProperty("In_ID_TAREA",readParamLong(jsonObj,"in_id_tarea") );
        paramcomun.addProperty("In_ID_USUARIO",readParamLong(jsonObj,"in_id_usuario") );
        return paramcomun;
    }

//    private JsonObject GenerarFechaCarga(Logger logger, Connection conn, JsonObject paramcomunJson)
//            throws GesvitaException, SQLException {
//        // In_FECHA_CARGA
//        // new Columna("In_FECHA_CARGA","fecha","D",null,true),
//        String query = "SELECT TO_CHAR(CURRENT_DATE, 'dd/mm/yyyy') from dual";
//        PreparedStatement ps = conn.prepareStatement(query);
//        
//        ResultSet rs =  ps.executeQuery(); // ps.executeQuery();
//        if (rs.next()) {
//            paramcomunJson.addProperty("In_FECHA_CARGA",rs.getString(1));
//        } else {
//            throw new GesvitaException("No se puede recuperar fecha de BD");
//        }
//        try {
//            if (ps != null)
//                ps.close();
//            if (rs != null)
//                rs.close();
//        } catch (Exception e) {}
//        return paramcomunJson;
//        
//    }

    private JsonObject guardarEnBDExcel(Logger logger, Connection conn, Map<String,Columna> mapkeys,
            JsonObject paramcomunJson, Entrada[] entradas) throws GesvitaException {
        String[] nombre_tareas_hija = {
            "EQUIPAMIENTO","ACCESORIOS EQUIPAMIENTO",
            "ACCESORIOS I&R/MOBILIARIO","CARPINTERÍA",
            "GRÁFICA","CARGA DE PRODUCTOS",
            "MICROMERCHANDISING",
        };
        int numline = 1;
        int numitems = 0;
        int j;
        long out_codigo = 0L;
        // String out_mensaje = "";
        CallableStatement stmt = null;
        JsonObject item;
        // BYSECURITY logger.info(paramcomunJson.toString());
        long in_id_proyecto = Long.parseLong(readParamString(paramcomunJson, "In_ID_PROYECTO"));
        if (in_id_proyecto == 0L) {
            item = new JsonObject();
            item.addProperty("resultadoEjecucion", "NOK");
            item.addProperty("out_codigo", 9999);
            item.addProperty("out_mensaje", "Falta número de proyecto");
            return item;
        }
        
//        long listadoIdTareas[] = new long[nombre_tareas_hija.length];
        JsonObject jsonretorno = null;
        try {
            stmt = conn.prepareCall("{call PKG_MONITOR_INSERT.PROYECTO_TAREA_INS("
                    + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");
//            int i =0;
            for (Entrada t_padre : entradas) {
//                if (i<5) {
//                    // BYSECURITY logger.info("Padre " + i++ );
//                }
                jsonretorno = subirTaskSP(conn, stmt ,paramcomunJson, t_padre, 0, null);
                numitems++;
                long id_tarea_padre = Long.parseLong( jsonretorno.get("out_id_tarea").toString());
                out_codigo = Long.parseLong( jsonretorno.get("out_codigo").toString());
                for (j = 0;j<nombre_tareas_hija.length;j++) {
                    Entrada t = new Entrada(1.0F + j, 0, nombre_tareas_hija[j],        // Orden, #item, Nombre
                            t_padre.getFInicioEst(), t_padre.getFTerminoEst(),
                            t_padre.getFInicioReal(), t_padre.getFTerminoReal(),        // 4 Fechas (Inicio,Termino)x(Estimada,Real)
                            0, -1, -1,                                      //  Dif dias, linea, lado
                            t_padre.getPEquipamiento(),                     // % (Equip,Equip Acc,Acc Movi,Carp
                            t_padre.getPEquipamientoAccesorios(),           //    Graf, Carga Prod,MicroMer)
                            t_padre.getPEquipamientoMoviliario(),
                            t_padre.getPCarpinteria(),
                            t_padre.getPGrafica(),
                            t_padre.getPCargaProductos(),
                            t_padre.getPMicromerchandising(),             
                            nombre_tareas_hija[j]);                        //  Comentario
                    Float avance_i_j = null;
                    switch (j) {
                        case 0:
                            avance_i_j = t_padre.getPEquipamiento();
                            break;
                        case 1:
                            avance_i_j = t_padre.getPEquipamientoAccesorios();
                            break;
                        case 2:
                            avance_i_j = t_padre.getPEquipamientoMoviliario();
                            break;
                        case 3:
                            avance_i_j = t_padre.getPCarpinteria();
                            break;
                        case 4:
                            avance_i_j = t_padre.getPGrafica();
                            break;
                        case 5:
                            avance_i_j = t_padre.getPCargaProductos();
                            break;
                        default:
                            avance_i_j = t_padre.getPMicromerchandising();
                            break;
                    }
                    jsonretorno= subirTaskSP(conn, stmt, paramcomunJson, t, id_tarea_padre,avance_i_j);
                    numitems++;
                    // long id_tarea_hija = Long.parseLong( jsonretorno.get("out_id_tarea").toString());
                    out_codigo = Long.parseLong( jsonretorno.get("out_codigo").toString());
                    if (out_codigo != 0L) {
                        try {
                            vueltaAtras(conn,in_id_proyecto);
                        } catch (SQLException e) {}
                        item = new JsonObject();
                        item.addProperty("resultadoEjecucion", "NOK");
                        item.addProperty("out_codigo", out_codigo);
                        item.addProperty("out_mensaje", jsonretorno.get("out_mensaje").toString());
                        return item;
                    }
                }
            }
            if (jsonretorno != null) {
                String str = jsonretorno.get("out_mensaje").toString();
                item = new JsonObject();
                if (out_codigo == 0){
                    item.addProperty("resultadoEjecucion", "OK");
                    item.addProperty("numdata", numitems);
                } else {
                    item.addProperty("resultadoEjecucion", "NOK");
                }
                item.addProperty("out_codigo", out_codigo);
                item.addProperty("out_mensaje", limpiezaString(str));
                return item;
            } else {
                item = new JsonObject();
                item.addProperty("resultadoEjecucion", "NOK");
                item.addProperty("out_codigo", 9999);
                item.addProperty("out_mensaje", "No se obtuvo ninguna respuesta de servicio para ingreso de tareas");
                return item;
            }
        } catch (SQLException ex) {
            String msg = "SQLException en linea # " + numline;
            logger.error(msg);
            logger.error(ex.fillInStackTrace());
            if (in_id_proyecto != 0L){
                try {
                    vueltaAtras(conn,in_id_proyecto);
                } catch (SQLException e) {}
            }
            throw new GesvitaException(msg);
        } catch (GesvitaException ex) {
            String msg = "Error en linea # " + numline + " : " +ex.getMessage();
            logger.error(msg);
            logger.error(ex.fillInStackTrace());
            if (in_id_proyecto != 0L){
                try {
                    vueltaAtras(conn,in_id_proyecto);
                } catch (SQLException e) {}
            }
            throw new GesvitaException(msg);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {}
        }
    }

    private JsonObject subirTaskSP(Connection conn, CallableStatement stmt,  JsonObject jsonGral, Entrada t,long in_id_tarea_padre, Float avance) throws GesvitaException, SQLException {
        Clob clob;
        JsonObject dataRetornar = new JsonObject();
        // BYSECURITY logger.info("JSON_TASK = " + jsonTask.toString());
        // stmt = conn.prepareCall("{call PKG_MONITOR_INSERT.PROYECTO_TAREA_INS(?,to_date(?, \'DD/MM/YYYY\'),to_date(?, \'DD/MM/YYYY\'),to_date(?, \'DD/MM/YYYY\'),to_date(?, \'DD/MM/YYYY\'),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");
        // clob = conn.createClob();

        stmt.setFloat(1, Float.parseFloat(readParamString(jsonGral, "In_ID_PROYECTO")));
        setStatmentDate(stmt,2,t.getFInicioEst());
        setStatmentDate(stmt,3,t.getFTerminoEst());
        setStatmentDate(stmt,4,t.getFInicioReal());
        setStatmentDate(stmt,5,t.getFTerminoReal());
        if (in_id_tarea_padre == 0) {
            stmt.setNull(6, java.sql.Types.NUMERIC);
            stmt.setString(7, "");                      // TASK: in_id_tarea_dependencia
            stmt.setString(8, t.getArea() + " Línea " + t.getLineal() + " - Lado " + t.getLado());                   // TASK: in_nombre_tarea
            setStatmentString(stmt,9, t.getComentarios());  // Comentarios
            setStatmentLong(stmt,10,t.getDifDias());    // Duración Real
            stmt.setFloat(11, 0);                       // Tipo tarea
            stmt.setFloat(12, 3);                       //  "in_id_prioridad"= 3,media
            stmt.setFloat(13, 1);                       // in_id_estado (1 : No iniciado)
            stmt.setNull(14, java.sql.Types.NUMERIC);   //  "in_avance_planificado"
            stmt.setNull(15, java.sql.Types.NUMERIC);   //  "in_avance_real"
        } else {
            stmt.setLong(6, in_id_tarea_padre);
            stmt.setString(7, "");           // TASK: in_id_tarea_dependencia
            stmt.setString(8, t.getArea());             // TASK: in_nombre_tarea
            stmt.setString(9, "");                      // Comentarios
            stmt.setNull(10, java.sql.Types.NUMERIC);   // Duración Real
            stmt.setFloat(11, 0);                       // Tipo tarea : Normal
            stmt.setFloat(12, 3);                       //  "in_id_prioridad"= 3,media
            stmt.setNull(14, java.sql.Types.NUMERIC);   //  "in_avance_planificado"
            if (avance == null) {
                stmt.setFloat(13, 5);                       //  "in_id_estado"= 5 , N/A
                stmt.setNull(15, java.sql.Types.NUMERIC);   //  "in_avance_real"
            } else if (avance.doubleValue() == 0D) {
                stmt.setFloat(13, 1);                       //  "in_id_estado"= 1 , 0%
                stmt.setFloat(15,0F);                       // "in_avance_real"
            } else if (avance.doubleValue() == 100D) {
                stmt.setFloat(13, 3);                       //  "in_id_estado"= 3 , 100%
                stmt.setFloat(15,1F);                       // "in_avance_real"
            } else {
                stmt.setFloat(13, 2);                       //  "in_id_estado"= 2 , 0% < x < 100%
                stmt.setFloat(15, avance/100); // "in_avance_real"
            }
        }
        stmt.setNull(16, java.sql.Types.VARCHAR);          // "in_tarea_inbput"
        stmt.setNull(17, java.sql.Types.VARCHAR);          // "in_tarea_output"
        long nroUsuario = Long.parseLong(readParamString(jsonGral, "In_ID_USUARIO"));
        stmt.setFloat(18, 20);                             // Numerico : "in_id_formulario"
        stmt.setLong(19, nroUsuario);                      // Numero : "in_id_usuario_responsable"
        stmt.setLong(20, nroUsuario);                      // Numero : "in_id_usuario_ejecutor"
        clob = conn.createClob();
        clob.setString(1, "{lineal:" + t.getLineal() + ",lado:" + t.getLado() + "}");                        // Clob : "in_data_extendida"
        stmt.setClob(21, clob);
        stmt.setLong(22, nroUsuario);                      // Numerico : "in_id_usuario_creacion"
        stmt.setString(23, formatoNumOrden(t.getCorrelativo()));  // String : "in_id_bpm"
        // stmt.setLong(23, nroUsuario);                      // Numerico : "in_id_usuario_ejecutor"
        if (t.getDifDias() != 0) {
            stmt.setLong(24, t.getDifDias());              // Numerico : "in_duracion_planificada"
        } else {
            stmt.setNull(24, java.sql.Types.NUMERIC);
        }
        stmt.setNull(25, java.sql.Types.VARCHAR);          // String: "in_tipo_tarea_negocio"
        stmt.setNull(26, java.sql.Types.NUMERIC);          // Numerico : "in_id_tarea_template"
        stmt.setNull(27, java.sql.Types.NUMERIC);          // Numerico : "in_id_proyecto_enlacetemplate"
        stmt.setNull(28, java.sql.Types.NUMERIC);          // Numerico : "in_id_proyecto_enlace"
        stmt.setNull(29, java.sql.Types.NUMERIC);          // Numerico : "in_porcentaje_asignacion"
        stmt.setNull(30, java.sql.Types.NUMERIC);          // Numerico : "in_porcentaje_en_tarea_padre"
        stmt.setString(31, "" + t.getCorrelativo());       // Numerico : "in_codi_tarea"
        stmt.registerOutParameter(32, Types.NUMERIC);
        stmt.registerOutParameter(33, Types.NUMERIC);
        stmt.registerOutParameter(34, Types.VARCHAR);
        stmt.execute();
        dataRetornar.addProperty("out_id_tarea", stmt.getLong(32));
        dataRetornar.addProperty("out_codigo", stmt.getInt(33));
        dataRetornar.addProperty("out_mensaje", stmt.getString(34));
        return dataRetornar;
    }
    
    private void setStatmentDate(CallableStatement stmt, int i, Date fecha) throws SQLException {
        if (fecha == null) {
            stmt.setNull(i, java.sql.Types.DATE);
        } else {
            stmt.setDate(i, new java.sql.Date(fecha.getTime()));
        }
    }

    private void setStatmentLong(CallableStatement stmt, int i, Long data) throws SQLException {
        if (data == null) {
            stmt.setNull(i, java.sql.Types.NUMERIC);
        } else {
            stmt.setLong(i, data);
        }
    }

    private void setStatmentString(CallableStatement stmt, int i, String data) throws SQLException {
        if (data == null) {
            stmt.setNull(i, java.sql.Types.VARCHAR);
        } else {
            stmt.setString(i, data);
        }
    }
    
    private void vueltaAtras(Connection conn, long id) throws SQLException{
            // BYSECURITY logger.info("Vuelta atras id " + id);
            CallableStatement stmt = conn.prepareCall("{call PKG_MONITOR_DELETE.PROYECTO_ELIMINAR_PROYECTOALL"
                    + "(?,?,?)}");
            stmt.setLong(1, id);
            stmt.registerOutParameter(2, Types.NUMERIC);
            stmt.registerOutParameter(3, Types.VARCHAR);
            stmt.execute();
    }
    
//    private void Carga4JsonString(CallableStatement stmt, int j, Map<String,Columna> mapkeys,String field, JsonObject paramcomunJson)
//            throws GesvitaException, SQLException {
//        String valor;
//        Columna f = mapkeys.get(field);
//        if (f == null) {
//            throw new GesvitaException("Campo: " + field + " desconocido");
//        }
//        if (f.isObligatorio()) {
//            if (! paramcomunJson.has(field))
//                throw new GesvitaException("Falta campo obligatorio: " + field);;
//            valor =  extactParamString(paramcomunJson, field);
//        } else {
//            if (! paramcomunJson.has(field)) {
//                valor= f.getDefaultValue();
//                if (valor.equals("null"))
//                    stmt.setNull(j, Types.VARCHAR );
//            } else {
//                valor=  extactParamString(paramcomunJson, field);
//            }
//        }
//        stmt.setString(j, valor);
//    }

//    private void Carga4JsonLong(CallableStatement stmt, int j, Map<String,Columna> mapkeys,String field, JsonObject paramcomunJson)
//            throws GesvitaException, SQLException {
//        String valor;
//        long valorL;
//        Columna f = mapkeys.get(field);
//        if (f == null) {
//            throw new GesvitaException("Campo: " + field + " desconocido");
//        }
//        if (f.isObligatorio()) {
//            if (! paramcomunJson.has(field))
//                throw new GesvitaException("Falta campo obligatorio: " + field);;
//            valorL = Long.parseLong(extactParamString(paramcomunJson, field));
//        } else {
//            if (! paramcomunJson.has(field)) {
//                valor = f.getDefaultValue();
//            } else {
//                valor = extactParamString(paramcomunJson, field);
//            }
//            if (valor.equals("null")) {
//                stmt.setNull(j, Types.NUMERIC );
//                return;
//            }
//            valorL = Long.parseLong(valor);
//        }
//        stmt.setLong(j, valorL);
//    }

//    private void Carga4JsonDate(CallableStatement stmt, int j, Map<String,Columna> mapkeys, String field, JsonObject paramcomunJson, String formato)
//            throws ParseException, GesvitaException, SQLException {
//        String valor;
//        java.sql.Date valorD;
//        SimpleDateFormat sdf = new SimpleDateFormat(formato);
//        Columna f = mapkeys.get(field);
//        if (f == null) {
//            throw new GesvitaException("Campo: " + field + " desconocido");
//        }
//        if (f.isObligatorio()) {
//            if (! paramcomunJson.has(field))
//                throw new GesvitaException("Falta campo obligatorio: " + field);
//            valorD = new java.sql.Date(sdf.parse(extactParamString(paramcomunJson, field)).getTime());
//        } else {
//            if (! paramcomunJson.has(field)) {
//                valor = f.getDefaultValue();
//            } else {
//                valor = extactParamString(paramcomunJson, field);
//            }
//            if (valor.equals("null")) {
//                stmt.setNull(j, Types.DATE );
//                return;
//            }
//            valorD =  new java.sql.Date(sdf.parse(valor).getTime());
//        }
//        stmt.setDate(j, valorD);
//    }
    
    private String extactParamString(JsonObject jsonObj, String field) throws GesvitaException {
        JsonElement elem = jsonObj.get(field);
        
        return limpiezaString(elem.toString());
        
//        String salida = value.toString();
//        if (!(elem instanceof JsonArray) && !(elem instanceof JsonObject)) {
//            return limpiezaString(salida);
//        }
//        return salida;
    }
    
    private String limpiezaString(String in) {
        if (in.startsWith("\"") && in.endsWith("\""))
            return in.substring(1, in.length()-1);
        else if (in.startsWith("'") && in.endsWith("'"))
            return in.substring(1, in.length()-1);
        else
            return in;
    }

    private String formatoNumOrden(double num) {
        int desde = 2;
        if (num <0d) desde = 3;
        int entero = (int) num;
        double fraccion = num - entero;
        return String.format("%05d", entero) + "." + String.format("%014.12f", fraccion).substring(desde);
    }
}
