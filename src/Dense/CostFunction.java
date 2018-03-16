/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Dense;

import Jama.Matrix;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author Procheta
 */
public class CostFunction {

    int numDocs;
    int numWords;
    int embedDim;

    double[][] X;
    double[][] C;
    double[][] W;
    double[][] D;
    double[][] M;

    Matrix sumPart1;
    Matrix sumPart2;

    public CostFunction(int numDocs, int numWords, int embeddDim, String xMatrixpath, String cMatrixpath, String mMatrixPath) throws IOException {
        this.numDocs = numDocs;
        this.numWords = numWords;
        this.embedDim = embeddDim;
        String matrixFilePath = xMatrixpath;
        X = readMatrix(matrixFilePath, numDocs, numDocs);
        matrixFilePath = cMatrixpath;
        C = readMatrix(matrixFilePath, numWords, numDocs);
        W = new double[embeddDim][numWords];
        for(int i = 0; i < embeddDim; i++){
         for(int j = 0; j < numWords; j++)
            W[i][j] = 0.5;
        }
        D = new double[embeddDim][numDocs];
        for(int i = 0; i < embeddDim; i++){
         for(int j = 0; j < numDocs; j++)
            D[i][j] = 0.5;
        }
        matrixFilePath = mMatrixPath;
        M = readMatrix(matrixFilePath, numWords, numWords);

        Matrix X1 = new Matrix(X);
        Matrix C1 = new Matrix(C);
        Matrix C1T = C1.transpose();
        Matrix W1 = new Matrix(W);
        Matrix W1T = W1.transpose();
        Matrix D1 = new Matrix(D);
        Matrix D1T = D1.transpose();
        Matrix M1 = new Matrix(M);

        sumPart1 = C1T.times(W1T);
        sumPart1 = sumPart1.times(D1);

        sumPart2 = C1.times(D1T);
        sumPart2 = sumPart2.times(W1);
    }

    public double[][] readMatrix(String matrixFilePath, int rowNumber, int colNumber) throws FileNotFoundException, IOException {
        FileReader fr = new FileReader(new File(matrixFilePath));
        BufferedReader br = new BufferedReader(fr);

        double[][] m = new double[rowNumber][colNumber];
        String line = br.readLine();
        int i = 0;
        while (line != null) {
            String st[] = line.split(" ");

            for (int j = 0; j < colNumber; j++) {
                m[i][j] = Double.parseDouble(st[j]);
            }
            i++;
            line = br.readLine();
        }
        return m;
    }

    public double computeCostFuncVal() {

        Matrix X1 = new Matrix(X);
        Matrix M1 = new Matrix(M);

        double sum = 0.5 * calForbenniusSum(X1, sumPart1, numDocs, numDocs);
        sum += 0.5 * calForbenniusSum(M1, sumPart2, numDocs, numDocs);
        return sum;
    }

    public double calForbenniusSum(Matrix m1, Matrix m2, int rowNum, int colNum) {
        double sum = 0;
        for (int i = 0; i < rowNum; i++) {
            for (int j = 0; j < colNum; j++) {
                sum += (m1.get(i, j) - m2.get(i, j)) * (m1.get(i, j) - m2.get(i, j));
            }
        }
        return sum;
    }

    public double updateWordMAtrixCoordinate(Matrix m1, Matrix m2, int row, int column) {

        double nominator = 0;
        double denominator = 0;
    
        for (int i = 0; i < numDocs; i++) {
            for (int j = 0; j < numDocs; j++) {
                nominator += (X[i][j] - sumPart1.get(i, j))* m2.get(i, column)* D[row][j];
                denominator += m2.get(i, j) * D[i][j] * m2.get(i, j) * D[i][j];
            }
            nominator += (M[i][column] - sumPart2.get(i, column)) * m1.get(i, row);
            denominator += m1.get(i, row) * m1.get(i, row);
        }
        double w = nominator / denominator;
        return w;
    }

    public double updateDocumentMAtrixCoordinate(Matrix m, int row, int column) {

        double nominator = 0;
        double denominator = 0;
        for (int i = 0; i < numDocs; i++) {
            for (int j = 0; j < numDocs; j++) {
                nominator += (M[i][j] - sumPart2.get(i, j)) * C[i][column] * W[row][j];
                denominator += C[i][column] * W[row][j] * C[i][column] * W[row][j];
            }
            nominator += (X[i][column] - sumPart1.get(i, column)) * m.get(i, row);
            denominator += m.get(i, row) * m.get(i, row);
        }
        double w = nominator / denominator;
        return w;
    }

    public double[][] updateWordMatrix() {

        Matrix C1 = new Matrix(C);
        Matrix C1t = C1.transpose();
        Matrix D1t = new Matrix(D);
        D1t = D1t.transpose();

        Matrix prod = C1.times(D1t);
 
     double[][] WNew = new double[embedDim][numWords];
        for (int i = 0; i < embedDim; i++) {
            for (int j = 0; j < numWords; j++) {
            //    System.out.println("i "+ i +" j "+ j);
              //  System.out.println("previous W[i][j] "+ W[i][j]);
                WNew[i][j] = updateWordMAtrixCoordinate(prod, C1t, i, j);
              //  System.out.println("new value "+WNew[i][j] );
            }
        }

        return WNew;
    }

    public double[][] updateDocMatrix() {

        Matrix C1t = new Matrix(C);
        C1t = C1t.transpose();
        Matrix W1t = new Matrix(W);
        W1t = W1t.transpose();

        Matrix prod = C1t.times(W1t);

        double[][] DNew = new double[embedDim][numDocs];
        for (int i = 0; i < embedDim; i++) {
            for (int j = 0; j < numDocs; j++) {
                DNew[i][j] = updateDocumentMAtrixCoordinate(prod, i, j);
            }
        }
        return DNew;
    }

    public void converge() {

        for (int i = 0; i < 10; i++) {
            double cost = computeCostFuncVal();
            System.out.println("Cost Value at iteration " + i + " is " + cost);
            double[][] w1 = updateWordMatrix();
            double[][] d1 = updateDocMatrix();
            W = w1;
            D = d1;
            
        Matrix X1 = new Matrix(X);
        Matrix C1 = new Matrix(C);
        Matrix C1T = C1.transpose();
        Matrix W1 = new Matrix(W);
        Matrix W1T = W1.transpose();
        Matrix D1 = new Matrix(D);
        Matrix D1T = D1.transpose();
        Matrix M1 = new Matrix(M);

        sumPart1 = C1T.times(W1T);
        sumPart1 = sumPart1.times(D1);

        sumPart2 = C1.times(D1T);
        sumPart2 = sumPart2.times(W1);
            
        }
        
        System.out.println("Print word Embedding Matrix ");
        
        for(int i = 0; i < embedDim; i++){
         for(int j = 0; j < numWords; j++){
             System.out.print(W[i][j] + " ");
         }  
            System.out.println("");
        }
        
        System.out.println("Print Doc Embedding Matrix ");
        
        for(int i = 0; i < embedDim; i++){
         for(int j = 0; j < numDocs; j++){
             System.out.print(D[i][j] + " ");
         }  
            System.out.println("");
        }

    }
    
    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        
        CostFunction ct = new CostFunction(3, 356, 10,"C:\\Users\\Procheta\\Desktop/matrixPath/xmatrix.txt","C:\\Users\\Procheta\\Desktop/matrixPath/cmatrix.txt","C:\\Users\\Procheta\\Desktop/matrixPath/mmatrix.txt");
        ct.converge();
    }

}
