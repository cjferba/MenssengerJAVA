/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mensajeriacliente;

import Excepciones.ExcepcionDeAutentificacion;
import comunes.EstadoUsuario;
import comunes.Mensaje;
import comunes.TipoMensaje;
import static comunes.TipoMensaje.COMPROBAR_ESTADO_USUARIO_ACK;
import static comunes.TipoMensaje.ERROR;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 *
 * @author Alex Moreno
 */
class Cliente {
    private String username;
    private String clave;
    private Socket cliente;
    private EstadoUsuario estado;
    private ObjectOutputStream salida;
    private ObjectInputStream entrada;
    
    protected Cliente(String nomUsuario, String clave) throws IOException, ExcepcionDeAutentificacion {
        this.username = nomUsuario;
        this.clave = clave;
    }
    
    public String getUsername() {
        return this.username;
    }
    
    public String getIp() {
        return cliente.getInetAddress().getHostAddress();
    }
    
    protected void conectar(String ip, int puerto) throws IOException {
        System.out.println( "Intentando realizar conexión\n" );
        cliente = new Socket( ip, puerto );
        System.out.println( "Conectado a: " +  cliente.getInetAddress().getHostName() );
        
        salida = new ObjectOutputStream( cliente.getOutputStream() );
        salida.flush();
        entrada = new ObjectInputStream( cliente.getInputStream() );
    }

    protected void autenticar() throws IOException, ExcepcionDeAutentificacion {
        Mensaje mensaje = new Mensaje(TipoMensaje.AUTENTIFICACION_REQUEST, this.username + ":" + this.clave);
        enviarMensaje(mensaje);
        recibirAutenticacion();
    }
    
    protected Mensaje comprobarEstadoUsuario(String usuario) {
        Mensaje mensaje = new Mensaje(TipoMensaje.COMPROBAR_ESTADO_USUARIO_REQUEST, usuario);
        enviarMensaje(mensaje);
        return recibirMensaje(mensaje);
    }
    
    protected Mensaje enviarNombreLocalizacionYEstado(String nombre, String localizacion, String estado) {
        Mensaje mensaje = new Mensaje(TipoMensaje.COMUNICAR_NOMBRE_Y_LOCALIZACION_Y_ESTADO_REQUEST, nombre + ":" + localizacion + ":" + estado);
        enviarMensaje(mensaje);
        return recibirMensaje(mensaje);
    }
    
    protected Mensaje consultarLocalizacionUsuario(String usuario) {
        Mensaje mensaje = new Mensaje(TipoMensaje.CONSULTAR_LOCALIZACION_USUARIO_REQUEST, usuario);
        enviarMensaje(mensaje);
        return recibirMensaje(mensaje);
    }
    
    private void recibirAutenticacion() throws IOException, ExcepcionDeAutentificacion {
        Mensaje mensaje;
        try {
            mensaje = ( Mensaje ) entrada.readObject();
            switch(mensaje.getTipoMensaje()) {
                case ERROR: throw new ExcepcionDeAutentificacion((String) mensaje.getDatos());
            }
        } catch (ClassNotFoundException ex) {
            System.out.println("Se escribió un tipo de objeto desconocido.");
        }
    }
   
    protected void cerrarConexion() {
        try {
            Mensaje mensaje = new Mensaje(TipoMensaje.FINALIZAR_COMUNICACION, null);
            enviarMensaje(mensaje);
            if (salida != null) salida.close();
            if (entrada != null) entrada.close();
            if (cliente != null) cliente.close();
            System.out.println( "\nCerrando conexión" );
        } catch( IOException ex ) {}
    }

    private void enviarMensaje( Mensaje mensaje ) {
        try {
            if (salida != null) {
                salida.writeObject( mensaje );
                salida.flush();
                System.out.println("CLIENTE>>> Enviado mensaje \"" + mensaje.getTipoMensaje().name() + "\"");
            }
        } catch ( IOException ex ) {
            System.err.println( "Error al escribir el objeto" );
        }
    }
    
    private Mensaje recibirMensaje( Mensaje mensajeInicial ) {
        Mensaje mensaje;
        try {
            mensaje = ( Mensaje ) entrada.readObject();
            switch(mensaje.getTipoMensaje()) {
                case COMPROBAR_ESTADO_USUARIO_ACK:
                    System.out.println("SERVIDOR>> El estado del usuario \"" + ((String) mensajeInicial.getDatos()) + "\" es: " + ((String) mensaje.getDatos()));
                    break;
                case COMUNICAR_NOMBRE_Y_LOCALIZACION_Y_ESTADO_ACK:
                    System.out.println("SERVIDOR>> Datos recibidos con éxito");
                    break;
                case CONSULTAR_LOCALIZACION_USUARIO_ACK:
                    System.out.println("SERVIDOR>> La localización del usuario \"" + ((String) mensajeInicial.getDatos()) + "\" es: " + ((String) mensaje.getDatos()));
                    break;
                case ERROR:
                    System.out.println("Error: " + ((String) mensaje.getDatos()));
            }
            return mensaje;
        } catch (ClassNotFoundException ex) {
            System.out.println("Se escribió un tipo de objeto desconocido.");
        } catch (IOException ex) {
            System.out.println("Error desconocido de entrada y salida en la lectura.");
        }
        return null;
    }
}
