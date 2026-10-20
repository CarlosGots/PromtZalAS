package com.mycompany.promtzalp2.errores;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ManejadorErrores {

    private final List<ErrorCompilacion> errores = new ArrayList<>();

    public void agregar(ErrorCompilacion error) {
        errores.add(error);
    }

    public List<ErrorCompilacion> getErrores() {
        return Collections.unmodifiableList(errores);
    }

    public boolean hayErrores() {
        return !errores.isEmpty();
    }

    public int cantidad(TipoError tipo) {
        int total = 0;
        for (ErrorCompilacion e : errores) {
            if (e.getTipo() == tipo) {
                total++;
            }
        }
        return total;
    }

    public void limpiar() {
        errores.clear();
    }
}