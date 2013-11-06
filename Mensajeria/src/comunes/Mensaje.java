/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package comunes;

import java.io.Serializable;

/**
 *
 * @author Alex Moreno
 */
public class Mensaje implements Serializable{
    private TipoMensaje tipo;
    private Object datos;
    
    public Mensaje(TipoMensaje tipo, Object datos) {
        this.tipo = tipo;
        this.datos = datos;
    }
    
    public TipoMensaje getTipoMensaje() {
        return this.tipo;
    }
    
    public Object getDatos() {
        return this.datos;
    }
}
