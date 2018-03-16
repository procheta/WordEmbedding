/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package preprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 *
 * @author Procheta
 */
public class Preprocess {

    HashSet<String> WordSet;
    String folderPath;
    int docCount;
    int vocabularySize;
    HashMap<String, Integer> wordCountMatrix;
    ArrayList<String> vocabulary;
    String XMatrixPath;
    String MMatrixPath;
    String CMatrixPath;

    public Preprocess(String folderPath, String xmatrixpath, String mmatrixpath, String cmatrixpath) {
        this.folderPath = folderPath;
        MMatrixPath = mmatrixpath;
        CMatrixPath = cmatrixpath;
        XMatrixPath = xmatrixpath;
        WordSet = new HashSet<>();
        wordCountMatrix = new HashMap<>();
    }

    public void prepareDocTermMatrix() throws FileNotFoundException, IOException {

        File folder = new File(folderPath);
        HashMap<Integer, HashSet<String>> docTermMap = new HashMap<>();
        int count = 0;
        for (File f : folder.listFiles()) {
            FileReader fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);
            String line = br.readLine();
            HashSet<String> docWordSet = new HashSet<>();
            while (line != null) {
                String st[] = line.split(" ");
                for (int i = 0; i < st.length; i++) {
                    WordSet.add(st[i]);
                    docWordSet.add(st[i]);
                    if (wordCountMatrix.containsKey(st[i])) {
                        wordCountMatrix.put(st[i], wordCountMatrix.get(st[i]) + 1);
                    } else {
                        wordCountMatrix.put(st[i], 1);
                    }
                }
                line = br.readLine();
            }
            docTermMap.put(new Integer(count), docWordSet);
            count++;
        }
        docCount = count;
        vocabularySize = WordSet.size();
        double[][] docTermMatrix = new double[WordSet.size()][docCount];

        vocabulary = new ArrayList<>();
        Iterator it = WordSet.iterator();
        while (it.hasNext()) {
            String st = (String) it.next();
            vocabulary.add(st);
        }

        FileWriter fw = new FileWriter(new File(CMatrixPath));
        BufferedWriter bw = new BufferedWriter(fw);
        for (int i = 0; i < docCount; i++) {
            HashSet<String> docWordSet = docTermMap.get(i);
            Integer hh = 1;
            for (int j = 0; j < vocabularySize; j++) {
                if (docWordSet.contains(vocabulary.get(j))) {
                    docTermMatrix[j][i] = 1;
                    bw.write(hh.toString()+  " ");
                } else {
                    docTermMatrix[j][i] = 0;
                    bw.write("0 ");
                }
            }
            bw.newLine();
        }
        bw.close();
        System.out.println(vocabularySize);
    }

    public void prepareMMatrix(int windowSize) throws FileNotFoundException, IOException {
        HashMap<String, ArrayList<String>> wordContextMap = new HashMap<>();

        File folder = new File(folderPath);
        for (File f : folder.listFiles()) {
            FileReader fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);
            String line = br.readLine();
            HashSet<String> docWordSet = new HashSet<>();
            while (line != null) {
                String st[] = line.split(" ");
                for (int i = 0; i < st.length; i++) {
                    ArrayList<String> context = new ArrayList<>();
                    for (int j = i + 1; j < i + windowSize; j++) {
                        if (j < st.length) {
                            context.add(st[j]);
                        }
                    }
                    if (wordContextMap.containsKey(st[i])) {
                        ArrayList<String> ar = wordContextMap.get(st[i]);
                        ar.addAll(context);
                        wordContextMap.put(st[i], ar);
                    } else {
                        wordContextMap.put(st[i], context);
                    }
                }
                line = br.readLine();
            }
        }
        System.out.println(wordContextMap.keySet().size());
        double[][] M = new double[vocabularySize][vocabularySize];
        FileWriter fw = new FileWriter(new File(MMatrixPath));
        BufferedWriter bw = new BufferedWriter(fw);
        for (int i = 0; i < vocabularySize; i++) {
            ArrayList<String> ar = wordContextMap.get(vocabulary.get(i));
            String word1 = vocabulary.get(i);
            for (int j = 0; j < vocabularySize; j++) {
                String word = vocabulary.get(j);
                int count = 0;
                for (int k = 0; k < ar.size(); k++) {
                    if (ar.get(k).equals(word)) {
                        count++;
                    }
                }
                double prob = (double) count / (double) wordCountMatrix.get(word) * (double) wordCountMatrix.get(word1);
                M[i][j] = prob;
                bw.write(new Double(prob).toString() + " ");
            }
            bw.newLine();
        }
        bw.close();
    }

    public void prpareXMatrix() throws IOException {

        double[][] X = new double[docCount][docCount];
        FileWriter fw = new FileWriter(new File(XMatrixPath));
        BufferedWriter bw = new BufferedWriter(fw);
        for (int i = 0; i < docCount; i++) {
            for (int j = 0; j < docCount; j++) {
                X[i][j] = 1;
                bw.write("1 ");
            }
            bw.newLine();
        }
        bw.close();
    }

    public static void main(String[] args) throws IOException {

        Preprocess prep = new Preprocess("C:\\Users\\Procheta\\Desktop/col/", "C:\\Users\\Procheta\\Desktop/matrixPath/xmatrix.txt", "C:\\Users\\Procheta\\Desktop/matrixPath/mmatrix.txt", "C:\\Users\\Procheta\\Desktop/matrixPath/cmatrix.txt");
        prep.prepareDocTermMatrix();
        prep.prepareMMatrix(3);
        prep.prpareXMatrix();
        System.out.println("vocabulary size "+ prep.vocabularySize);
    }

}
