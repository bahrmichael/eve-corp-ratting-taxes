package de.bahr.eve.corprattingtaxes;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Resource {

    @Value("${SECRET}")
    private String secret;

    private final ApiPuller puller;

    public Resource(final ApiPuller puller) {
        this.puller = puller;
    }

    @GetMapping("/accessKey/{accessKey}/today")
    public ResponseEntity getToday(@PathVariable("accessKey") String accessKey) {
        if (secret.equals(accessKey)) {
            return ResponseEntity.ok(puller.getTodaysSum());
        } else {
            return ResponseEntity.status(403).build();
        }
    }
}
