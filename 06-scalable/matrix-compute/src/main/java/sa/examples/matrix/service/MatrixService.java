package sa.examples.matrix.service;

import java.util.Random;

import org.springframework.stereotype.Service;

@Service
public class MatrixService {

    public double multiply(int size, long seed) {
        double[][] a = randomMatrix(size, seed);
        double[][] b = randomMatrix(size, seed + 1);
        double[][] result = new double[size][size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                double sum = 0;
                for (int k = 0; k < size; k++) {
                    sum += a[i][k] * b[k][j];
                }
                result[i][j] = sum;
            }
        }

        return trace(result, size);
    }

    private double[][] randomMatrix(int size, long seed) {
        Random random = new Random(seed);
        double[][] matrix = new double[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = random.nextDouble() * 100;
            }
        }
        return matrix;
    }

    private double trace(double[][] matrix, int size) {
        double sum = 0;
        for (int i = 0; i < size; i++) {
            sum += matrix[i][i];
        }
        return sum;
    }
}
