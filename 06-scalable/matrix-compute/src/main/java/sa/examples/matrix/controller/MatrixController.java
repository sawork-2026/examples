package sa.examples.matrix.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import sa.examples.matrix.model.MatrixResult;
import sa.examples.matrix.service.MatrixService;

@RestController
@RequestMapping("/api/matrix")
public class MatrixController {

    private final MatrixService matrixService;

    public MatrixController(MatrixService matrixService) {
        this.matrixService = matrixService;
    }

    @GetMapping("/multiply")
    public MatrixResult multiply(
            @RequestParam(defaultValue = "200") int size,
            @RequestParam(defaultValue = "42") long seed) {
        long start = System.currentTimeMillis();
        double trace = matrixService.multiply(size, seed);
        long elapsed = System.currentTimeMillis() - start;
        return new MatrixResult(size, trace, elapsed);
    }
}
