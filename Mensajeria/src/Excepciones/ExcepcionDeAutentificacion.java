/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Excepciones;

/**
 *
 * @author Dell
 */
public class ExcepcionDeAutentificacion extends Exception {

    /**
     * Creates a new instance of
     * <code>ExcepcionDeAutentificacion</code> without detail message.
     */
    public ExcepcionDeAutentificacion() {}

    /**
     * Constructs an instance of
     * <code>ExcepcionDeAutentificacion</code> with the specified detail
     * message.
     *
     * @param msg the detail message.
     */
    public ExcepcionDeAutentificacion(String msg) {
        super(msg);
    }
}
