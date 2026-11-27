package com.vehiculosrd.vehiculos;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/vehiculos")
public class VehiculoController {

    private final VehiculoService vehiculoService;

    public VehiculoController(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    @GetMapping
    public ResponseEntity<?> listar(
            @RequestParam(required = false) String marca,
            @RequestParam(required = false) String modelo,
            @RequestParam(required = false) Integer anio,
            @RequestParam(required = false) BigDecimal precioMin,
            @RequestParam(required = false) BigDecimal precioMax,
            @RequestParam(required = false) String provincia
    ) {
        if (precioMin != null && precioMax != null
                && precioMin.compareTo(precioMax) > 0) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "El precio minimo no puede ser mayor que el precio maximo")
            );
        }

        return ResponseEntity.ok(
                vehiculoService.listar(marca, modelo, anio, precioMin, precioMax, provincia)
        );
    }

    @GetMapping("/mios")
    public List<VehiculoResponse> listarMios(Authentication auth) {
        return vehiculoService.listarPorUsuario(auth.getName());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtener(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(vehiculoService.obtener(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> crear(
            @Valid @RequestBody VehiculoRequest request,
            Authentication auth
    ) {
        VehiculoResponse creado = vehiculoService.crear(request, auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editar(
            @PathVariable UUID id,
            @Valid @RequestBody VehiculoRequest request,
            Authentication auth
    ) {
        try {
            VehiculoResponse actualizado = vehiculoService.editar(id, request, auth.getName());

            return ResponseEntity.ok(Map.of(
                    "mensaje", "Vehiculo actualizado correctamente",
                    "vehiculo", actualizado
            ));

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable UUID id, Authentication auth) {
        try {
            vehiculoService.eliminar(id, auth.getName());

            return ResponseEntity.ok(Map.of(
                    "mensaje", "Vehiculo eliminado correctamente",
                    "id", id
            ));

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}