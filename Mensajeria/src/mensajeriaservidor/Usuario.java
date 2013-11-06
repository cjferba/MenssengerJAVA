/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mensajeriaservidor;

import comunes.EstadoUsuario;

/**
 *
 * @author Alex Moreno
 */
class Usuario {
    private String username;
    private String name;
    private String password;
    private EstadoUsuario estado;
    private String ip;
    
    public Usuario(String username, String name, String password) {
        this.username = username;
        this.name = name;
        this.password = password;
    }
    
    public String getUsername() {
        return this.username;
    }
    
    public String getName() {
        return this.name;
    }
    
    public String getPassword() {
        return this.password;
    }
    
    public String getEstado() {
        return this.estado.name();
    }
    
    public void setEstado(EstadoUsuario estado) {
        this.estado = estado;
    }
    
    public String getIp() {
        return this.ip;
    }
    
    public void setIp(String ip) {
        this.ip = ip;
    }
}
