package com.nextgenhealthcare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class NextgenHealthcareBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(NextgenHealthcareBackendApplication.class, args);

	}

}
// import org.springframework.web.bind.annotation.GetMapping; import org.springframework.web.bind.annotation.RestController;
@RestController
 class QuickCheckController {
    @GetMapping("/__quickcheck__")
    public String quick() { return "OK"; }
}

