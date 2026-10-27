package com.mycompany.promtzalp2.gui;

import com.mycompany.promtzalp2.errores.ErrorCompilacion;
import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

/**
 * Editor de codigo PromptZal: numeros de linea, resaltado de sintaxis,
 * marcas de error y control del archivo abierto (si tiene cambios sin guardar).
 */
public class EditorCodigo {

    private final CodeArea area = new CodeArea();
    private final VirtualizedScrollPane<CodeArea> vista = new VirtualizedScrollPane<>(area);

    /** Errores de la ultima compilacion; se borran al editar el texto. */
    private List<ErrorCompilacion> errores = List.of();
    private File archivo;
    private boolean modificado;
    /** Evita marcar como modificado cuando el texto se carga desde un archivo. */
    private boolean cargando;
    private Runnable alCambiarEstado = () -> { };

    public EditorCodigo() {
        area.setParagraphGraphicFactory(LineNumberFactory.get(area));

        area.textProperty().addListener((obs, viejo, nuevo) -> {
            if (cargando) {
                return;
            }
            errores = List.of();   // el codigo cambio: las marcas viejas ya no aplican
            if (!modificado) {
                modificado = true;
                alCambiarEstado.run();
            }
        });

        // Re-colorea 150 ms despues de que el usuario deja de escribir.
        area.multiPlainChanges()
            .successionEnds(Duration.ofMillis(150))
            .subscribe(cambios -> aplicarResaltado());
    }

    /** Recalcula y aplica los colores de todo el texto. */
    public void aplicarResaltado() {
        area.setStyleSpans(0, ResaltadorSintaxis.calcular(area.getText(), errores));
    }

    /** Reemplaza el contenido (al abrir un archivo o crear uno nuevo). */
    public void cargarTexto(String texto, File archivo) {
        cargando = true;
        area.replaceText(texto.replace("\r\n", "\n"));
        cargando = false;
        this.archivo = archivo;
        this.modificado = false;
        this.errores = List.of();
        area.getUndoManager().forgetHistory();
        area.moveTo(0);
        area.requestFollowCaret();
        aplicarResaltado();
        alCambiarEstado.run();
    }

    /** Subraya en el editor los errores de la ultima compilacion. */
    public void marcarErrores(List<ErrorCompilacion> lista) {
        errores = new ArrayList<>(lista);
        aplicarResaltado();
    }

    /** Mueve el cursor a una fila y columna (desde 1) y le da el foco al editor. */
    public void irA(int fila, int columna) {
        int parrafo = Math.max(0, Math.min(fila - 1, area.getParagraphs().size() - 1));
        int col = Math.max(0, Math.min(columna - 1, area.getParagraphLength(parrafo)));
        area.moveTo(parrafo, col);
        area.requestFollowCaret();
        area.requestFocus();
    }

    /** Se llama despues de guardar con exito. */
    public void marcarGuardado(File archivo) {
        this.archivo = archivo;
        this.modificado = false;
        alCambiarEstado.run();
    }

    public VirtualizedScrollPane<CodeArea> getVista() {
        return vista;
    }

    public CodeArea getArea() {
        return area;
    }

    public String getTexto() {
        return area.getText();
    }

    public File getArchivo() {
        return archivo;
    }

    public boolean isModificado() {
        return modificado;
    }

    /** Accion a ejecutar cuando cambia el estado (archivo o cambios sin guardar). */
    public void setAlCambiarEstado(Runnable accion) {
        this.alCambiarEstado = accion;
    }
}