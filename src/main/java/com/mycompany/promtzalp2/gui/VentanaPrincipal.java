package com.mycompany.promtzalp2.gui;

import atlantafx.base.theme.Styles;
import com.mycompany.promtzalp2.Compilador;
import com.mycompany.promtzalp2.ResultadoCompilacion;
import com.mycompany.promtzalp2.errores.ManejadorErrores;
import com.mycompany.promtzalp2.errores.TipoError;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Ventana principal del IDE. Arma la distribucion de paneles, maneja los
 * archivos .pz y conecta el boton Compilar con el Compilador.
 *
 *  [ menu + barra de herramientas                         ]
 *  [ arbol AST | editor de codigo      | promptzal        ]
 *  [ Problemas | Consola | Tokens | Simbolos              ]
 *  [ barra de estado                                      ]
 */
public class VentanaPrincipal extends BorderPane {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final Stage stage;
    private final Compilador compilador = new Compilador();

    private final EditorCodigo editor = new EditorCodigo();
    private final ArbolAST arbol = new ArbolAST();
    private final PanelPromptzal panelPromptzal = new PanelPromptzal();
    private final PanelProblemas panelProblemas = new PanelProblemas();
    private final PanelTokens panelTokens = new PanelTokens();
    private final PanelSimbolos panelSimbolos = new PanelSimbolos();
    private final TextArea consola = new TextArea();
    private final BarraEstado barraEstado = new BarraEstado();

    private final TabPane pestanas = new TabPane();
    private final Tab tabProblemas = new Tab("Problemas", panelProblemas);
    private final Tab tabConsola = new Tab("Consola", consola);
    private final Tab tabTokens = new Tab("Tokens", panelTokens);
    private final Tab tabSimbolos = new Tab("Símbolos", panelSimbolos);

    /** Resultado de la ultima compilacion (lo usaran los reportes). */
    private ResultadoCompilacion ultimoResultado;

    public VentanaPrincipal(Stage stage) {
        this.stage = stage;

        setTop(new VBox(crearMenu(), crearBarraHerramientas()));
        setCenter(crearAreaTrabajo());
        setBottom(barraEstado);
        conectarEventos();

        editor.cargarTexto(EJEMPLO, null);
        log("PromptZal IDE listo. Presiona F5 para compilar.");
    }

    // =====================================================================
    //  Construccion de la interfaz
    // =====================================================================

    private MenuBar crearMenu() {
        Menu archivo = new Menu("Archivo");
        archivo.getItems().addAll(
                item("Nuevo", "Shortcut+N", this::nuevo),
                item("Abrir...", "Shortcut+O", this::abrir),
                item("Guardar", "Shortcut+S", this::guardar),
                item("Guardar como...", "Shortcut+Shift+S", this::guardarComo),
                new SeparatorMenuItem(),
                item("Salir", null, this::salir));

        Menu compilar = new Menu("Compilar");
        compilar.getItems().add(item("Compilar", "F5", this::compilar));

        Menu ayuda = new Menu("Ayuda");
        ayuda.getItems().add(item("Acerca de", null, this::acercaDe));

        return new MenuBar(archivo, compilar, ayuda);
    }

    private ToolBar crearBarraHerramientas() {
        Button nuevo = boton("Nuevo", "Nuevo archivo (Ctrl+N)", this::nuevo);
        Button abrir = boton("Abrir", "Abrir archivo .pz (Ctrl+O)", this::abrir);
        Button guardar = boton("Guardar", "Guardar (Ctrl+S)", this::guardar);
        Button compilar = boton("▶  Compilar", "Compilar (F5)", this::compilar);
        compilar.getStyleClass().add(Styles.SUCCESS);
        return new ToolBar(nuevo, abrir, guardar, new Separator(), compilar);
    }

    /** Distribucion central: tres columnas arriba y pestanas abajo. */
    private Node crearAreaTrabajo() {
        consola.setEditable(false);
        consola.getStyleClass().add("consola");
        pestanas.getTabs().addAll(tabProblemas, tabConsola, tabTokens, tabSimbolos);
        pestanas.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Node panelArbol = conTitulo("ESTRUCTURA (AST)", arbol);
        SplitPane horizontal = new SplitPane(panelArbol, editor.getVista(), panelPromptzal);
        horizontal.setDividerPositions(0.18, 0.64);
        SplitPane.setResizableWithParent(panelArbol, false);

        SplitPane vertical = new SplitPane(horizontal, pestanas);
        vertical.setOrientation(Orientation.VERTICAL);
        vertical.setDividerPositions(0.70);
        return vertical;
    }

    private void conectarEventos() {
        editor.setAlCambiarEstado(this::actualizarTitulo);
        editor.getArea().caretPositionProperty().addListener((obs, viejo, nuevo) ->
                barraEstado.setPosicion(editor.getArea().getCurrentParagraph() + 1,
                        editor.getArea().getCaretColumn() + 1));

        panelProblemas.setAlSeleccionar(e -> editor.irA(e.getFila(), e.getColumna()));
        arbol.setAlSeleccionar(n -> editor.irA(n.getFila(), n.getColumna()));

        panelPromptzal.setNombreBase(this::nombreBase);
        panelPromptzal.setAlGuardar(ruta -> log("Promptzal guardado en " + ruta));

        stage.setOnCloseRequest(ev -> {
            if (!confirmarDescartarCambios()) {
                ev.consume();
            }
        });
    }

    // =====================================================================
    //  Compilacion
    // =====================================================================

    /** Ejecuta las 4 fases y actualiza todos los paneles con el resultado. */
    private void compilar() {
        log("Compilando " + nombreArchivo() + "...");
        try {
            ResultadoCompilacion r = compilador.compilar(editor.getTexto());
            ultimoResultado = r;
            ManejadorErrores errores = r.getErrores();

            log(String.format("  Lexico:     %d tokens, %d errores",
                    r.getTokens().size(), errores.cantidad(TipoError.LEXICO)));
            log(String.format("  Sintactico: %d errores", errores.cantidad(TipoError.SINTACTICO)));
            log(String.format("  Semantico:  %d simbolos, %d errores",
                    r.getTablaSimbolos().getSimbolos().size(), errores.cantidad(TipoError.SEMANTICO)));

            panelProblemas.mostrar(errores.getErrores());
            panelTokens.mostrar(r.getTokens());
            panelSimbolos.mostrar(r.getTablaSimbolos().getSimbolos());
            arbol.mostrar(r.getPrograma());
            panelPromptzal.mostrar(r.getPromptzal(), !r.esExitosa());
            editor.marcarErrores(errores.getErrores());

            int total = errores.getErrores().size();
            tabProblemas.setText("Problemas (" + total + ")");
            tabTokens.setText("Tokens (" + r.getTokens().size() + ")");
            tabSimbolos.setText("Símbolos (" + r.getTablaSimbolos().getSimbolos().size() + ")");
            barraEstado.setTiempo(r.getTiempoMs());

            if (r.esExitosa()) {
                log("Compilacion exitosa en " + r.getTiempoMs() + " ms. Promptzal generado.");
                barraEstado.setEstado("✔ Compilación exitosa", false);
                pestanas.getSelectionModel().select(tabConsola);
            } else {
                log("Compilacion terminada con " + total + " error(es) en " + r.getTiempoMs() + " ms.");
                barraEstado.setEstado("✖ " + total + " error(es)", true);
                pestanas.getSelectionModel().select(tabProblemas);
            }
        } catch (RuntimeException ex) {
            // Respaldo para que un fallo inesperado no cierre el IDE.
            log("ERROR INTERNO: " + ex);
            barraEstado.setEstado("Error interno del compilador", true);
        }
    }

    private void limpiarResultados() {
        ultimoResultado = null;
        panelProblemas.limpiar();
        panelTokens.limpiar();
        panelSimbolos.limpiar();
        arbol.limpiar();
        panelPromptzal.limpiar();
        tabProblemas.setText("Problemas");
        tabTokens.setText("Tokens");
        tabSimbolos.setText("Símbolos");
        barraEstado.setEstado("Listo", false);
        barraEstado.setTiempo(-1);
    }

    // =====================================================================
    //  Archivos
    // =====================================================================

    private void nuevo() {
        if (confirmarDescartarCambios()) {
            editor.cargarTexto("", null);
            limpiarResultados();
            log("Nuevo archivo.");
        }
    }

    private void abrir() {
        if (!confirmarDescartarCambios()) {
            return;
        }
        FileChooser selector = selectorPz("Abrir archivo PromptZal");
        File archivo = selector.showOpenDialog(stage);
        if (archivo == null) {
            return;
        }
        try {
            String contenido = Files.readString(archivo.toPath(), StandardCharsets.UTF_8);
            editor.cargarTexto(contenido, archivo);
            limpiarResultados();
            log("Archivo abierto: " + archivo.getAbsolutePath());
        } catch (IOException ex) {
            error("No se pudo abrir el archivo", ex);
        }
    }

    /** Guarda en el archivo actual; si es nuevo, pregunta donde. Devuelve true si se guardo. */
    private boolean guardar() {
        if (editor.getArchivo() == null) {
            return guardarComo();
        }
        return escribir(editor.getArchivo());
    }

    private boolean guardarComo() {
        FileChooser selector = selectorPz("Guardar archivo PromptZal");
        selector.setInitialFileName(nombreArchivo());
        File archivo = selector.showSaveDialog(stage);
        if (archivo == null) {
            return false;
        }
        if (!archivo.getName().toLowerCase().endsWith(".pz")) {
            archivo = new File(archivo.getParentFile(), archivo.getName() + ".pz");
        }
        return escribir(archivo);
    }

    private boolean escribir(File archivo) {
        try {
            Files.writeString(archivo.toPath(), editor.getTexto(), StandardCharsets.UTF_8);
            editor.marcarGuardado(archivo);
            log("Archivo guardado: " + archivo.getAbsolutePath());
            return true;
        } catch (IOException ex) {
            error("No se pudo guardar el archivo", ex);
            return false;
        }
    }

    /** Selector de archivos .pz; abre en la carpeta del archivo actual o en "pruebas". */
    private FileChooser selectorPz(String titulo) {
        FileChooser selector = new FileChooser();
        selector.setTitle(titulo);
        selector.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PromptZal (*.pz)", "*.pz"),
                new FileChooser.ExtensionFilter("Todos los archivos", "*.*"));
        File carpeta = editor.getArchivo() != null ? editor.getArchivo().getParentFile() : new File("pruebas");
        if (carpeta != null && carpeta.isDirectory()) {
            selector.setInitialDirectory(carpeta);
        }
        return selector;
    }

    /** Si hay cambios sin guardar, pregunta. Devuelve true si se puede continuar. */
    private boolean confirmarDescartarCambios() {
        if (!editor.isModificado()) {
            return true;
        }
        ButtonType btnGuardar = new ButtonType("Guardar");
        ButtonType btnDescartar = new ButtonType("No guardar");
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION,
                "Hay cambios sin guardar en " + nombreArchivo() + ".",
                btnGuardar, btnDescartar, btnCancelar);
        alerta.setHeaderText("¿Deseas guardar los cambios?");
        alerta.initOwner(stage);

        Optional<ButtonType> respuesta = alerta.showAndWait();
        if (respuesta.isEmpty() || respuesta.get() == btnCancelar) {
            return false;
        }
        if (respuesta.get() == btnGuardar) {
            return guardar();
        }
        return true;
    }

    private void salir() {
        if (confirmarDescartarCambios()) {
            stage.close();
        }
    }

    // =====================================================================
    //  Utilidades
    // =====================================================================

    private void actualizarTitulo() {
        stage.setTitle("PromptZal IDE - " + nombreArchivo() + (editor.isModificado() ? " *" : ""));
    }

    private String nombreArchivo() {
        return editor.getArchivo() == null ? "sin_titulo.pz" : editor.getArchivo().getName();
    }

    /** Nombre del archivo sin la extension .pz (para sugerir el nombre del promptzal). */
    private String nombreBase() {
        String nombre = nombreArchivo();
        return nombre.toLowerCase().endsWith(".pz") ? nombre.substring(0, nombre.length() - 3) : nombre;
    }

    private void log(String mensaje) {
        consola.appendText("[" + LocalTime.now().format(HORA) + "] " + mensaje + "\n");
    }

    private void error(String titulo, Exception ex) {
        log(titulo + ": " + ex.getMessage());
        Alert alerta = new Alert(Alert.AlertType.ERROR, ex.getMessage());
        alerta.setHeaderText(titulo);
        alerta.initOwner(stage);
        alerta.showAndWait();
    }

    private void acercaDe() {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION,
                "Compilador del lenguaje PromptZal\n"
                + "Proyecto 2 - Laboratorio de Lenguajes Formales y de Programacion\n"
                + "USAC - CUNOC, Segundo semestre 2026\n\n"
                + "Lexico: JFlex | Sintactico: descendente recursivo | Interfaz: JavaFX");
        alerta.setHeaderText("PromptZal IDE");
        alerta.initOwner(stage);
        alerta.showAndWait();
    }

    private static MenuItem item(String texto, String atajo, Runnable accion) {
        MenuItem item = new MenuItem(texto);
        if (atajo != null) {
            item.setAccelerator(KeyCombination.keyCombination(atajo));
        }
        item.setOnAction(e -> accion.run());
        return item;
    }

    private static Button boton(String texto, String ayuda, Runnable accion) {
        Button boton = new Button(texto);
        boton.setTooltip(new Tooltip(ayuda));
        boton.setOnAction(e -> accion.run());
        return boton;
    }

    private static Node conTitulo(String titulo, Node contenido) {
        Label etiqueta = new Label(titulo);
        etiqueta.getStyleClass().add("titulo-panel");
        BorderPane panel = new BorderPane(contenido);
        panel.setTop(etiqueta);
        return panel;
    }

    /** Programa de ejemplo que se muestra al abrir el IDE. */
    private static final String EJEMPLO = """
            @modelo "claude-sonnet-4-6"
            @rol "analista de datos"
            @formato "markdown"
            // Agente que prepara el analisis de un archivo de ventas
            AGENTE analista {
              contexto = "Eres un analista de datos experto"
              variable ventas = CARGAR("ventas.csv")
              PREGUNTAR "Cuales son las 3 tendencias principales?" SOBRE ventas -> tendencias
              GENERAR codigo DESDE "grafica de barras de " + tendencias -> script
              RESUMIR tendencias EN 100 palabras -> resumen
            }
            EJECUTAR analista
            EXPORTAR resumen, script
            """;
}