package cl.techstore.api.controller;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import cl.techstore.api.dto.ProductoDTO;
import cl.techstore.api.model.Producto;
import cl.techstore.api.service.ProductoService;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    
    @Autowired
    private SqsClient sqsClient;

    @Autowired
    private ObjectMapper objectMapper;

    private final String QUEUE_NAME = "techstore-audit-queue";

    @GetMapping
    public ResponseEntity<List<Producto>> listar() {
        return ResponseEntity.ok(productoService.listarTodos());
    }

    @PostMapping
    public ResponseEntity<Producto> crear(@RequestBody ProductoDTO dto) {
        Producto nuevoProducto = productoService.crear(dto);
        
        // Disparamos la auditoría tras crear
        enviarAuditoria("CREAR", nuevoProducto);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoProducto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Producto> modificar(@PathVariable Long id, @RequestBody ProductoDTO dto) {
        Producto productoModificado = productoService.modificar(id, dto);
        
        
        enviarAuditoria("MODIFICAR", productoModificado);
        
        return ResponseEntity.ok(productoModificado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
       
        
        productoService.eliminar(id);
        
        
        Producto productoEliminado = new Producto();
        productoEliminado.setId(id);
        productoEliminado.setNombre("Producto Eliminado"); 
        
        enviarAuditoria("ELIMINAR", productoEliminado);
        
        return ResponseEntity.noContent().build(); 
    }

   
    private void enviarAuditoria(String accion, Producto producto) {
        try {
           
            String usuario = SecurityContextHolder.getContext().getAuthentication().getName();
            
           
            String fecha = Instant.now().toString();

           
            Map<String, Object> auditoria = new HashMap<>();
            auditoria.put("accion", accion); 
            auditoria.put("productoId", producto.getId()); 
            auditoria.put("nombre", producto.getNombre()); 
            auditoria.put("usuario", (usuario != null && !usuario.equals("anonymousUser")) ? usuario : "admin@techstore.cl"); // Correo del JWT [cite: 53]
            auditoria.put("fecha", fecha); 

            
            String jsonBody = objectMapper.writeValueAsString(auditoria);

           
            String queueUrl = sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName(QUEUE_NAME).build()).queueUrl();
            
            
            sqsClient.sendMessage(SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(jsonBody)
                    .build());

            System.out.println("[FaaS Audit] Mensaje asíncrono enviado a SQS: " + jsonBody);

        } catch (Exception e) {
            
            System.err.println("[FaaS Audit Error] Fallo al enviar mensaje a SQS: " + e.getMessage());
        }
    }
}