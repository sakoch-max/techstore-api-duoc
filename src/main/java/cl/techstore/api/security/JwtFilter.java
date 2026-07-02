package cl.techstore.api.security;

import java.io.IOException;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

   @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
    
    String authorizationHeader = request.getHeader("Authorization");
    String token = null;
    String username = null;

    System.out.println("[DEBUG FILTER] Entrando al filtro. Método: " + request.getMethod() + " URL: " + request.getRequestURI());
    System.out.println("[DEBUG FILTER] Header Authorization: " + authorizationHeader);

    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
        token = authorizationHeader.substring(7);
        try {
            username = jwtUtil.extraerUsername(token);
            System.out.println("[DEBUG FILTER] Usuario extraído del token: " + username);
        } catch (Exception e) {
        System.out.println("Error extrayendo el usuario del token: " + e.getMessage());
        e.printStackTrace();
    }
    }

    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        boolean esValido = jwtUtil.validarToken(token);
        System.out.println("[DEBUG FILTER] ¿El token es válido según JwtUtil?: " + esValido);
        
        if (esValido) {
            UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>());
            authToken.setDetails(new org.springframework.security.web.authentication.WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
            System.out.println("[DEBUG FILTER] ¡Autenticación establecida con éxito en Spring Security para " + username + "!");
        }
    } else {
        System.out.println("[DEBUG FILTER] No se intentó validar. Username es nulo o ya había una autenticación activa.");
    }
    
    filterChain.doFilter(request, response);
}
}