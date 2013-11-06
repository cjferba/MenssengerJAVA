/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mensajeriaservidor;

import comunes.EstadoUsuario;
import comunes.Mensaje;
import comunes.TipoMensaje;
import static comunes.TipoMensaje.COMPROBAR_ESTADO_USUARIO_REQUEST;
import static comunes.TipoMensaje.COMUNICAR_NOMBRE_Y_LOCALIZACION_Y_ESTADO_REQUEST;
import static comunes.TipoMensaje.CONSULTAR_LOCALIZACION_USUARIO_REQUEST;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 *
 * @author Alex Moreno
 */
public class MensajeriaServidor {
    private static HashMap<String, Usuario> users = new HashMap<String, Usuario>();
    private static ServerSocket servidor;
    private static Socket conexion;
    private static ObjectOutputStream salida;
    private static ObjectInputStream entrada;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        cargarUsuarios();
        try {
            servidor = new ServerSocket(15000);
            while ( true ) {
                try {
                    esperarConexion();
                    obtenerFlujos();
                    autentificacion();
                    procesarConexion();
                } catch (IOException ex) {
                    System.err.println("ERROR: No se ha podido realizar la conexión o se ha perdido (" + ex.getMessage() + ")");
                } finally {
                    cerrarConexion();
                }
            }
        } catch (IOException ex) {
            System.err.println("ERROR: No se ha podido iniciar el servidor");
        }        
    }
    
    public static void cargarUsuarios() {
        String cadenaDatosUser = "";
        String[] datosUsuario;
        Usuario usuario;
        FileReader f;
        try {
            f = new FileReader("users.txt");
            int caracter = f.read();
            while (caracter != -1) {
                if (String.valueOf((char) caracter).matches("[\r]")) {
                    datosUsuario = cadenaDatosUser.replaceAll("\n", "").split(":");
                    usuario = new Usuario(datosUsuario[0], datosUsuario[1], datosUsuario[2]);
                    usuario.setEstado(EstadoUsuario.AUSENTE);
                    users.put(datosUsuario[0], usuario);
                    cadenaDatosUser = "";
                } else { cadenaDatosUser += String.valueOf((char) caracter); }
                caracter = f.read();
            }
            System.out.println("Usuarios cargados");
        } catch (FileNotFoundException ex) {
            System.out.print("No se ha encontrado el archivo");
        } catch (IOException ex) {
            Logger.getLogger("Carga de usuarios erronea");
        }
    }

    private static void esperarConexion() throws IOException {
        System.out.println("Esperando una conexión");
        conexion = servidor.accept();      
        System.out.println("Conexión recibida de: " + conexion.getInetAddress().getHostName());
    }

    private static void obtenerFlujos() throws IOException {
        salida = new ObjectOutputStream(conexion.getOutputStream());
        salida.flush();
        entrada = new ObjectInputStream(conexion.getInputStream());
        System.out.println("Se recibieron los flujos de E/S");
    }

    private static void procesarConexion() throws IOException {
        Mensaje mensaje = null, mensajeAEnviar;
        Usuario usuario;
        String texto;
        while (mensaje == null || mensaje.getTipoMensaje() != TipoMensaje.FINALIZAR_COMUNICACION) {
            try {
                mensaje = ( Mensaje ) entrada.readObject();
                System.out.println("CLIENTE>> Petición \"" + mensaje.getTipoMensaje().name() + "\"");
                switch(mensaje.getTipoMensaje()) {
                    case COMPROBAR_ESTADO_USUARIO_REQUEST:
                        usuario = users.get((String) mensaje.getDatos());
                        if (usuario == null) {
                            texto = "Usuario \"" + (String) mensaje.getDatos() + "\" no encontrado en la base de datos";
                            mensajeAEnviar = new Mensaje(TipoMensaje.ERROR, texto);
                        } else { mensajeAEnviar = new Mensaje(TipoMensaje.COMPROBAR_ESTADO_USUARIO_ACK , usuario.getEstado()); }
                        enviarMensaje(mensajeAEnviar);
                        break;
                    case COMUNICAR_NOMBRE_Y_LOCALIZACION_Y_ESTADO_REQUEST:
                        String[] nombreLocalizacionEstado = ((String)mensaje.getDatos()).split(":");
                        usuario = users.get(nombreLocalizacionEstado[0]);
                        usuario.setIp(nombreLocalizacionEstado[1]);
                        usuario.setEstado(EstadoUsuario.valueOf(nombreLocalizacionEstado[2]));
                        mensajeAEnviar = new Mensaje(TipoMensaje.COMUNICAR_NOMBRE_Y_LOCALIZACION_Y_ESTADO_ACK , null);
                        enviarMensaje(mensajeAEnviar);
                        break;
                    case CONSULTAR_LOCALIZACION_USUARIO_REQUEST:
                        usuario = users.get((String) mensaje.getDatos());
                        if (usuario == null) {
                            texto = "El usuario \"" + ((String) mensaje.getDatos()) + "\" no encontrado en la base de datos";
                            mensajeAEnviar = new Mensaje(TipoMensaje.ERROR, texto);
                        } else {
                            if (usuario.getIp() == null) {
                                texto = "El usuario \"" + ((String) mensaje.getDatos()) + "\" no ha comunicado su localizacion";
                                mensajeAEnviar = new Mensaje(TipoMensaje.ERROR, texto);
                            } else { mensajeAEnviar = new Mensaje(TipoMensaje.CONSULTAR_LOCALIZACION_USUARIO_ACK , usuario.getIp()); }
                        }
                        enviarMensaje(mensajeAEnviar);
                        break;
                    case FINALIZAR_COMUNICACION:
                        System.out.println("SERVIDOR>> Sesión cerrada con el usuario de: " + conexion.getInetAddress().getHostName());
                }
            } catch (ClassNotFoundException ex) {
                System.out.println("\nSe escribió un tipo de objeto desconocido.");
                mensajeAEnviar = new Mensaje(TipoMensaje.ERROR, "Error en el servidor clase no encontrada");
                enviarMensaje(mensajeAEnviar);
            }
        }
    }

    private static void cerrarConexion() {
        System.out.println( "\nFinalizando la conexión" );

        try {
            if (salida != null) salida.close();
            if (entrada != null) entrada.close();
            if (conexion != null) conexion.close();
        } catch( IOException ex ) {
            System.out.println("\nConexion cerrada");
        }
    }

    private static void enviarMensaje( Mensaje mensaje ) {
        try {
            salida.writeObject( mensaje );
            salida.flush();
            System.out.println("\nSERVIDOR>>> Enviado mensaje \"" + mensaje.getTipoMensaje().name() + "\"");
        } catch ( IOException ex ) {
            System.err.println( "\nError al escribir el objeto" );
        }
    }
    
    private static void autentificacion() throws IOException {
        System.out.println("Esperando autentificación");
        Mensaje mensaje;
        try {
            mensaje = ( Mensaje ) entrada.readObject();
            if (mensaje.getTipoMensaje().equals(TipoMensaje.AUTENTIFICACION_REQUEST)) {
                String[] nomUsuarioClave = ((String) mensaje.getDatos()).split(":");
                Usuario usuario = users.get(nomUsuarioClave[0]);
                if (usuario == null) {

                } else {
                    String password = usuario.getUsername() + "1234567890" + usuario.getPassword() + "0987654321";
                    byte[] passwordBytes = password.getBytes();
                    try {
                        MessageDigest algoritmo = MessageDigest.getInstance("MD5");
                        algoritmo.reset();
                        algoritmo.update(passwordBytes);
                        byte[] clave = algoritmo.digest();
                        StringBuilder hexString = new StringBuilder();
                        for (int i=0;i<clave.length;i++) {
                           String hex = Integer.toHexString(0xFF & clave[i]); 
                           if(hex.length()==1)
                           hexString.append('0');
                           hexString.append(hex);
                        }
                        Mensaje mensajeAEnviar;
                        if (hexString.toString().equals(nomUsuarioClave[1])) { mensajeAEnviar = new Mensaje(TipoMensaje.AUTENTIFICACION_ACK, null); }
                        else { mensajeAEnviar = new Mensaje(TipoMensaje.ERROR, "Usuario o contraseña incorrectos"); }
                        enviarMensaje(mensajeAEnviar);
                    } catch (NoSuchAlgorithmException ex) {
                        String text = "No se ha encontrado el algoritmo especificado";
                        System.err.println(text);
                        Mensaje mensajeAEnviar = new Mensaje(TipoMensaje.ERROR, "Error de MD5");
                        enviarMensaje(mensajeAEnviar);
                        throw new IOException(text);
                    }
                }
            } else {
                String text = "La autentificación a fallado";
                System.err.println(text);
                Mensaje mensajeAEnviar = new Mensaje(TipoMensaje.ERROR, text);
                enviarMensaje(mensajeAEnviar);
                throw new IOException(text);
            }
        } catch (ClassNotFoundException ex) {
            String text = "Error de formato en el mensaje";
            System.err.println(text);
            Mensaje mensajeAEnviar = new Mensaje(TipoMensaje.ERROR, text);
            enviarMensaje(mensajeAEnviar);
            throw new IOException(text);
        }
    }
}
