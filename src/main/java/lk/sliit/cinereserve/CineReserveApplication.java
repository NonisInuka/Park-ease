package lk.sliit.cinereserve;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CineReserve – Online Movie Ticket Reservation Platform
 * SE1020 Object Oriented Programming – Group 135, Sub Group 07.02
 *
 * Entry point for the Spring Boot application.
 */
@SpringBootApplication
public class CineReserveApplication {

    public static void main(String[] args) {
        SpringApplication.run(CineReserveApplication.class, args);
        System.out.println("=================================================");
        System.out.println("  CineReserve is running!");
        System.out.println("  Open: http://localhost:8080");
        System.out.println("=================================================");
    }
}
