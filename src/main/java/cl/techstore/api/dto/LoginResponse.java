package cl.techstore.api.dto;

public class LoginResponse {
    private String token;
    private String tipo = "Bearer";
    private String expiracion;

    public LoginResponse(String token, String expiracion) {
        this.token = token;
        this.expiracion = expiracion;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getExpiracion() { return expiracion; }
    public void setExpiracion(String expiracion) { this.expiracion = expiracion; }
}