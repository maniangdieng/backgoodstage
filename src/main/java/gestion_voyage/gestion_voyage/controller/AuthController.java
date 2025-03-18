package gestion_voyage.gestion_voyage.controller;

import gestion_voyage.gestion_voyage.dto.LoginRequest;
import gestion_voyage.gestion_voyage.entity.Utilisateur;
import gestion_voyage.gestion_voyage.repository.UtilisateurRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

  @PostMapping("/login")
  public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest loginRequest, HttpSession session) {
    Utilisateur utilisateur = utilisateurRepository.findByEmail(loginRequest.getEmail())
      .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

    // Vérifier le mot de passe (sans chiffrement pour simplifier)
    if (utilisateur.getMotDePasse().equals(loginRequest.getMotDePasse())) {
      // Authentification réussie : stocker l'utilisateur dans la session
      session.setAttribute("user", utilisateur);

      Map<String, Object> response = new HashMap<>();
      response.put("message", "Authentification réussie");

      // Ajouter les informations de l'utilisateur à la réponse
      Map<String, Object> userInfo = new HashMap<>();
      userInfo.put("id", utilisateur.getId());
      userInfo.put("nom", utilisateur.getNom());
      userInfo.put("prenom", utilisateur.getPrenom());
      userInfo.put("email", utilisateur.getEmail());
      userInfo.put("role", utilisateur.getRole());

      response.put("user", userInfo); // Ajouter l'utilisateur à la réponse
      return ResponseEntity.ok(response); // Renvoyer une réponse JSON
    } else {
      return ResponseEntity.status(401).body(Map.of("error", "Email ou mot de passe incorrect"));
    }
  }}
