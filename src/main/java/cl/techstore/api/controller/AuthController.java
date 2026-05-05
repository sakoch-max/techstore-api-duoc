package cl.techstore.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.techstore.api.dto.LoginRequest;
import cl.techstore.api.dto.LoginResponse;
import cl.techstore.api.security.JwtUtil;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    
    private final String USUARIO_VALIDO = "admin@techstore.cl";
    private final String PASSWORD_VALIDO = "Admin1234";

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        
       
        if (USUARIO_VALIDO.equals(request.getUsername()) && PASSWORD_VALIDO.equals(request.getPassword())) {
            
            
            String tokenGenerado = jwtUtil.generarToken(request.getUsername());
            
            
            LoginResponse response = new LoginResponse(tokenGenerado, "3600");
            return ResponseEntity.ok(response);
            
        } else {
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales incorrectas");
        }
    }
}